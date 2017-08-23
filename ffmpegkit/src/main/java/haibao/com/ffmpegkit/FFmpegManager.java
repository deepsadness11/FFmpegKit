package haibao.com.ffmpegkit;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import haibao.com.ffmpegkit.bean.CommandType;
import haibao.com.ffmpegkit.service.FFmpegRemoteService;
import haibao.com.ffmpegkit.utils.AppCheckUtils;

import static android.content.Context.BIND_AUTO_CREATE;
import static haibao.com.ffmpegkit.service.FFmpegRemoteService.FINAL_ERROR;
import static haibao.com.ffmpegkit.service.FFmpegRemoteService.FINAL_OUTPUT;
import static haibao.com.ffmpegkit.service.FFmpegRemoteService.FINAL_PATH;
import static haibao.com.ffmpegkit.service.FFmpegRemoteService.SCALE_IMG_ERROR;
import static haibao.com.ffmpegkit.service.FFmpegRemoteService.SCALE_IMG_SUCCESS;
import static haibao.com.ffmpegkit.service.FFmpegRemoteService.SCALE_VIDEO_ERROR;
import static haibao.com.ffmpegkit.service.FFmpegRemoteService.SCALE_VIDEO_PROGRESS;
import static haibao.com.ffmpegkit.service.FFmpegRemoteService.SCALE_VIDEO_SUCCESS;

/**
 * haibao.com.ffmpegkit
 * <p>
 * ${CLASS_NAME}
 *
 * @author Administrator
 * @date 2017/5/5.
 */

public class FFmpegManager {
    public static final String CMD = "CMD";
    public static final String SCALE_VIDEO_OUTPATH = "scale_video_outpath";
    public static final String SCALE_IMG_OUTPATH = "scale_img_outpath";

    private static final String TAG = "FFmpegManager";
    private static FFmpegManager fFmpegManager;
    private OnScaleListener mOnScaleListener;
    private OnFinalListener mOnFinalListener;
    private Messenger mService;
    private boolean isBinding = false;
    private int resumeTaskCourseId;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            isBinding = true;
            Log.i(TAG, "绑定成功");
            //如果是回复任务。则

            if (!mTempCommandTypes.isEmpty()) {
                for (CommandType tempCommandType : mTempCommandTypes) {
                    sendComandType(tempCommandType);
                }
                mTempCommandTypes.clear();
            }

            if (isToResumeTask) {
                //发送回复command
                if (resumeTaskCourseId != 0) {
                    sendResumeTask(String.valueOf(resumeTaskCourseId));
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            isBinding = false;
            Log.i(TAG, "断开连接");
        }
    };

