package haibao.com.ffmpegkit.business.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.Callable;

import haibao.com.ffmpegkit.FFmpegJNIWrapper;
import haibao.com.ffmpegkit.bean.CommandType;
import haibao.com.ffmpegkit.business.FileGeneratorHelper;
import haibao.com.ffmpegkit.cmd.CutVideoCommand;
import haibao.com.ffmpegkit.commands.Command;
import haibao.com.ffmpegkit.utils.FileUtils;


/**
 * haibao.com.ffmpegkit.business.impl
 * <p>
 * ${CLASS_NAME}
 *
 * @author Administrator
 * @date 2017/5/9.
 */

public class CutVideoTask extends AbsTask {

    String inputFile;

    public CutVideoTask(Context context, TaskCompletedCallBack completedCallBack) {
        super(context, TaskC.TO_AUDIO_LEVEL, TaskC.CUT_VIDEO, completedCallBack);
    }

    public void addStartCommand(CommandType command) {
        realStartTime = command.time;
        startTime = command.video_time;
//        FileGeneratorHelper.getInstance();
        inputFile = command.inputPath;
        //将realStartTime传入
        outputPath = FileGeneratorHelper.getInstance().getVideoCutOutputFilePath(realStartTime);
    }

    public void addEndCommand(CommandType command) {
        // TODO: 2017/5/9 待检查
        double endTime;
        if (command.video_time == 0) {    //如果videoTime 为0 。则说明是move?
            endTime = command.time;       //就用当前的移动量来充当.那同样需要将去的是实际的时间。这里的时间已经是unix时间了。不需要再除以1000
            duration = (endTime - realStartTime);
        } else {
            endTime = command.video_time;
            duration = (endTime - startTime);
        }
    }

    @Override
    protected Callable<Boolean> buildCallable() {
        if (!checkInputParamsNotNull()) {
            return null;
        }

        Command command = new CutVideoCommand.Builder()
                .setInputFile(inputFile)
                .setOutputFile(outputPath)
                .setStartTime(String.valueOf(startTime))
                .setDuration(String.valueOf(duration))
                .build();

        final String cmd = command.getCommand();

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

    @Override
    protected boolean checkInputParamsNotNull() {
        if (TextUtils.isEmpty(inputFile)) {
            return false;
        }
        if (FileUtils.isFilexists(inputFile)) {
            return false;
        }
        return true;
    }
}
