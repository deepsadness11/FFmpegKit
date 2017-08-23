package haibao.com.ffmpegkit.business.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import haibao.com.ffmpegkit.bean.CommandType;
import haibao.com.ffmpegkit.business.CmdDistribution;

import static haibao.com.ffmpegkit.business.impl.CacheHelper.getFromSharePref;
import static haibao.com.ffmpegkit.business.impl.CacheHelper.setInputAudioPathToSharePref;
import static haibao.com.ffmpegkit.business.impl.CacheHelper.setInputAudioPathsToSharePref;
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
 * 默认的任务处理方式。这里进入的时候。需要将命令缓存起来。完成一个任务之后，去查询是否还有下一个任务需要进行。添加一个命令后，查询是否有正在进行的任务。
 * 在这里处理业务逻辑。
 * 对任务进行分发。
 * <p>
 * 处理任务的逻辑如下：
 * 1.开始任务。 开始任务默认的逻辑是 幻灯片类型的一级任务。
 * 2.课件移动。 如果上一个一级任务类型是幻灯片。执行该任务。则在改任务内继续添加子图片。
 * 3.课件播放。 执行上一个一级任务(如果是幻灯片，查看是否合成，如果没有合成则还需要合成改文件)。并且开始一个执行视频片段任务。
 * 4.课件暂停。 执行视频片段任务。开始 幻灯片任务。
 * 5.课程结束。 执行最后一个任务。 开启合并一级任务和音频切割任务。 最后合并二级任务。生成最后的视频。
 * <p>
 * <p>
 * 视频恢复生成的逻辑暂时不知道要怎么做。暂时只能重新执行一遍cmd了。。
 *
 * @author zzx
 * @date 2017/5/8.
 */

public class CmdDistributionImpl3 implements CmdDistribution, AbsTask.TaskCompletedCallBack {

    ProgressCallBack mProgressCallBack;
    OnScaleVideoListener scaleListener;

    private String TAG = this.getClass().getSimpleName();
    private Context context;
    /**
     * 还未创建好。初始化的任务。
     */
    private LinkedList<AbsTask> prepareTaskList = new LinkedList<>();
    /**
     * 创建的正在排队的任务。
     */
    private LinkedList<AbsTask> waitingTaskList = new LinkedList<>();

    /**
     * 当前正在运行的list
     */
    private LinkedList<AbsTask> runningTaskList = new LinkedList<>();

    //开始混音的运行list.只有传入的最后音频后，并且完成音频剪切和其余的任务。才会添加到这个集合中。
    private CopyOnWriteArrayList<AbsTask> runningAddAudioTaskList = new CopyOnWriteArrayList<>();

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
    /**
     * 用来缓存所有执行过的task的集合。todo 用于最后删除.需要缓存起来。
     */
    private ArrayList<AbsTask> allTask = new ArrayList<>();

    private double mRealCourseStartTime;
    private String finalInputAudioPath;
    private String lastCourseId;
    //cmd的集合。
    private ArrayList<CommandType> commandTypes;
    //等到任务为空时。开始执行剪切音频任务的flag
    private boolean pendingCutAudioTaskFlag;
    private boolean isNeedCleanAllTempOutput = true;
    private boolean isToEnd = false;

    public CmdDistributionImpl3(Context context) {
        this.context = context;
    }

    @Override
    public void distributionCommand(CommandType command) {
        //还是将必要的缓存起来。
        String course_id = command.course_id;
        boolean isNeedRestart = false;
        if (!course_id.equals(lastCourseId)) {
            //和上面的课程不同。就需要重新取数值
            commandTypes = getFromSharePref(course_id);
            lastCourseId = course_id;
            //todo 如果commandTypes不为空。则遍历开始？
//            isNeedRestart = !commandTypes.isEmpty();
        }
//        CacheHelper.setToSharePref(lastCourseId, commandTypes, command);
//        if (isNeedRestart) {    //如果是另外一个课件。检查缓存不为空的时候，需要将原来的任务，全部运行一遍
//            for (CommandType commandType : commandTypes) {
//                doWhenCourseAct(commandType);
//            }
//        }
        doWhenCourseAct(command);
    }

