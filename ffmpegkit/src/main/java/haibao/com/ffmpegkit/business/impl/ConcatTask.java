package haibao.com.ffmpegkit.business.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import haibao.com.ffmpegkit.FFmpegJNIWrapper;
import haibao.com.ffmpegkit.business.FileGeneratorHelper;
import haibao.com.ffmpegkit.cmd.ConcentrateAVCommand;
import haibao.com.ffmpegkit.commands.Command;
import haibao.com.ffmpegkit.utils.FileUtils;

import static haibao.com.ffmpegkit.business.impl.TaskC.TO_AUDIO_LEVEL;
import static haibao.com.ffmpegkit.cmd.ConcentrateAVCommand.Builder.INPUT_FILE;

/**
 * SlideShowTask
 * <p>
 * 合并任务。合并slideShow
 * 需要：
 * 输入文件的地址。
 * <p>
 * 逻辑：
 *
 * @author zzx
 * @time 2017/5/8
 */
public class ConcatTask extends AbsTask {
    private String definePath;
    private ArrayList<AbsTask> inputImagePaths;

    public ConcatTask(Context context, TaskCompletedCallBack completedCallBack) {
        super(context, TO_AUDIO_LEVEL, TaskC.C_SLIDE, completedCallBack);

    }

    public void addInputImagePaths(CopyOnWriteArrayList<AbsTask> inputImagePaths) {
        ArrayList<AbsTask> absTasks = new ArrayList<>();
        absTasks.addAll(inputImagePaths);
        //先进行排序
        Collections.sort(absTasks);
        this.inputImagePaths = absTasks;
        //取出第一个标记开始的时间
        AbsTask absTask = inputImagePaths.get(0);
        realStartTime = absTask.realStartTime;

        outputPath = FileGeneratorHelper.getInstance().getConcatOutputFilePath(realStartTime * level);
        String definedFileContentPath = getDefinedFilePath(absTasks);
        definePath = FileGeneratorHelper.getInstance().getConcatInputFilePath(definedFileContentPath, (int) (realStartTime * level));

        for (AbsTask task : absTasks) {
            duration += task.getDuration();
        }
    }

    @Override
    protected Callable<Boolean> buildCallable() {
        if (!checkInputParamsNotNull()) {
            return null;
        }

        Command concatAvCommand = new ConcentrateAVCommand.Builder()
                .setGenerateMode(INPUT_FILE)
                .setInputDefinedFile(definePath)
                .setOutputFile(outputPath)
                .build();

        final String cmd = concatAvCommand.getCommand();

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
        if (TextUtils.isEmpty(definePath) || TextUtils.isEmpty(outputPath)) {
//            throw new IllegalArgumentException("command params is already initial?");
            return false;
        }
        if (inputImagePaths == null || inputImagePaths.isEmpty()) {
            return false;
        }

        for (AbsTask inputImagePath : inputImagePaths) {
            if (!FileUtils.isFilexists(inputImagePath.getOutputPath())) {
                return false;
            }
        }
        return true;
    }

    //返回生成的cat 文件的地址
    private String getDefinedFilePath(ArrayList<AbsTask> inputImagePaths) {
        StringBuilder contentBuilder = new StringBuilder();
        for (AbsTask inputImagePath : inputImagePaths) {
            String outputPath = inputImagePath.getOutputPath();
            double duration = inputImagePath.getDuration();
            contentBuilder.append("file '").append(outputPath).append("'").append('\n');
            contentBuilder.append("duration ").append(duration).append('\n');
        }

        return contentBuilder.toString();
    }

}