    //记录command的index
    private int commandIndex = 0;
    //处理来自进程B回复的消息
    private Messenger mMessenger = new Messenger(new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == FFmpegRemoteService.GET_RESULT) {
                Log.i(TAG, "Int form process B is " + msg.arg1);//msg.arg1就是remoteInt
            } else if (msg.what == FFmpegRemoteService.RESULT_OK) {
                switch (msg.arg1) {
                    case 200:
//                        Toast.makeText(context, "完成混音任务！！速速去查看", Toast.LENGTH_SHORT).show();
                        break;
                    case SCALE_VIDEO_SUCCESS:
                        Bundle data = msg.getData();
                        String outputPath = data.getString(SCALE_VIDEO_OUTPATH);
                        if (mOnScaleListener != null) {
                            mOnScaleListener.onScaleVideoCompleted(outputPath);
                        }
                        break;
                    case SCALE_VIDEO_ERROR:
//                    Toast.makeText(context, "视频任务失败", Toast.LENGTH_SHORT).show();
                        if (mOnScaleListener != null) {
                            mOnScaleListener.onScaleVideoError();
                        }
                        break;
                    case SCALE_VIDEO_PROGRESS:
                        int progress = msg.arg2;
                        System.out.println("ffmpeg progress=" + msg.arg2);
                        //传递给callBack
                        if (mOnScaleListener != null) {
                            mOnScaleListener.onScaleVideoProgress(progress);
                        }
                        break;
                    case SCALE_IMG_SUCCESS:
                        Bundle data2 = msg.getData();
                        String outputPath2 = data2.getString(SCALE_IMG_OUTPATH);
                        if (mOnScaleListener != null) {
                            mOnScaleListener.onScaleVideoCompleted(outputPath2);
                        }
                        break;
                    case SCALE_IMG_ERROR:
                        if (mOnScaleListener != null) {
                            mOnScaleListener.onScaleVideoError();
                        }
                        break;
                    case FINAL_ERROR:
                        if (mOnFinalListener != null) {
                            System.out.println("mOnFinalListener !=null");
                            mOnFinalListener.onFinalError();
                        } else {
                            System.out.println("mOnFinalListener ==null");
                        }
                        break;
                    case FINAL_PATH:
                        System.out.println("Get FINAL｜path");
                        Bundle data3 = msg.getData();
                        String outputPath3 = data3.getString(FINAL_OUTPUT);
                        System.out.println("Get FINAL｜path outputPath3" + outputPath3);
                        //传递给最后的人。完成任务
                        if (mOnFinalListener != null) {
                            System.out.println("mOnFinalListener !=null");
                            mOnFinalListener.onFinalOutput(outputPath3);
                        } else {
                            System.out.println("mOnFinalListener ==null");
                        }
                        break;
                    default:
//                        Toast.makeText(context, "完成Single任务!", Toast.LENGTH_SHORT).show();
                        break;
                }
            } else {
                super.handleMessage(msg);
            }
        }
    });
    private ArrayList<CommandType> mTempCommandTypes = new ArrayList<>();
    private boolean isToResumeTask;

    private FFmpegManager() {

    }

    public static FFmpegManager getInstance() {
        if (fFmpegManager == null) {
            fFmpegManager = new FFmpegManager();
        }
        return fFmpegManager;
    }

    public void bindRemoteService(Context context) {
        //绑定进程B的服务
        context.bindService(new Intent(context, FFmpegRemoteService.class), mConnection, BIND_AUTO_CREATE);
    }

    //关闭service
    public void startService(Context context) {
        context.startService(new Intent(context, FFmpegRemoteService.class));
    }

    //关闭service
    public void stopService(Context context) {
        if (serviceIsRunning(context)) {
            context.stopService(new Intent(context, FFmpegRemoteService.class));
        }
    }

    public boolean serviceIsRunning(Context context) {

        boolean serviceRunning = AppCheckUtils.isServiceRunning(context, "haibao.com.ffmpegkit.service.FFmpegRemoteService");
        return serviceRunning;
    }

    public boolean isBinding() {
        if (isBinding && mConnection != null && mService != null) {
            return true;
        }else {
            return false;
        }
    }

    public void unbindRemoteService(Context context) {
        if (mConnection != null && mService != null) {
            //绑定进程B的服务
            context.unbindService(mConnection);
        }
        mOnScaleListener = null;
    }

    public void sendCommand(CommandType commandType) {
        //检查service是否存在。如果不存则，则开启service
//        if (!serviceIsRunning(BaseApplication.getInstance())) {
//            //重新开启service ,重新绑定
//            startService(BaseApplication.getInstance());
//            bindRemoteService(BaseApplication.getInstance());
//        }
//        if (!isBinding) {
//            mTempCommandTypes.add(commandType);
//            return;
//        } else {
//            sendComandType(commandType);
//        }
    }

    //先注释
    private void sendComandType(CommandType tempCommandType) {
//        Message message = Message.obtain(null, FFmpegRemoteService.COMMAND_SEND);
//        message.replyTo = mMessenger;
//        Bundle bundle = new Bundle();
//        bundle.putParcelable(CMD, tempCommandType);
//        message.setData(bundle);
//
//        try {
//            mService.send(message);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }

    }

    public void sendAudioPath(String course_id, String path) {
//        Toast.makeText(context, "设置音频路径中...", Toast.LENGTH_SHORT).show();
        Message message = Message.obtain(null, FFmpegRemoteService.AUDIO_PATH);
        message.replyTo = mMessenger;
        Bundle bundle = new Bundle();
        bundle.putString("audio", new File(path).getAbsolutePath());
        bundle.putString("course_id", course_id);
        message.setData(bundle);
        try {
            mService.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void sendResumeTask(String course_id) {
        Message message = Message.obtain(null, FFmpegRemoteService.RESUME_TASK);
        Bundle bundle = new Bundle();
        bundle.putString("course_id", course_id);
        message.replyTo = mMessenger;
        try {
            mService.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendAudioPaths(String course_id, ArrayList<String> paths) {
//        Toast.makeText(context, "设置音频路径中...", Toast.LENGTH_SHORT).show();
        Message message = Message.obtain(null, FFmpegRemoteService.AUDIO_PATHS);
        message.replyTo = mMessenger;
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("audio_list", paths);
        bundle.putString("course_id", course_id);
        message.setData(bundle);
        try {
            mService.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    //缩放视频的时候需要
    public void sendScaleVideoPath(int type, int rotation, String path, OnScaleListener mOnScaleListener) {
        this.mOnScaleListener = mOnScaleListener;
        Message message = Message.obtain(null, FFmpegRemoteService.SCALE_VIDEO);
        message.replyTo = mMessenger;
        Bundle bundle = new Bundle();
        bundle.putString("scale", new File(path).getAbsolutePath());
        bundle.putInt("rotation", rotation);
        bundle.putInt("type", type);
        message.setData(bundle);
        try {
            mService.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //缩放图片的时候需要
    public void sendScaleImagePath(int type, String path, OnScaleListener mOnScaleListener) {
        this.mOnScaleListener = mOnScaleListener;
        Message message = Message.obtain(null, FFmpegRemoteService.SCALE_IMAGE);
        message.replyTo = mMessenger;
        Bundle bundle = new Bundle();
        bundle.putString("scale", new File(path).getAbsolutePath());
        bundle.putInt("type", type);
        message.setData(bundle);
        try {
            mService.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public OnFinalListener getOnFinalListener() {
        return mOnFinalListener;
    }

    public FFmpegManager setOnFinalListener(OnFinalListener onFinalListener) {
        this.mOnFinalListener = onFinalListener;
        return this;
    }

    public void resumeTask(int course_id) {
        isToResumeTask = true;
        resumeTaskCourseId = course_id;
        if (!serviceIsRunning(FFmpegKit.getContext())) {
            //重新开启service ,重新绑定
            startService(FFmpegKit.getContext());
            bindRemoteService(FFmpegKit.getContext());
        }

    }

    public interface OnScaleListener {
        void onScaleVideoCompleted(String outputPath);

        void onScaleVideoError();

        void onScaleVideoProgress(int progress);
    }

    public interface OnFinalListener {
        void onFinalOutput(String output);

        void onFinalError();
    }
}
