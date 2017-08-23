package haibao.com.ffmpegkit.business.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.Callable;

import haibao.com.ffmpegkit.FFmpegJNIWrapper;
import haibao.com.ffmpegkit.business.FileGeneratorHelper;
import haibao.com.ffmpegkit.utils.FileUtils;

import static haibao.com.ffmpegkit.business.impl.TaskC.OUT_LEVEL;

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
public class ScaleVideoTask extends AbsTask {

    int type;
    int rotation;
    private String inputVideoPath;

    public ScaleVideoTask(Context context, TaskCompletedCallBack completedCallBack) {
        super(context, OUT_LEVEL, TaskC.SCALE_VIDEO, completedCallBack);

    }

    public void addInputVideo(int type, int rotation, String scaleVideoPath) {
        this.type = type;
        this.rotation = rotation;
        inputVideoPath = scaleVideoPath;

        outputPath = FileGeneratorHelper.getInstance().getScaleVideoOutFilePath(scaleVideoPath);
//        String definedFileContentPath = getDefinedFilePath(absTasks);
//        definePath = FileGeneratorHelper.getInstance().getConcatInputFilePath(definedFileContentPath, realStartTime * level);
//
    }

    @Override
    protected Callable<Boolean> buildCallable() {
        if (!checkInputParamsNotNull()) {
            return null;
        }
        int targetH = 600;
        int targetW = 800;
        //todo 开始一个视频缩放的任务
        String vfPad;
        if (type == 1) {   //4.3
            vfPad = "-vf [a]pad=iw:iw*3/4:0:(oh-ih)/2[b];[b]scale=800:600";
        } else {//16.9
            vfPad = "-vf [a]pad=ih*16/9:ih:(ow-iw)/2:0[b];[b]scale=950:536";
            targetH = 534;
            targetW = 950;
        }

        String Rotation = "";
        switch (rotation) {
            case 90:
//                Rotation = ",transpose=1 ";
                break;
            case 270:
                Rotation = ",transpose=2 ";
                break;
            case 180:
                Rotation = ",transpose=2,transpose=1";
                break;
            default:
                Rotation = "";
//                hasRotation = false;
                break;
        }

//        ffmpeg -i 145931.mp4 -vf "scale=min(iw*600/ih\,800):min(600\,ih*800/iw),pad=800:600:(800-iw)/2:(600-ih)/2" -acodec aac -vcodec h264 hand3.mp4

        final String cmd = "ffmpeg -y -i " + inputVideoPath + " -vf scale=min(iw*" + targetH + "/ih\\," + targetW + "):min(" + targetH + "\\,ih*" + targetW + "/iw),pad=" + targetW + ":" + targetH + ":(" + targetW + "-iw)/2:(" + targetH + "-ih)/2" + Rotation + " -acodec aac -vcodec h264 -b:v 1000K -b:a 64K -r 20 -preset ultrafast " + outputPath;
//        final String cmd = "ffmpeg -i " + inputVideoPath + " " + vfPad + Rotation + " -metadata:s:v rotate=\"\" -acodec libfaac -vcodec libx264 " + outputPath;

//
//        Command concatAvCommand = new ConcentrateAVCommand.Builder()
//                .setGenerateMode(INPUT_FILE)
//                .setInputDefinedFile(definePath)
//                .setOutputFile(outputPath)
//                .build();
//
//        final String cmd = concatAvCommand.getCommand();

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
        if (TextUtils.isEmpty(inputVideoPath) || TextUtils.isEmpty(outputPath)) {
            return false;
        }
        return FileUtils.isFilexists(inputVideoPath);
    }

}
