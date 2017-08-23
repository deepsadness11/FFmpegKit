package haibao.com.ffmpegkit.business.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.Callable;

import haibao.com.ffmpegkit.FFmpegJNIWrapper;
import haibao.com.ffmpegkit.business.FileGeneratorHelper;
import haibao.com.ffmpegkit.utils.FileUtils;


/**
 * 图片剪切和缩放的任务。
 * 在执行幻灯片任务之前需要进行校验。如果校验不通过，则需要进行缩放和剪切。
 *
 * @author zzx
 * @time 2017/5/8
 */
public class ImageScaleTask extends AbsTask {

    private String inputImagePath;
    private int ConfigType;

    public ImageScaleTask(Context context, TaskCompletedCallBack completedCallBack) {
        super(context, TaskC.SINGLE_LEVEL, TaskC.SCALE_IMG, completedCallBack);

    }

    @Override
    protected Callable<Boolean> buildCallable() {
        if (!checkInputParamsNotNull()) {
            return null;
        }

        String scaleCmd = "";
        if (ConfigType == 1) {
            scaleCmd = "scale=800:600";
        } else {
            scaleCmd = "scale=950:534";
        }

        final String cmd = "ffmpeg -y -i " + inputImagePath + " -vf " + scaleCmd + " " + outputPath;
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
        if (TextUtils.isEmpty(inputImagePath)) {
//            throw new IllegalArgumentException("command params is already initial?");
            return false;
        }
        return FileUtils.isFilexists(inputImagePath);
    }

    public void addInputVideo(int type, String scaleImagePath) {
        this.ConfigType = type;
        inputImagePath = scaleImagePath;
        outputPath = FileGeneratorHelper.getInstance().getScaleImageOutputFilePath();

    }
}
