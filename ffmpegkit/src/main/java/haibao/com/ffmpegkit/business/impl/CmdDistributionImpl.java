package haibao.com.ffmpegkit.business.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import haibao.com.ffmpegkit.bean.CommandType;
import haibao.com.ffmpegkit.business.CmdDistribution;
import haibao.com.utilscollection.io.SharedPreferencesUtils;

import static haibao.com.ffmpegkit.business.impl.TaskC.AUDIO_LEVEL;
import static haibao.com.ffmpegkit.business.impl.TaskC.OUT_LEVEL;
import static haibao.com.ffmpegkit.business.impl.TaskC.TO_AUDIO_LEVEL;
import static haibao.com.ffmpegkit.temp.CourseAwareCmd.COURSEWARE_MOVE;
import static haibao.com.ffmpegkit.temp.CourseAwareCmd.COURSEWARE_PAUSE;
import static haibao.com.ffmpegkit.temp.CourseAwareCmd.COURSEWARE_PLAY;
import static haibao.com.ffmpegkit.temp.CourseAwareCmd.COURSEWARE_VIDEO_END;
import static haibao.com.ffmpegkit.temp.CourseAwareCmd.COURSE_LIVE_BEGIN;
import static haibao.com.ffmpegkit.temp.CourseAwareCmd.COURSE_LIVE_CRASH;
import static haibao.com.ffmpegkit.temp.CourseAwareCmd.COURSE_LIVE_END;

/**
 * 默认的任务处理方式
 * 在这里处理业务逻辑。
 * 对任务进行分发。
 * <p>
 * 处理任务的逻辑如下：
 * 1.开始任务。 开始任务默认的逻辑是 幻灯片类型的一级任务。
 * 2.课件移动。 如果上一个一级任务类型是幻灯片。执行该任务。则在改任务内继续添加子图片。
 * 3.课件播放。 执行上一个一级任务(如果是幻灯片，查看是否合成，如果没有合成则还需要合成改文件)。并且开始一个执行视频片段任务。
 * 4.课件暂停。 执行视频片段任务。开始 幻灯片任务。
 * 5.课程结束。 执行最后一个任务。 开启合并一级任务和音频切割任务。 最后合并二级任务。生成最后的视频。
 *
 * @author zzx
 * @date 2017/5/8.
 */

public class CmdDistributionImpl implements CmdDistribution, AbsTask.TaskCompletedCallBack {

    ProgressCallBack mProgressCallBack;
    OnScaleVideoListener scaleListener;
    Gson gson = new Gson();
    Type type = new TypeToken<ArrayList<CommandType>>() {
    }.getType();
    private String TAG = this.getClass().getSimpleName();
    private Context context;
    /**
     * 创建任务后，插入任务列表的头部。 执行任务时，取出任务列表的尾部
     */
    private LinkedList<AbsTask> prepareTaskList = new LinkedList<>();
    /**
     * 当前正在运行的list
     */
    private CopyOnWriteArrayList<AbsTask> runningConcatTask = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<AbsTask> runningCutVideoTaskList = new CopyOnWriteArrayList<>();
    private Vector<AbsTask> runningSlideTaskList = new Vector<>();
    //开始混音的运行list.只有传入的最后音频后，并且完成音频剪切和其余的任务。才会添加到这个集合中。
    private CopyOnWriteArrayList<AbsTask> runningAddAudioTaskList = new CopyOnWriteArrayList<>();
    /**
     * 运行错误的list
     */
    private ArrayList<AbsTask> errorList = new ArrayList<>();
    /**
     * 存放的是完成之后的SlideShow任务。等待一个Concat 方法执行时，将其清空
     */
    private CopyOnWriteArrayList<AbsTask> waitingToConcatSlide = new CopyOnWriteArrayList<>();
    /**
     * level2 是加上声音的结果。  level3 就是最后的结果了
     */
    private CopyOnWriteArrayList<AbsTask> waitingToAddAudio = new CopyOnWriteArrayList<>();
    /**
     * 加入到剪切音频的等待列表
     */
    private CopyOnWriteArrayList<CutAudioTask> cutAudioTasks = new CopyOnWriteArrayList<>();
    private boolean isToEnd;
    private double mRealCourseStartTime;
    private boolean isToConcatEnd;
    private boolean isSlideShowConcatComplete = true;
    private boolean isCutVideoComplete = true;
    private String finalInputAudioPath;
    private boolean isToCutAudio;
    private AtomicInteger cutAudioCount = new AtomicInteger(0);
    private AtomicInteger addAudioCount = new AtomicInteger(0);
    private String lastCourseId;
    //cmd的集合。
    private ArrayList<CommandType> commandTypes;


