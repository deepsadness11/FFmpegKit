package haibao.com.ffmpegkit.business.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.Callable;

import haibao.com.ffmpegkit.FFmpegJNIWrapper;
import haibao.com.ffmpegkit.business.FileGeneratorHelper;
import haibao.com.ffmpegkit.cmd.CutAudioCommand;
import haibao.com.ffmpegkit.commands.Command;
import haibao.com.ffmpegkit.utils.FileUtils;


/**
 * DESCRIPTION: 切割视频的任务的level 应该是2
 * Author: Cry
 * DATE: 17/5/9 上午12:58
 */

public class CutAudioTask extends AbsTask {

    String audioInputPath;  //等到最后才添加

    public CutAudioTask(Context context, TaskCompletedCallBack completedCallBack) {
        super(context, TaskC.AUDIO_LEVEL, TaskC.CUT_AUDIO, completedCallBack);
    }

    //通过完成的二级任务来。转化成音频切割
    public void convertToCutTask(double realCourseStartTime, AbsTask task) {
        if (task.level != TaskC.TO_AUDIO_LEVEL && task.level != TaskC.SINGLE_LEVEL) {//因为有可能是SLideSHow
            return;
        }
        //fixme 得到开始的时间
        this.realStartTime = task.realStartTime;
        //通过实际的开课时间来决定发送的时间
        startTime = (int) ((realStartTime - realCourseStartTime) / 1000.f);
        //得到时长
        duration = task.getDuration();
    }

    @Override
    protected Callable<Boolean> buildCallable() {
        if (!checkInputParamsNotNull()) {
            return null;
        }
        Log.i(TAG, "ffmepg doCutAudio");

        //生成输出的文件.完成任务后，需要将生成的位置保存起来。或者现在就保存起来
        outputPath = FileGeneratorHelper.getInstance().generateTempAudioFilePathByRealStartTime(realStartTime);

        //获取截取的总时长

        //生成对应的Command
        Command cutCommand = new CutAudioCommand.Builder()
                .setStartTime(String.valueOf(startTime))
                .setDuration(String.valueOf(duration))
                .setInputFile(audioInputPath)
                .setOutputFile(outputPath)
                .build();
        final String cmd = cutCommand.getCommand();

        Log.i(TAG, "ffmepg cmd=" + cmd);

        return new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                Log.i(TAG, "ffmepg result start");
                int result = FFmpegJNIWrapper.call_ffmpegWrapper(context, cmd.split(" "));
                Log.i(TAG, "ffmepg result===" + result);
                System.out.println("Command===" + cmd);
                System.out.println("Command Completed Result===" + result);

                return true;
            }
        };
    }

    private String generateTempAudioFilePathByAudioIndex() {
        return null;
    }

    @Override
    protected boolean checkInputParamsNotNull() {
        if (TextUtils.isEmpty(audioInputPath)) {
//            throw new IllegalArgumentException("command params is already initial?");
            return false;
        }
        return FileUtils.isFilexists(audioInputPath);
    }

    public CutAudioTask setAudioInputPath(String audioInputPath) {
        this.audioInputPath = audioInputPath;
        return this;
    }
}
