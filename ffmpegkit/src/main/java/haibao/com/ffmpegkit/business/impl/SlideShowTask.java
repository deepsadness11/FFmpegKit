package haibao.com.ffmpegkit.business.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.Callable;

import haibao.com.ffmpegkit.FFmpegJNIWrapper;
import haibao.com.ffmpegkit.bean.CommandType;
import haibao.com.ffmpegkit.business.FileGeneratorHelper;
import haibao.com.ffmpegkit.cmd.SlideShowCommand;
import haibao.com.ffmpegkit.commands.Command;
import haibao.com.ffmpegkit.utils.FileUtils;

import static haibao.com.ffmpegkit.cmd.SlideShowCommand.Builder.SINGLE_IMAGE_NO_AUDIO;

/**
 * SlideShowTask
 * <p>
 * 幻灯片任务
 * <p>
 * 需要：
 * 图片地址的集合。
 * 声音文件
 * duration 时长
 * <p>
 * 逻辑：
 * 1. 添加一个图片的地址，和对应的时长之后，就开始执行一个子任务。
 * 2. build任务后，就回自动创建一个合并任务。
 * 3. 执行任务完成之后，需要将子任务生成的文件删除掉。
 *
 * @author zzx
 * @time 2017/5/8
 */
public class SlideShowTask extends AbsTask {

    //是否从改项目开始合并
    boolean isToConcat;

    private String inputImagePath;

    public SlideShowTask(Context context, TaskCompletedCallBack completedCallBack) {
        super(context, TaskC.SINGLE_LEVEL, TaskC.SLIDE_SHOW, completedCallBack);

    }

    public void addStartCommand(CommandType command) {
        realStartTime = command.time;
//        FileGeneratorHelper.getInstance();
        inputImagePath = command.inputPath;
        outputPath = FileGeneratorHelper.getInstance().getSlideShowOutputFilePath(realStartTime);
    }

    public void addEndCommand(CommandType command) {
        //因为传入的时间就是unix时间了。所以不需要/1000
        duration = (command.time - realStartTime);
    }

    @Override
    protected Callable<Boolean> buildCallable() {
        if (!checkInputParamsNotNull()) {
            return null;
        }
        //取证
//        final float tempDuration = (float) (Math.round(duration * 10)) / 10;
        //这里默认就是整数了。不需要取整数了
        final double tempDuration = duration;

        //长度为0。则不进行操作？
        if (duration == 0) {
            return null;
        }

        //(这里的100就是2位小数点,如果要其它位,如4位,这里两个100改成10000)
        Command slideCommand = new SlideShowCommand.Builder()
                .setInputImageFile(inputImagePath)
                .setOutputFile(outputPath)
                .setGenerateMode(SINGLE_IMAGE_NO_AUDIO)
                .setDuration(String.valueOf(tempDuration))
                .setFps(2)
                .build();

        final String cmd = slideCommand.getCommand();

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
        if (duration == -1 || TextUtils.isEmpty(inputImagePath)) {
//            throw new IllegalArgumentException("command params is already initial?");
            return false;
        }
        return FileUtils.isFilexists(inputImagePath);
    }

    public boolean isToConcat() {
        return isToConcat;
    }

    public SlideShowTask setToConcat(boolean toConcat) {
        isToConcat = toConcat;
        return this;
    }
}
