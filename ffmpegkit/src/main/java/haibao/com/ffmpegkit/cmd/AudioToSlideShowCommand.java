package haibao.com.ffmpegkit.cmd;


import haibao.com.ffmpegkit.commands.BaseCommand;
import haibao.com.ffmpegkit.commands.Command;

import static haibao.com.ffmpegkit.utils.TextUtils.cmdFormat;

/**
 * MixingAVCommand
 * <p>
 * 将音频混音到视频中的Command
 *
 * @author zzx
 * @time 2017/4/19 0019
 */
public class AudioToSlideShowCommand extends BaseCommand {
    //注意，这里只能剪切 wma
    private static final String CMD = "ffmpeg -y -i %s -i %s -c copy -map 0:v -map 1:a %s";
    String target = "ffmpeg -y -i %s -i %s -c copy -map 0:v -map 1:a %s";

    private AudioToSlideShowCommand(String command) {
        super(command);
    }

    public static class Builder implements IBuilder {

        String inputAudioFile;
        String inputSlideShowVideoFile;

        String outputFile;

        public Builder setInputAudioFile(String inputAudioFile) {
            this.inputAudioFile = inputAudioFile;
            return this;
        }

        public Builder setInputSlideShowVideoFile(String inputSlideShowVideoFile) {
            this.inputSlideShowVideoFile = inputSlideShowVideoFile;
            return this;
        }

        public Builder setOutputFile(String outputFile) {
            this.outputFile = outputFile;
            return this;
        }

        @Override
        public Command build() {
            String cmd = cmdFormat(CMD, inputSlideShowVideoFile, inputAudioFile, outputFile);
            return new AudioToSlideShowCommand(cmd);
        }
    }
}
