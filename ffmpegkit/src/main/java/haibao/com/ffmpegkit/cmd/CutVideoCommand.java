package haibao.com.ffmpegkit.cmd;


import haibao.com.ffmpegkit.commands.BaseCommand;
import haibao.com.ffmpegkit.commands.Command;

import static haibao.com.ffmpegkit.utils.TextUtils.cmdFormat;

/**
 * CutAudioCommand
 * <p>
 * 对音频进行剪切的Command
 *
 * @author zzx
 * @time 2017/4/19 0019
 */
public class CutVideoCommand extends BaseCommand {
    //注意，这里只能剪切 wma
    private static final String CMD = "ffmpeg -y -ss %s -t %s -i %s -c:v copy %s";
    private static final String CMD2 = "ffmpeg -y -ss %s -t %s -i %s -acodec libfaac -b:a 64K -pix_fmt yuv420p %s";

    private CutVideoCommand(String command) {
        super(command);
    }

    public static class Builder implements IBuilder {

        String startTime;

        String duration;

        String inputFile;

        String outputFile;

        public Builder setStartTime(String startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder setDuration(String duration) {
            this.duration = duration;
            return this;
        }

        public Builder setInputFile(String inputFile) {
            this.inputFile = inputFile;
            return this;
        }

        public Builder setOutputFile(String outputFile) {
            this.outputFile = outputFile;
            return this;
        }

        @Override
        public Command build() {
            String cmd = cmdFormat(CMD, startTime, duration, inputFile, outputFile);
            return new CutVideoCommand(cmd);
        }
    }
}