    public CmdDistributionImpl(Context context) {
        this.context = context;
    }

    @Override
    public void distributionCommand(CommandType command) {
        doWhenCourseAct(command);
    }

    @Override
    public void onDestroy() {

    }

    private void setToSharePref(CommandType command) {
        HashMap<String, String> command_list = SharedPreferencesUtils.getObject("Command_list", HashMap.class);
        commandTypes.add(command);
        String s1 = gson.toJson(commandTypes, type);
        if (command_list == null) {
            command_list = new HashMap<>();
        }
        command_list.put(lastCourseId, s1);
        SharedPreferencesUtils.setObject("Command_list", command_list);
    }

    private ArrayList<CommandType> getFromSharePref(String course_id) {
        HashMap<String, String> command_list = SharedPreferencesUtils.getObject("Command_list", HashMap.class);
        if (command_list == null) {
            command_list = new HashMap<>();
        }
        String s = command_list.get(course_id);
        ArrayList<CommandType> getFromList;

        try {
            getFromList = gson.fromJson(s, type);
        } catch (Exception e) {
            //解析失败。则说明并不存在。
            getFromList = new ArrayList<>();
            //再设置回去。
            String s1 = gson.toJson(getFromList, type);
            command_list.put(course_id, s1);
            SharedPreferencesUtils.setObject("Command_list", command_list);
        }
        return getFromList;
    }

    /**
     * 随着课程进行进行后台合成的代码。存在bug.暂时不按照这种方式了。简化逻辑的复杂程度
     *
     * @param command
     */
    private void doWhenCourseAct(CommandType command) {
        switch (command.cmd_type) {
            case COURSE_LIVE_BEGIN:     //课程直播开始。可能有多次
                Log.i(TAG, "ffmepg course_live_begin");
                //将课程开始的时间标记
                mRealCourseStartTime = command.time;
                createNewSlideShowTaskCommand(context, command);
                break;
            case COURSE_LIVE_END:
                //等所有的任务完成之后，开始等待音频地址的回调，先生成需要分割的音频数据的段落。因为这里的二级。
                isToEnd = true;
                //如果判断上一个任务。
                buildTaskByCommandWhenPLAYAndEnd(command);

                break;
            case COURSEWARE_MOVE:       //有两种可能，一种是播放中进行移动。一个是图片的移动。
                //执行上一个任务
                buildTaskByCommandWhenMove(command);

                //开始下一个任务的创建
                createNewSlideShowTaskCommand(context, command);

                break;
            case COURSEWARE_PLAY:
                //执行上一个任务
                buildTaskByCommandWhenPLAYAndEnd(command);
                //开始下一个任务的创建
                createNewVideoCutTaskCommand(context, command);
                break;
            case COURSEWARE_PAUSE:  //播放暂停。也是同样需要暂停上一个任务。开始下一个任务。上一个任务一定是实行任务。
                //处理视频任务
                buildTaskByCommandWhenPause(command);
                //开始下一个任务的创建
                createNewSlideShowTaskCommand(context, command);

                break;
            case COURSEWARE_VIDEO_END:
                //视频结束。也是执行视频任务。不过需要判断是不是需要 startTime 是不是需要剪切
                buildTaskByCommandWhenVideoEnd(command);

                //开始下一个任务的创建
                createNewSlideShowTaskCommand(context, command);

                break;
            case COURSE_LIVE_CRASH:
                //课件意外暂停，不会开启下一个任务。将上一个任务完成。
                // 如果讲师在使用中关机了。就需要重启的方法
                buildTaskByCommandWhenPLAYAndEnd(command);

                break;
        }
    }