    //Service结束的时候。
    @Override
    public void onDestroy() {
        //刷新一下缓存标记。
        boolean isFinalRuning = finalInputAudioPath != null;
        if (isFinalRuning) {    //音频输入路径不为空时

        }

    }

    /**
     * 每个命令只负责执行任务。
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
                isToEnd = true;
                //等所有的任务完成之后，开始等待音频地址的回调，先生成需要分割的音频数据的段落。因为这里的二级。
                //如果判断上一个任务。
                System.out.println("createNewCutAudioTask COURSE_LIVE_END");
                buildTaskByCommandWhenPLAYAndEnd(command);
                break;
            case COURSEWARE_MOVE:       //有两种可能，一种是播放中进行移动。一个是图片的移动。
                //先创建下一个任务的创建。
                createNewSlideShowTaskCommand(context, command);
                //再执行上一个任务
                buildTaskByCommandWhenMove(command);

                break;
            case COURSEWARE_PLAY:
                //开始下一个任务的创建
                createNewVideoCutTaskCommand(context, command);
                //执行上一个任务
                buildTaskByCommandWhenPLAYAndEnd(command);

                break;
            case COURSEWARE_PAUSE:  //播放暂停。也是同样需要暂停上一个任务。开始下一个任务。上一个任务一定是实行任务。
                //开始下一个任务的创建
                createNewSlideShowTaskCommand(context, command);
                //处理视频任务
                buildTaskByCommandWhenPause(command);

                break;
            case COURSEWARE_VIDEO_END:
                //开始下一个任务的创建
                createNewSlideShowTaskCommand(context, command);
                //视频结束。也是执行视频任务。不过需要判断是不是需要 startTime 是不是需要剪切
                buildTaskByCommandWhenVideoEnd(command);
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
        buildTaskByCommand(command);
    }

    private void buildTaskByCommandWhenMove(CommandType command) {
        //课件移动，则需要取出最后一个课件。
        buildTaskByCommand(command);
    }

    private void buildTaskByCutVideoCommand(CommandType command) {
        //获取并移除队列的最后一个元素
        AbsTask absTask = prepareTaskList.pollLast();
        if (absTask.getType() == TaskC.CUT_VIDEO) { //上一个任务只能是剪切视频的任务
            CutVideoTask cutVideoTask = (CutVideoTask) absTask;
            cutVideoTask.addEndCommand(command);
            waitingTaskList.addLast(cutVideoTask);
        }
        if (runningTaskList.isEmpty()) {
            //如果正在运行的任务为空。则开始执行
            AbsTask waitTask = waitingTaskList.pollLast();
            runningTaskList.addFirst(waitTask);
            allTask.add(waitTask);
            waitTask.execute();
        }
    }

    private void buildTaskByCommand(CommandType command) {
        //课件移动，则需要取出最后一个课件。
        if (prepareTaskList.isEmpty()) {
            return;
        }
        AbsTask absTask = prepareTaskList.pollLast();
        if (absTask == null) {
            return;
        }
        if (absTask.getType() == TaskC.SLIDE_SHOW) { //上一个任务是 slideShow
            SlideShowTask showTask = (SlideShowTask) absTask;
            showTask.addEndCommand(command);
            waitingTaskList.addFirst(showTask);
            //添加到对应任务列表中
        } else if (absTask.getType() == TaskC.CUT_VIDEO) {    //上一个是视频任务
            //执行视频剪切任务
            CutVideoTask cutVideoTask = (CutVideoTask) absTask;
            cutVideoTask.addEndCommand(command);
            waitingTaskList.addFirst(cutVideoTask);
        }
        //检查是否有正在运行的任务。如果没有。则开始执行
        if (runningTaskList.isEmpty()) {
            //如果正在运行的任务为空。则开始执行
            AbsTask waitTask = waitingTaskList.pollLast();
            runningTaskList.addFirst(waitTask);
            allTask.add(waitTask);
            waitTask.execute();
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
    private boolean createConcatSlideTask() {
        boolean isNeedToFindNextToExecute = false;
        final CopyOnWriteArrayList<AbsTask> tempWaitingList = this.waitingToConcatSlide;
        int size = tempWaitingList.size();
        if (size == 0) {    //这种情况不可能出现。
            isNeedToFindNextToExecute = true;
        } else if (size == 1) {  //如果只有一个文件，则说明这个片段可以升级了。加入到等待添加的队列中。并将当前的队列清空
            AbsTask absTask = tempWaitingList.get(0);
            waitingToAddAudio.add(absTask);
            waitingToConcatSlide.clear();
            //如果只有一个，还需要创建切割音频的任务
            createNewCutAudioTask(absTask);
            isNeedToFindNextToExecute = true;
        } else {                //如果是多个。就开始合并吧
            ConcatTask concatTask = new ConcatTask(context, this);
            concatTask.addInputImagePaths(tempWaitingList);
            //加入正在运行的合并中
            runningTaskList.addFirst(concatTask);
            //将队列清空。
            waitingToConcatSlide.removeAll(tempWaitingList);
            concatTask.execute();
            allTask.add(concatTask);
            //执行后clear level.将处理过的任务清空掉
        }
        return isNeedToFindNextToExecute;
    }

    //接口回调
    @Override
    public void onTaskCompleted(AbsTask task) {
        //如果完成，或者失败，都先将任务移除
        addTaskByLevel(task);

    }

    private void addTaskByLevel(AbsTask task) {
        //从正在执行的任务中移除.清除最后一个。
        runningTaskList.pollLast();

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
                        //加入专属于SlideShow的等待合并的队列
                        waitingToConcatSlide.add(task);
                        boolean findNext = true;
                        //查询下一个任务是什么？如果下一个任务是不是SlideShow.则开始合并
                        //应该先查询等待的队列中的情况。在查询准备队列中的情况。
                        boolean waitingNotIsSlideShow = !waitingTaskList.isEmpty() && waitingTaskList.getLast().type != TaskC.SLIDE_SHOW;
                        boolean prepareIsNotSlideShow = !prepareTaskList.isEmpty() && prepareTaskList.getLast().type != TaskC.SLIDE_SHOW;
                        if (waitingNotIsSlideShow || prepareIsNotSlideShow || isToEnd) {   //不为空。而且下一个任务不为SlideShow
                            //那就开始创建合并任务。并开始执行
                            findNext = createConcatSlideTask();
                        }
                        if (findNext) {
                            //正常的分发任务
                            findNextWaitingTaskToExecute();
                        }
                        break;
                    default:
                }
                break;
            case TO_AUDIO_LEVEL:
                switch (type) {
                    case TaskC.CONCAT_AUDIO:            //如果讲师中断。或者其他原因导致音频生成是有分段构成的。则需要执行该任务。
                        //合并音频合并完成。调用最后设置的方法。通常任务应该这个level的最后一个任务了。
                        setFinalInputAudioPath(task.getOutputPath());
                        break;
                    case TaskC.C_SLIDE:                 //多个SlideShow合并成文件。
                        waitingToAddAudio.add(task);
                        //需要创建一个剪切任务
                        createNewCutAudioTask(task);
                        //合并完视频。应该继续找下一个任务。查看时候可以执行
                        findNextWaitingTaskToExecute();
                        break;
                    case TaskC.CUT_VIDEO:
                        waitingToAddAudio.add(task);
                        //需要创建一个剪切任务
                        createNewCutAudioTask(task);
                        //合并完视频。应该继续找下一个任务。查看时候可以执行
                        findNextWaitingTaskToExecute();
                        break;
                }
                break;
            case AUDIO_LEVEL:
                switch (type) {
                    case TaskC.CUT_AUDIO:               //完成剪切音频的任务。这些音频任务。应该让这个任务排列在所有上一个等级的任务完成之后，才能执行
                        //正常的分发任务
                        if (waitingTaskList.isEmpty()) {
                            //剪切音频的任务完成.开启混音的任务。
                            startToAddAudio();
                        } else {
                            findNextWaitingTaskToExecute();
                        }
                        break;
                    case TaskC.C_SLIDE_AUDIO:       //如果是合并的幻灯片和剪切的视频的话
                    case TaskC.C_CUT_VIDEO:
                        System.out.println("waitingTask size=" + waitingTaskList.size());
                        if (waitingTaskList.isEmpty()) {
                            //剪切音频的任务完成.开启混音的任务。
                            startFinalConcat();
                        } else {
                            findNextWaitingTaskToExecute();
                        }
                        break;
                }
                break;
            case OUT_LEVEL:         //最后输出的任务等级
                switch (type) {
                    case TaskC.SCALE_VIDEO:     //视频缩放任务
                        // 视频缩放完成！
                        if (scaleListener != null) {
                            scaleListener.onScaleTaskCompleted(task.getOutputPath());
                        }
                        break;
                    case TaskC.F_SLIDE_VIDEO:
                        //视频合并完成！！
                        resultCleanAndOutput(task.outputPath);
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    /**
     * 找到下一个等待的任务开始执行
     */
    private void findNextWaitingTaskToExecute() {
        if (waitingTaskList.isEmpty() || !runningTaskList.isEmpty()) {
            //判断是否输入了音频
            if (pendingCutAudioTaskFlag) {
                setFinalInputAudioPath(finalInputAudioPath);
            }
            return;
        }
        AbsTask absTask = waitingTaskList.pollLast();
        if (absTask != null) {
            runningTaskList.addFirst(absTask);
            allTask.add(absTask);
            absTask.execute();
        }
    }

