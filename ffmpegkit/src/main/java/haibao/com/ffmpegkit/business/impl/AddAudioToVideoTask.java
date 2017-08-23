package haibao.com.ffmpegkit.business.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.Callable;

import haibao.com.ffmpegkit.FFmpegJNIWrapper;
import haibao.com.ffmpegkit.business.FileGeneratorHelper;
import haibao.com.ffmpegkit.cmd.MixingAVCommand;
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

public class AddAudioToVideoTask extends AbsTask {

    String inputAudioFile;
    private String inputMp4Path;

    public AddAudioToVideoTask(Context context, TaskCompletedCallBack completedCallBack) {
        super(context, TaskC.AUDIO_LEVEL, TaskC.C_CUT_VIDEO, completedCallBack);
    }

    public AddAudioToVideoTask addInputMp4s(String inputMp4Path, double duration, double realStartTime) {
        this.inputMp4Path = inputMp4Path;
        this.duration = duration;
        this.realStartTime = realStartTime;
        return this;
    }

    public AddAudioToVideoTask addInputAudioPath(String inputAudioFile) {
        this.inputAudioFile = inputAudioFile;
        return this;
    }

    @Override
    protected Callable<Boolean> buildCallable() {
        if (!checkInputParamsNotNull()) {
            return null;
        }
        outputPath = FileGeneratorHelper.getInstance().getAddAudioFile(realStartTime);
        //生成对应的Command
        Command slideCommand = new MixingAVCommand.Builder()
                .setOutputFile(outputPath)
                .setInputAudioFile(inputAudioFile)
                .setInputVideoFile(inputMp4Path)
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
        if (TextUtils.isEmpty(inputAudioFile)) {
//            throw new IllegalArgumentException("command params is already initial?");
            return false;
        }
        if (!FileUtils.isFilexists(inputAudioFile) || !FileUtils.isFilexists(inputMp4Path)){
            return false;
        }
        return true;
    }

}