    private void buildTaskByCommandWhenVideoEnd(CommandType command) {
        buildTaskByCutVideoCommand(command);
    }

    private void buildTaskByCommandWhenPause(CommandType command) {
        buildTaskByCutVideoCommand(command);
    }

    private void buildTaskByCommandWhenPLAYAndEnd(CommandType command) {
        //上一个任务是
        buildTaskByCommand(command, true);
    }

    private void buildTaskByCommandWhenMove(CommandType command) {
        //课件移动，则需要取出最后一个课件。
        buildTaskByCommand(command, false);
    }

    private void buildTaskByCutVideoCommand(CommandType command) {
        //获取并移除队列的最后一个元素
        AbsTask absTask = prepareTaskList.pollLast();
        if (absTask.getType() == TaskC.CUT_VIDEO) { //上一个任务只能是剪切视频的任务
            CutVideoTask cutVideoTask = (CutVideoTask) absTask;
            cutVideoTask.addEndCommand(command);
            runningCutVideoTaskList.add(absTask);
            cutVideoTask.execute();
        }

    }

    private void buildTaskByCommand(CommandType command, boolean isToConcatSlide) {
        //课件移动，则需要取出最后一个课件。
        if (prepareTaskList.isEmpty()) {
            return;
        }
        AbsTask absTask = prepareTaskList.pollLast();
        if (absTask == null) {
            return;
        }
        if (absTask.getType() == TaskC.SLIDE_SHOW) { //上一个任务是 slideShow
            isSlideShowConcatComplete = false;
            SlideShowTask showTask = (SlideShowTask) absTask;
            showTask.setToConcat(isToConcatSlide);
            showTask.addEndCommand(command);
            runningSlideTaskList.add(showTask);
            showTask.execute();
            //添加到对应任务列表中
        } else if (absTask.getType() == TaskC.CUT_VIDEO) {    //上一个是视频任务
            //执行视频剪切任务
            isCutVideoComplete = false;
            CutVideoTask showTask = (CutVideoTask) absTask;
            showTask.addEndCommand(command);
            runningCutVideoTaskList.add(showTask);
            showTask.execute();
        }

    }

    private void createNewVideoCutTaskCommand(Context context, CommandType command) {
        //创建一个SlideShow任务。
        CutVideoTask task = new CutVideoTask(context, this);
        task.addStartCommand(command);
        //插入到准备集合的头部
        prepareTaskList.addFirst(task);
    }

    //创建新SlideShow任务。基本上是每个静态的画面都需要的场景
    private void createNewSlideShowTaskCommand(Context context, CommandType command) {
        //创建一个SlideShow任务。
        SlideShowTask task = new SlideShowTask(context, this);
        task.addStartCommand(command);
        //插入到准备集合的头部
        prepareTaskList.addFirst(task);

    }

    //创建slideShow的合并任务
    private void createConcatSlideTask() {
        //如果是slideShow的任务，再对level进行查询。如果taskLevel1 还存在存在文件。则需要进行一个合并的任务
        final CopyOnWriteArrayList<AbsTask> tempWaitingList = this.waitingToConcatSlide;
        int size = tempWaitingList.size();
        if (size == 0) {
            return;
        } else if (size == 1) {  //如果只有一个文件，则直接进入等待音频的list
            AbsTask absTask = tempWaitingList.get(0);
            waitingToAddAudio.add(absTask);
            waitingToConcatSlide.clear();
            //如果只有一个，还需要创建切割音频的任务
            createNewCutAudioTask(absTask);
        } else {
            ConcatTask concatTask = new ConcatTask(context, this);
            concatTask.addInputImagePaths(tempWaitingList);
            runningConcatTask.add(concatTask);
            concatTask.execute();
            //执行后clear level.将处理过的任务清空掉
            waitingToConcatSlide.removeAll(tempWaitingList);
        }
    }

    //接口回调
    @Override
    public void onTaskCompleted(AbsTask task) {
        //如果完成，或者失败，都先将任务移除
        addTaskByLevel(task);
    }