    //开始最后的合成
    private void startFinalConcat() {
        System.out.println("startFinalConcat runningAddAudioTaskList size=" + runningAddAudioTaskList.size());
        if (runningAddAudioTaskList.size() == 1) {
            resultCleanAndOutput(runningAddAudioTaskList.get(0).outputPath);
        } else {
            System.out.println("create finalConcatTask");
            FinalConcatTask finalConcatTask = new FinalConcatTask(context, this);
            finalConcatTask.addInputImagePaths(runningAddAudioTaskList);
            finalConcatTask.execute();
        }

    }

    //最后的结果输出和做清除
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void resultCleanAndOutput(String outputPath) {
        //如果是有一个，则不需要了

        if (mProgressCallBack != null) {
            System.out.println("mProgressCallBack!=null");
            mProgressCallBack.onFinalTaskCompleted(outputPath);
        } else {
            System.out.println("mProgressCallBack==null");
        }
        if (isNeedCleanAllTempOutput) {
            for (AbsTask absTask : allTask) {
                String outputPath1 = absTask.getOutputPath();
                if (outputPath1.equals(outputPath)) {
                    continue;
                }
                File tempFile = new File(outputPath1);

                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }

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
            if (cutAudioTask.realStartTime == absTask.realStartTime) {    //TODO 待证明证明是一对。则开始
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
//            throw new IllegalArgumentException("runningAddAudioTaskList size is not correct!");
        }
        for (int i = 0; i < runningAddAudioTaskList.size(); i++) {      //最后将其运行
            AbsTask absTask = runningAddAudioTaskList.get(i);
            //同样加入等待队列
            waitingTaskList.addFirst(absTask);
        }
        //进行查找下一个
        AbsTask absTask = waitingTaskList.pollLast();
        if (absTask != null) {
            runningTaskList.addFirst(absTask);
            allTask.add(absTask);
            absTask.execute();
        }
    }

    private void createNewCutAudioTask(AbsTask task) {
        CutAudioTask cutAudioTask = new CutAudioTask(context, this);
        cutAudioTask.convertToCutTask(mRealCourseStartTime, task);
        //加入到剪切音频等待列表,但这些任务需要等待得到音频地址后，才能开始执行。执行完成音频的剪切任务后，会去等待添加音频的任务中，找到对应的realStartTime.进行合并音频
        System.out.println("createNewCutAudioTask 添加剪切任务");
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

        if (mProgressCallBack != null) {
            System.out.println("mProgressCallBack!=null");
            mProgressCallBack.onFinalTaskError(task.getOutputPath());
        }
    }

    public CmdDistributionImpl3 setFinalInputAudioPath(String finalInputAudioPath) {
        this.finalInputAudioPath = finalInputAudioPath;
//        doWhenCourseActSetFAudio(finalInputAudioPath);
        //如果使用后面一种方案的话。设置最后输入的音频路径才是一切的开始。
        doCourseCombineProcessAfterAll();
        return this;
    }

    //是指多个音频输入路径时。通常是所有命令的最后。输入
    public void setFinalInputAudioPaths(ArrayList<String> audio_list) {
        //1,开始一个合并音频的任务
        ConcatAudioTask concatAudioTask = new ConcatAudioTask(context, this);
        concatAudioTask.addInputVideoPaths(audio_list);
        //查询是否有在等待的任务。
        if (!waitingTaskList.isEmpty() || !runningTaskList.isEmpty()) {   //如果有等待的任务。放入到等到的任务的第一个
            waitingTaskList.addFirst(concatAudioTask);
        } else {    //如果没有任务在执行和等待。则执行这个任务。
            runningTaskList.addFirst(concatAudioTask);
            allTask.add(concatAudioTask);
            concatAudioTask.execute();
        }
    }

    private void doCourseCombineProcessAfterAll() {
        //输入音频之后。开始音频剪切的任务。
        if (!waitingTaskList.isEmpty() || !runningTaskList.isEmpty()) {   //如果有等待的任务。放入到等到的任务的第一个
            pendingCutAudioTaskFlag = true;
        } else {    //如果没有任务在执行和等待。则执行这个任务。
            pendingCutAudioTaskFlag = false;
            //遍历。将这个任务全部加入等待队列当中。再开始下一个任务的执行
            for (int i = 0; i < cutAudioTasks.size(); i++) {
                CutAudioTask cutAudioTask = cutAudioTasks.get(i);
                cutAudioTask.setAudioInputPath(finalInputAudioPath);
                waitingTaskList.addFirst(cutAudioTask);
            }
            System.out.println("start cutlist=" + cutAudioTasks.size());
            AbsTask absTask = waitingTaskList.pollLast();
            if (absTask != null) {
                runningTaskList.addFirst(absTask);
                allTask.add(absTask);
                absTask.execute();
            }
        }
    }


    public CmdDistributionImpl3 setProgressCallBack(ProgressCallBack progressCallBack) {
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


    public CmdDistributionImpl3 setScaleListener(OnScaleVideoListener scaleListener) {
        this.scaleListener = scaleListener;
        return this;
    }

    public void setFinalInputAudioPath(String course_id, String audio) {
        //进行缓存
        setInputAudioPathToSharePref(course_id, audio);
        setFinalInputAudioPath(audio);
    }

    public void setFinalInputAudioPaths(String course_id, ArrayList<String> audio_list) {
        //进行缓存
        setInputAudioPathsToSharePref(course_id, audio_list);
        setFinalInputAudioPaths(audio_list);
    }

    public void resolveAllCommand(String course_id, ArrayList<CommandType> commandTypes) {
        ArrayList<CommandType> fromSharePref = getFromSharePref(course_id);
        commandTypes.addAll(fromSharePref);
        for (CommandType commandType : commandTypes) {
            doWhenCourseAct(commandType);
        }
        //先查询是否有多音频地址
        String inputAudioPathToSharePref = CacheHelper.getInputAudioPathToSharePref(course_id);
        if (!TextUtils.isEmpty(inputAudioPathToSharePref)) {
            setFinalInputAudioPath(inputAudioPathToSharePref);
        } else {
            ArrayList<String> inputAudioPathsToSharePref = CacheHelper.getInputAudioPathsToSharePref(course_id);
            if (inputAudioPathsToSharePref != null && !inputAudioPathsToSharePref.isEmpty()) {
                setFinalInputAudioPaths(inputAudioPathsToSharePref);
            }
        }
    }

    public void resumeAllTask(String course_id) {
        if (commandTypes == null) {
            commandTypes = new ArrayList<>();
        } else {
            commandTypes.clear();
        }
        ArrayList<CommandType> fromSharePref = getFromSharePref(course_id);
        commandTypes.addAll(fromSharePref);
        for (CommandType commandType : commandTypes) {
            doWhenCourseAct(commandType);
        }
        //先查询是否有多音频地址
        String inputAudioPathToSharePref = CacheHelper.getInputAudioPathToSharePref(course_id);
        if (!TextUtils.isEmpty(inputAudioPathToSharePref)) {
            setFinalInputAudioPath(inputAudioPathToSharePref);
        } else {
            ArrayList<String> inputAudioPathsToSharePref = CacheHelper.getInputAudioPathsToSharePref(course_id);
            if (inputAudioPathsToSharePref != null && !inputAudioPathsToSharePref.isEmpty()) {
                setFinalInputAudioPaths(inputAudioPathsToSharePref);
            }
        }
    }
}
