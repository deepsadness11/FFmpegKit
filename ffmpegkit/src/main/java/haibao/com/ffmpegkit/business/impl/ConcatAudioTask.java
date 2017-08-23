package haibao.com.ffmpegkit.business.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import haibao.com.ffmpegkit.FFmpegJNIWrapper;
import haibao.com.ffmpegkit.business.FileGeneratorHelper;
import haibao.com.ffmpegkit.cmd.ConcentrateAVCommand;
import haibao.com.ffmpegkit.commands.Command;
import haibao.com.ffmpegkit.utils.FileUtils;

import static haibao.com.ffmpegkit.business.impl.TaskC.TO_AUDIO_LEVEL;
import static haibao.com.ffmpegkit.cmd.ConcentrateAVCommand.Builder.INPUT_AUDIO;

/**
 * todo　合并音频的任务。
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
public class ConcatAudioTask extends AbsTask {
    private String definePath;
    private ArrayList<String> inputAudioPaths;

    public ConcatAudioTask(Context context, TaskCompletedCallBack completedCallBack) {
        super(context, TO_AUDIO_LEVEL, TaskC.CONCAT_AUDIO, completedCallBack);
    }

    public void addInputVideoPaths(ArrayList<String> inputAudioPaths) {
        outputPath = FileGeneratorHelper.getInstance().getConcatOutputAudioFilePath((int) System.currentTimeMillis());
        this.inputAudioPaths = inputAudioPaths;
        String definedFileContentPath = getDefinedAudioFilePath(inputAudioPaths);
        definePath = FileGeneratorHelper.getInstance().getConcatInputFilePath(definedFileContentPath, (int) System.currentTimeMillis());
    }

    @Override
    protected Callable<Boolean> buildCallable() {
        if (!checkInputParamsNotNull()) {
            return null;
        }

        Command concatAvCommand = new ConcentrateAVCommand.Builder()
                .setGenerateMode(INPUT_AUDIO)
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
        if (inputAudioPaths == null || inputAudioPaths.isEmpty()) {
            return false;
        }
        boolean result = true;
        for (int i = 0; i < inputAudioPaths.size(); i++) {
            if (!FileUtils.isFilexists(inputAudioPaths.get(i))) {
                result = false;
            }
        }
        return result;

    }

    //返回生成的cat 文件的地址
    private String getDefinedAudioFilePath(ArrayList<String> inputImagePaths) {
        StringBuilder contentBuilder = new StringBuilder();
        for (String inputImagePath : inputImagePaths) {
            contentBuilder.append("file '").append(inputImagePath).append("'").append('\n');
        }
        return contentBuilder.toString();
    }

}