    private void addTaskByLevel(AbsTask task) {
        int level = task.level;
        int type = task.type;
        switch (level) {
            case TaskC.SINGLE_LEVEL:            //单个任务的等级。
                switch (type) {
                    case TaskC.SCALE_IMG:       //缩放图片。这个方法会目前会导致service dead.故不采用
                        // 视频缩放完成！
                        if (scaleListener != null) {
                            scaleListener.onScaleTaskCompleted(task.getOutputPath());
                        }

                        break;
                    case TaskC.SLIDE_SHOW:      //单个幻灯片任务.--->在直播结束。或者开始视频播放的时候。会将之前的任务开始合并
                        //从正在执行的任务中移除
                        runningSlideTaskList.remove(task);
                        //加入等待合并的队列
                        waitingToConcatSlide.add(task);

                        //在直播结束。或者开始视频播放的时候。会将之前的任务开始合并
                        if (((SlideShowTask) task).isToConcat()) {

                            //如果是课件结束直播的话。将会有这个标志位
                            if (isToEnd) {
                                isToConcatEnd = true;
                            }
                            //创建合并任务。就是想之前的任务清空。将自己开始执行。
                            createConcatSlideTask();
                            return;
                        }

                        //todo 下面这部分代码基本上是没办法走到了->
                        //todo 因为如果是isToEnd 最后一个slideShowTask一定是 isToCat。如果是视频任务更不可能走这个代码
                        if (isToEnd && runningSlideTaskList.isEmpty()) {
                            //表示所有的单个完成的任务全部完成！！
                            isToConcatEnd = true;
                            createConcatSlideTask();
                        }
                        break;
                    default:
                }
                break;
            case TO_AUDIO_LEVEL:
                switch (type) {
                    case TaskC.CONCAT_AUDIO:            //如果讲师中断。或者其他原因导致音频生成是有分段构成的。则需要执行该任务。
                        //合并音频合并完成。调用最后设置的方法
                        setFinalInputAudioPath(task.getOutputPath());
                        return;
                    case TaskC.C_SLIDE:                 //多个SlideShow合并成文件。
                        //从正在执行的队列中移除
                        runningConcatTask.remove(task);

                        //判断是否需要开始对音频进行剪切
                        if (isToConcatEnd && runningConcatTask.isEmpty()) {
                            //表示所有的单个SlideShow 合并完成！！
                            isSlideShowConcatComplete = true;
                            //查询是否可以开始执行音频剪切的任务
                            checkToStartAudioCut();
                        }
                        break;
                    case TaskC.CUT_VIDEO:
//                        if (mProgressCallBack != null) {
//                            mProgressCallBack.onCutVideoTaskCompleted(runningSlideTaskList.size() + runningCutVideoTaskList.size());
//                        }
                        //从正在执行的队列中移除
                        runningCutVideoTaskList.remove(task);

                        //判断是否需要开始对音频进行剪切
                        if (isToEnd && runningCutVideoTaskList.isEmpty()) {
                            //表示所有的视频剪切任务完成
                            isCutVideoComplete = true;
                            //查询是否可以开始执行音频剪切的任务
                            checkToStartAudioCut();
                        }
                        break;
                }

                //后两个需要混音的任务。需要创建一个属于自己的音频片段。等待音频输入路径后，开始剪切后得到可以混音的片段。
                //生成一个CutAudio任务。并加入taskListLevel2
                createNewCutAudioTask(task);
                //后面两个任务。需要添加到等待添加音频的队列中
                waitingToAddAudio.add(task);


                break;
            case AUDIO_LEVEL:
                switch (type) {
                    case TaskC.CUT_AUDIO:               //完成剪切音频的任务。这些音频任务。应该让这个任务排列在所有上一个等级的任务完成之后，才能执行
                        if (task instanceof CutAudioTask) {
                            int countCut = cutAudioCount.incrementAndGet();
                            //检查是否完成音频的剪切
                            if (countCut == cutAudioTasks.size()) {  //如果完成的话，则开启混音
                                startToAddAudio();
                            }
                        }
                        break;
                    case TaskC.C_SLIDE_AUDIO:       //如果是合并的幻灯片和剪切的视频的话
                    case TaskC.C_CUT_VIDEO:
                        //更新标记
                        int countConcat = addAudioCount.incrementAndGet();
//                        if (mProgressCallBack != null) {
//                            mProgressCallBack.onAddAudioTaskCompleted(count, runningAddAudioTaskList.size());
//                        }

                        //todo 判断如果混音完，则可以开始最后的合并
                        if (countConcat == runningAddAudioTaskList.size()) {  //如果完成的话，则开启最后的合并
                            startFinalConcat();
                        }
                        break;

                }
                break;
            case OUT_LEVEL:         //最后输出的任务等级
                switch (task.type) {
                    case TaskC.SCALE_VIDEO:     //视频缩放任务
                        // 视频缩放完成！
                        if (scaleListener != null) {
                            scaleListener.onScaleTaskCompleted(task.getOutputPath());
                        }
                        break;
                    case TaskC.F_SLIDE_VIDEO:
                        //视频合并完成！！
                        if (mProgressCallBack != null) {
                            mProgressCallBack.onFinalTaskCompleted(task.outputPath);
                        }
                        break;
                    default:

                        break;
                }
                break;
        }

    }

