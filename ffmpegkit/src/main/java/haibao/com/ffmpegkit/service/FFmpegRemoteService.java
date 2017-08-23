package haibao.com.ffmpegkit.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import haibao.com.ffmpegkit.FFmpegJNIWrapper;
import haibao.com.ffmpegkit.R;
import haibao.com.ffmpegkit.bean.CommandType;
import haibao.com.ffmpegkit.business.impl.CmdDistributionImpl3;
import haibao.com.ffmpegkit.business.impl.OnScaleVideoListener;
import haibao.com.ffmpegkit.business.impl.ProgressCallBack;

import static haibao.com.ffmpegkit.FFmpegManager.CMD;
import static haibao.com.ffmpegkit.FFmpegManager.SCALE_IMG_OUTPATH;
import static haibao.com.ffmpegkit.FFmpegManager.SCALE_VIDEO_OUTPATH;

public class FFmpegRemoteService extends Service {

    public static final int GET_RESULT = 1;
    public static final int SCALE_VIDEO = 2;
    public static final int COMMAND_SEND = 3;
    public static final int AUDIO_PATH = 4;
    public static final int FINAL_PATH = 5;
    public static final int AUDIO_PATHS = 6;
    public static final int SCALE_IMAGE = 7;
    public static final int RESUME_TASK = 8;
    public static final int RESULT_OK = -1;
    public static final int SCALE_VIDEO_SUCCESS = 1001;
    public static final int SCALE_VIDEO_ERROR = 1002;
    public static final int SCALE_VIDEO_PROGRESS = 1003;
    public static final int SCALE_IMG_SUCCESS = 1004;
    public static final int SCALE_IMG_ERROR = 1005;
    public static final int FINAL_ERROR = 1006;
    public static final String FINAL_OUTPUT = "FINAL_OUTPUT";
    private static final int SERVERCE_NOTIFICATION_UID = 110;
    String TAG = "FFmpegRemoteService";
    private int remoteInt = 0;//返回到进程A的值

    private Messenger mReplyTo;
    private CmdDistributionImpl3 mCmdDistribution;
    //开启一个线程，进行回调
    private int mProcess;

    private boolean isRunning;

