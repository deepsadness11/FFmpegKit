package haibao.com.ffmpegkit.business.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.Callable;

import haibao.com.ffmpegkit.FFmpegJNIWrapper;
import haibao.com.ffmpegkit.business.FileGeneratorHelper;
import haibao.com.ffmpegkit.cmd.AudioToSlideShowCommand;
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

public class AddAudioToSlideTask extends AbsTask {

    private String inputAudioFile;
    private String inputMp4Path;

    public AddAudioToSlideTask(Context context, TaskCompletedCallBack completedCallBack) {
        super(context, TaskC.AUDIO_LEVEL, TaskC.C_SLIDE_AUDIO, completedCallBack);
    }

    public AddAudioToSlideTask addInputMp4s(String inputMp4Path, double duration, double realTime) {
        this.inputMp4Path = inputMp4Path;
        this.duration = duration;
        this.realStartTime = realTime;
        return this;
    }

    public AddAudioToSlideTask addInputAudioPath(String inputAudioFile) {
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
        Command slideCommand = new AudioToSlideShowCommand.Builder()
                .setOutputFile(outputPath)
                .setInputAudioFile(inputAudioFile)
                .setInputSlideShowVideoFile(inputMp4Path)
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
//            callbackError(new IllegalArgumentException("command params is already initial?"));
            return false;
        }
        if (!FileUtils.isFilexists(inputAudioFile) || !FileUtils.isFilexists(inputMp4Path)) {
//            callbackError(new IllegalArgumentException("Input File is not exit!"));
            return false;
        }
        return true;
    }
}