    //开始最后的合成
    private void startFinalConcat() {
        if (runningAddAudioTaskList.size() == 1) {
            //如果是有一个，则不需要了
            //视频合并完成！！
            if (mProgressCallBack != null) {
                mProgressCallBack.onFinalTaskCompleted(runningAddAudioTaskList.get(0).outputPath);
            }
            return;
        }
        FinalConcatTask finalConcatTask = new FinalConcatTask(context, this);
        finalConcatTask.addInputImagePaths(runningAddAudioTaskList);
        finalConcatTask.execute();

    }

    //开始混音
    private void startToAddAudio() {
        ArrayList<CutAudioTask> tempCuts = new ArrayList<>();
        tempCuts.addAll(cutAudioTasks);

        ArrayList<AbsTask> tempWaitingToAddAudio = new ArrayList<>();
        tempWaitingToAddAudio.addAll(waitingToAddAudio);

        //排序
        Collections.sort(tempCuts);
        Collections.sort(tempWaitingToAddAudio);

        int size = tempWaitingToAddAudio.size();
        if (size != tempCuts.size()) {

//            throw new IllegalArgumentException("pair size is not equal");
        }
        // 取出对应的realStartTime
        for (int x = 0; x < tempCuts.size(); x++) { //取出对应的值。因为之前已经排序了
            CutAudioTask cutAudioTask = tempCuts.get(x);
            AbsTask absTask = tempWaitingToAddAudio.get(x);
            if (cutAudioTask.realStartTime == absTask.realStartTime) {    //证明是一对。则开始
                if (absTask.type == TaskC.CUT_VIDEO) {   //视频文件
                    AddAudioToVideoTask addAudioToVideoTask = new AddAudioToVideoTask(context, this);
                    addAudioToVideoTask.addInputMp4s(absTask.outputPath, absTask.duration, absTask.realStartTime);
                    addAudioToVideoTask.addInputAudioPath(cutAudioTask.outputPath);
                    runningAddAudioTaskList.add(addAudioToVideoTask);
                } else if (absTask.type == TaskC.C_SLIDE || absTask.type == TaskC.SLIDE_SHOW) {                                 //幻灯片文件
                    AddAudioToSlideTask audioToSlideTask = new AddAudioToSlideTask(context, this);
                    audioToSlideTask.addInputMp4s(absTask.outputPath, absTask.duration, absTask.realStartTime);
                    audioToSlideTask.addInputAudioPath(cutAudioTask.outputPath);
                    runningAddAudioTaskList.add(audioToSlideTask);
                }
            }
        }
        if (runningAddAudioTaskList.size() != size) {
            throw new IllegalArgumentException("runningAddAudioTaskList size is not correct!");
        }
        for (int i = 0; i < runningAddAudioTaskList.size(); i++) {      //最后将其运行
            AbsTask absTask = runningAddAudioTaskList.get(i);
            absTask.execute();
        }
    }

