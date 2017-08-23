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

import static haibao.com.ffmpegkit.business.impl.TaskC.OUT_LEVEL;
import static haibao.com.ffmpegkit.cmd.ConcentrateAVCommand.Builder.INPUT_FILE;

/**
 * SlideShowTask
 * <p>
 * 合并任务。合并slideShow 和 最后合并时会用到
 * <p>
 * 需要：
 * 输入文件的地址。
 * <p>
 * 逻辑：
 *
 * @author zzx
 * @time 2017/5/8
 */
public class FinalConcatTask extends AbsTask {
    private String definePath;
    private ArrayList<AbsTask> tempInputs;

    public FinalConcatTask(Context context, TaskCompletedCallBack completedCallBack) {
        super(context, OUT_LEVEL, TaskC.F_SLIDE_VIDEO, completedCallBack);

    }

    public void addInputImagePaths(CopyOnWriteArrayList<AbsTask> inputImagePaths) {
        ArrayList<AbsTask> tempInputs = new ArrayList<>();
        tempInputs.addAll(inputImagePaths);
        //先进行排序
        this.tempInputs = tempInputs;
        Collections.sort(tempInputs);
        //取出第一个标记开始的时间
        AbsTask absTask = tempInputs.get(0);
        realStartTime = absTask.realStartTime;

        outputPath = FileGeneratorHelper.getInstance().getConcatOutputFilePath((realStartTime + 365) * level);
        String definedFileContentPath = getDefinedFilePath(tempInputs);
        definePath = FileGeneratorHelper.getInstance().getConcatInputFilePath(definedFileContentPath, (int) ((realStartTime + 365) * level));

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
            return false;
        }
        if (tempInputs == null || tempInputs.isEmpty()) {
            return false;
        }
        for (AbsTask tempInput : tempInputs) {
            if (!FileUtils.isFilexists(tempInput.getOutputPath())) {
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