    private ProgressCallBack mProgressCallBack = new ProgressCallBack() {
        @Override
        public void onFinalTaskCompleted(String outputPath) {
            isRunning=false;
            //完整的合并完视频
            System.out.println("onFinalTaskCompleted" + outputPath);
            if (mReplyTo != null) {
                System.out.println("mReplyTo onFinalTaskCompleted" + outputPath);
                Bundle resultBundle = new Bundle();
                resultBundle.putString(FINAL_OUTPUT, outputPath);
                Message obtain = Message.obtain(null, RESULT_OK);
                obtain.arg1 = FINAL_PATH;
                obtain.setData(resultBundle);
                try {
                    mReplyTo.send(obtain);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("mReplyTo ==null");
            }
        }

        @Override
        public void onSingleSlideTaskCompleted(int totalSize) {

        }

        @Override
        public void onSlideConcatTaskCompleted(int totalSize) {

        }

        @Override
        public void onCutVideoTaskCompleted(int totalSize) {

        }

        @Override
        public void onCutAudioTaskCompleted(int count, int size) {

        }

        @Override
        public void onAddAudioTaskCompleted(int count, int size) {

        }

        @Override
        public void onFinalTaskError(String outputPath) {
            isRunning=false;
            sendErrorToRemote(outputPath);
        }
    };

    private void sendErrorToRemote(String outputPath) {
        System.out.println("onFinalTaskError" + outputPath);
        if (mReplyTo != null) {
            System.out.println("mReplyTo onFinalTaskError" + outputPath);
            Bundle resultBundle = new Bundle();
            Message obtain = Message.obtain(null, RESULT_OK);
            obtain.arg1 = FINAL_ERROR;
            obtain.setData(resultBundle);
            try {
                mReplyTo.send(obtain);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("mReplyTo ==null");
        }
    }

//    private boolean isFinalTaskSetFlag; //是否发送到最后的任务

    //通信模块
    private final Messenger mMessenger = new Messenger(new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            mReplyTo = msg.replyTo;
            isRunning=true;
            switch (msg.what) {
                case GET_RESULT:
                    try {
//                    如要设置classLoader
                        bundle.setClassLoader(CommandType.class.getClassLoader());
                        CommandType command = bundle.getParcelable(CMD);
                        if (command != null) {

                            mReplyTo.send(Message.obtain(null, GET_RESULT, remoteInt, 0));
                            remoteInt++;
                            Log.i(TAG, "FFmpegRemoteService remoteInt==" + remoteInt);
                            Log.i(TAG, command.toString());
//                        distributionJobByCommand(command);
                            mCmdDistribution.distributionCommand(command);
                        }
                        String audio = bundle.getString("audio");
                        if (!TextUtils.isEmpty(audio)) {
                            //上传音频之后，设置监听事件
                            mCmdDistribution.setProgressCallBack(mProgressCallBack);
                            mCmdDistribution.setFinalInputAudioPath(audio);
                        }
//
//                    doAudioMixToVideo();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case COMMAND_SEND:
                    bundle.setClassLoader(CommandType.class.getClassLoader());
                    CommandType command = bundle.getParcelable(CMD);
                    if (command != null) {

                        mCmdDistribution.distributionCommand(command);
                    }
                    break;
                case AUDIO_PATH: {
                    String audio = bundle.getString("audio");
                    String course_id = bundle.getString("course_id");
                    if (!TextUtils.isEmpty(audio)) {
                        mCmdDistribution.setProgressCallBack(mProgressCallBack);
                        mCmdDistribution.setFinalInputAudioPath(course_id, audio);
                    }
                    break;
                }
                case AUDIO_PATHS: {
                    //发送音频的数组。
                    ArrayList<String> audio_list = bundle.getStringArrayList("audio_list");
                    String course_id = bundle.getString("course_id");
                    if (audio_list == null) {
                        return;
                    }
                    if (audio_list.isEmpty()) {
                        return;
                    }
                    //上传音频之后，设置监听事件
                    mCmdDistribution.setProgressCallBack(mProgressCallBack);
                    mCmdDistribution.setFinalInputAudioPaths(course_id, audio_list);

                    break;
                }
                case SCALE_VIDEO: {
                    String scaleVideoPath = bundle.getString("scale");
                    int rotation = bundle.getInt("rotation");
                    int type = bundle.getInt("type");   //比例的类型 1=4.3 2=16.9


                    if (!TextUtils.isEmpty(scaleVideoPath)) {
                        mCmdDistribution.setScaleListener(new OnScaleVideoListener() {
                            @Override
                            public void onScaleTaskCompleted(String outputPath) {
                                //完成视频剪切任务
                                Bundle data = new Bundle();
                                data.putString(SCALE_VIDEO_OUTPATH, outputPath);
                                sendFinalMessge(SCALE_VIDEO_SUCCESS, data);
                            }

                            @Override
                            public void onScaleTaskError() {
                                //任务失败
                                sendFinalMessge(SCALE_VIDEO_ERROR);
                            }
                        });
                        //开启进度的监听
                        startNewThreadForProgress();
                        mCmdDistribution.startScaleVideo(type, rotation, scaleVideoPath);
                    }
                    break;
                }
                case SCALE_IMAGE: {
                    String scaleImgPath = bundle.getString("scale");
                    int type = bundle.getInt("type");   //比例的类型 1=4.3 2=16.9


                    if (!TextUtils.isEmpty(scaleImgPath)) {
                        mCmdDistribution.setScaleListener(new OnScaleVideoListener() {
                            @Override
                            public void onScaleTaskCompleted(String outputPath) {
                                //完成视频剪切任务
                                Bundle data = new Bundle();
                                data.putString(SCALE_IMG_OUTPATH, outputPath);
                                sendFinalMessge(SCALE_IMG_SUCCESS, data);
                            }

                            @Override
                            public void onScaleTaskError() {
                                //任务失败
                                sendFinalMessge(SCALE_IMG_ERROR);
                            }
                        });
                        //暂时不需要进度的监听
//                    startNewThreadForProgress();
                        mCmdDistribution.startScaleImg(type, scaleImgPath);
                    }

                    break;
                }
                case RESUME_TASK: {
                    String course_id = bundle.getString("course_id");
                    mCmdDistribution.setProgressCallBack(mProgressCallBack);
                    mCmdDistribution.resumeAllTask(course_id);
                    break;
                }
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    });

    public FFmpegRemoteService() {
        mCmdDistribution = new CmdDistributionImpl3(this);
        initCrash();
        //保存

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 在API11之后构建Notification的方式
        Notification.Builder builder = new Notification.Builder
                (getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent();
        nfIntent.setClassName(this, "haibao.com.course.CourseLiveActivity");
        builder
                .setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))// 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        R.mipmap.ic_launcher)) // 设置下拉列表中的图标(大图标)
                .setContentTitle("视频生成中") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("孩宝小镇") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

        Notification notification = builder.getNotification(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音


        startForeground(SERVERCE_NOTIFICATION_UID, notification);// 开始前台服务

        return super.onStartCommand(intent, flags, startId);
    }


    private void initCrash() {
        // install
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, final Throwable ex) {
                isRunning=false;
                //调用reply
                sendErrorToRemote("");

                System.out.println("thread.getName() =" + thread.getName());
                ex.printStackTrace();
                try {
//                    CrashHandlerUtils.saveToSDCard(BaseApplication.getInstance(), Common.DIR_ERROR_LOG, ex);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        mExecutorService = Executors.newFixedThreadPool(1);
//        mTempMp4OutputPaths = new ArrayList<>();
//        mTempDuration = new ArrayList<>();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private void sendFinalMessge(int resultCode) {
        //发送消息会主线程
        try {
            if (mReplyTo != null) {
                mReplyTo.send(Message.obtain(null, RESULT_OK, resultCode, 0));
                Log.i(TAG, "发送消息~");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendFinalMessge(int resultCode, int arg2) {
        //发送消息会主线程
        try {
            if (mReplyTo != null) {
                mReplyTo.send(Message.obtain(null, RESULT_OK, resultCode, arg2));
                Log.i(TAG, "发送消息~");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendFinalMessge(int resultCode, Bundle bundle) {
        //发送消息会主线程
        try {
            if (mReplyTo != null) {
                Message obtain = Message.obtain(null, RESULT_OK, resultCode, 0);
                obtain.setData(bundle);
                mReplyTo.send(obtain);
                Log.i(TAG, "发送消息~");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void startNewThreadForProgress() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mProcess < 100) {
                    int process = FFmpegJNIWrapper.call_progress(FFmpegRemoteService.this);
                    if (mProcess != process) {
                        mProcess = process;
                        System.out.println("FFMPEG REMOTE progresss==" + mProcess);
                        //发送消息给客户端
                        sendFinalMessge(SCALE_VIDEO_PROGRESS, mProcess);
                    }
                }
                mProcess = 0;
            }
        }).start();
    }

    //停止当前的任务
    private void cancelCurrentTask() {
        FFmpegJNIWrapper.cancelTask(FFmpegRemoteService.this);
    }


    @Override
    public void onDestroy() {
        //刷新一下是否运行完任务。
        if (mCmdDistribution != null) {
            mCmdDistribution.onDestroy();
        }
        super.onDestroy();
    }
}