    private void createNewCutAudioTask(AbsTask task) {
        CutAudioTask cutAudioTask = new CutAudioTask(context, this);
        cutAudioTask.convertToCutTask(mRealCourseStartTime, task);
        //加入到剪切音频等待列表,但这些任务需要等待得到音频地址后，才能开始执行。执行完成音频的剪切任务后，会去等待添加音频的任务中，找到对应的realStartTime.进行合并音频
        cutAudioTasks.add(cutAudioTask);
    }

    @Override
    public void onTaskError(AbsTask task) {
// TODO: 2017/5/9 错误处理
//        runningSlideTaskList.remove(task);
//        errorList.add(task);
        if (task.type == TaskC.SCALE_VIDEO) {
            // 视频缩放完成！
            if (scaleListener != null) {
                scaleListener.onScaleTaskError();
            }
        }
    }

    public String getFinalInputAudioPath() {
        return finalInputAudioPath;
    }

    public CmdDistributionImpl setFinalInputAudioPath(String finalInputAudioPath) {
        this.finalInputAudioPath=finalInputAudioPath;
//        doWhenCourseActSetFAudio(finalInputAudioPath);
        //如果使用后面一种方案的话。设置最后输入的音频路径才是一切的开始。
        doCourseCombineProcessAfterAll();
        return this;
    }

    private void doCourseCombineProcessAfterAll() {
        //先取出缓存。一个任务一个任务执行。同样输出给
    }

    private void doWhenCourseActSetFAudio(String finalInputAudioPath) {
        this.finalInputAudioPath = finalInputAudioPath;
        //设置之后，开始检查合并任务是否完成。如果完成，则开始音频剪切的任务。
        checkToStartAudioCut();
    }

    private void checkToStartAudioCut() {
        //如果之前的任务全部完成。则可以开始下一步的剪切任务
        if (runningSlideTaskList.isEmpty() && runningConcatTask.isEmpty() && runningCutVideoTaskList.isEmpty() && !TextUtils.isEmpty(finalInputAudioPath)) {
            for (int i = 0; i < cutAudioTasks.size(); i++) {
                cutAudioTasks.get(i).setAudioInputPath(finalInputAudioPath).execute();
            }
            isToCutAudio = true;
        }
    }

    public CmdDistributionImpl setProgressCallBack(ProgressCallBack progressCallBack) {
        mProgressCallBack = progressCallBack;
        return this;
    }

    public void startScaleVideo(int type, int rotation, String scaleVideoPath) {
        ScaleVideoTask scaleVideoTask = new ScaleVideoTask(context, this);
        scaleVideoTask.addInputVideo(type, rotation, scaleVideoPath);
        scaleVideoTask.execute();
    }

    public void startScaleImg(int type, String scaleImagePath) {
        ImageScaleTask scaleVideoTask = new ImageScaleTask(context, this);
        scaleVideoTask.addInputVideo(type, scaleImagePath);
        Callable<Boolean> booleanCallable = scaleVideoTask.buildCallable();
        try {
            Boolean call = booleanCallable.call();
            if (call) {
                if (scaleListener != null) {
                    scaleListener.onScaleTaskCompleted(scaleVideoTask.getOutputPath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public CmdDistributionImpl setScaleListener(OnScaleVideoListener scaleListener) {
        this.scaleListener = scaleListener;
        return this;
    }

    public void setFinalInputAudioPaths(ArrayList<String> audio_list) {
        doWhenCourseActSetFinal(audio_list);

    }

    private void doWhenCourseActSetFinal(ArrayList<String> audio_list) {
        //1,开始一个合并音频的任务
        ConcatAudioTask concatAudioTask = new ConcatAudioTask(context, this);
        concatAudioTask.addInputVideoPaths(audio_list);
        concatAudioTask.execute();
        //2.从结束中。监听到返回的音频
    }

    public void setFinalInputAudioPath(String course_id, String audio) {
        //进行缓存
        setFinalInputAudioPath(audio);
    }

    public void setFinalInputAudioPaths(String course_id, ArrayList<String> audio_list) {
        //进行缓存

        setFinalInputAudioPaths(audio_list);
    }
}
