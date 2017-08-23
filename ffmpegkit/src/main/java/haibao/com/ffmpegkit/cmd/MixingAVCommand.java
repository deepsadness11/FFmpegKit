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
public class MixingAVCommand extends BaseCommand {
    //注意，这里只能剪切 wma
//    private static final String CMD = "ffmpeg -y -i %s -i %s -filter_complex \"[0:a][1:a]amerge=inputs=2[a]\" -map 0:v -map \"[a]\" -c:v copy -c:a libvorbis -ac 2 -shortest %s";
//    private static final String CMD = "ffmpeg -y -i %s -i %s -filter_complex [0:a][1:a]amerge=inputs=2[a] -map 0:v -map [a] -c:v copy -c:a libvorbis -ac 2 -shortest %s";
    private static final String CMD = "ffmpeg -y -i %s -i %s -filter_complex [0:a][1:a]amerge=inputs=2[a] -map 0:v -map [a] -c:v copy -c:a mp2 -ac 2 -shortest %s";
    String target = "ffmpeg -y -i ping20s.mp4 -i audio0.mp3 -filter_complex \"[0:a][1:a]amerge=inputs=2[a]\" -map 0:v -map \"[a]\" -c:v copy -c:a libvorbis -ac 2 -shortest videoPlusAudio3.mp4";

    private MixingAVCommand(String command) {
        super(command);
    }

    public static class Builder implements IBuilder {

        String inputAudioFile;
        String inputVideoFile;

        String outputFile;

        public Builder setInputAudioFile(String inputAudioFile) {
            this.inputAudioFile = inputAudioFile;
            return this;
        }

        public Builder setInputVideoFile(String inputVideoFile) {
            this.inputVideoFile = inputVideoFile;
            return this;
        }

        public Builder setOutputFile(String outputFile) {
            this.outputFile = outputFile;
            return this;
        }

        @Override
        public Command build() {
            String cmd = cmdFormat(CMD, inputVideoFile, inputAudioFile, outputFile);
            return new MixingAVCommand(cmd);
        }
    }
}
