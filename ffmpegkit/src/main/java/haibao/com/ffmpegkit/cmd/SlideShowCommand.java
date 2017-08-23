package haibao.com.ffmpegkit.cmd;


import haibao.com.ffmpegkit.commands.BaseCommand;
import haibao.com.ffmpegkit.commands.Command;

import static haibao.com.ffmpegkit.utils.TextUtils.cmdFormat;

/**
 * SlideShowCommand
 * 将图片和音频生成对应的视频
 *
 * @author zzx
 * @time 2017/4/18 0018
 */
public class SlideShowCommand extends BaseCommand {

    //对单个图片进行处理
    private static final String CMD = "ffmpeg -y -loop 1 -i %d -i %d -vcodec h264 -b:v 1000K -b:a 64K -r %s -shortest -pix_fmt yuv420p %s";
    private static final String CMD2 = "ffmpeg -y -safe 0 -f concat -i %d -vsync vfr -vcodec h264 -b:v 1000K -r %s -pix_fmt yuv420p %s";
    private static final String CMD3 = "ffmpeg -y -loop 1 -i %s " +
//            "-r %d " +
            "-t %s -vcodec libx264 -b:v 1000K -preset ultrafast -pix_fmt yuv420p %s";
    String target = "ffmpeg -loop 1 -i 00654QmXgy1feo7x24iaaj30zk1kwapz.jpg -i cutAudio0.wma -vcodec h264 -b:v 1000K -b:a 64K -r 2 -shortest slideshow1-0.mp4";
    String target2 = "ffmpeg -safe 0 -f concat -i input.txt -vsync vfr -vcodec h264 -b:v 1000K -b:a 64K -shortest slideshow2-0.mp4";

    private SlideShowCommand(String command) {
        super(command);
    }

    public static class Builder implements IBuilder {

        public static final int SINGLE_IMAGE_ADDING_AUDIO = 1;
        public static final int MULTI_IMAGE_NO_AUDIO = 2;
        public static final int SINGLE_IMAGE_NO_AUDIO = 3;
        /**
         * 生成模式。
         * 1---> 1张图片 1个音频生成
         * 2---> 多张图片 0个音频生成
         */
        int generateMode;

        int fps;
        String duration;

        String inputImageFile;
        String inputAudioFile;
        String inputDurationFile;

        String outputFile;


        public Builder setInputDurationFile(String inputDurationFile) {
            this.inputDurationFile = inputDurationFile;
            return this;
        }

        public Builder setGenerateMode(int generateMode) {
            this.generateMode = generateMode;
            return this;
        }

        public Builder setFps(int fps) {
            this.fps = fps;
            return this;
        }

        public Builder setInputImageFile(String inputImageFile) {
            this.inputImageFile = inputImageFile;
            return this;
        }

        public Builder setInputAudioFile(String inputAudioFile) {
            this.inputAudioFile = inputAudioFile;
            return this;
        }

        public Builder setOutputFile(String outputFile) {
            this.outputFile = outputFile;
            return this;
        }

        public Builder setDuration(String duration) {
            this.duration = duration;
            return this;
        }

        @Override
        public Command build() {
            String cmd = "";
            if (generateMode == SINGLE_IMAGE_ADDING_AUDIO) {
                cmd = cmdFormat(CMD, inputImageFile, inputAudioFile, fps, outputFile);
            } else if (generateMode == MULTI_IMAGE_NO_AUDIO) {
                cmd = cmdFormat(CMD2, inputDurationFile, fps, outputFile);
            } else if (generateMode == SINGLE_IMAGE_NO_AUDIO) {
                cmd = cmdFormat(CMD3, inputImageFile
//                        , fps
                        , duration, outputFile);
            }
            return new SlideShowCommand(cmd);
        }
    }
}
