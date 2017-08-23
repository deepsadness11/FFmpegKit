package haibao.com.ffmpegkit.cmd;


import haibao.com.ffmpegkit.commands.BaseCommand;
import haibao.com.ffmpegkit.commands.Command;

import static haibao.com.ffmpegkit.utils.TextUtils.cmdFormat;

/**
 * AudioConvertCommand
 * <p>
 * 音频进行剪切的Command
 *
 * @author zzx
 * @time 2017/4/19 0019
 */
public class AudioConvertCommand extends BaseCommand {
    //注意，这里只能剪切 wma
    private static final String CMD = "ffmpeg -y -i %s -acodec aac -b:a 64K %s";
    private static final String CMD2 = "ffmpeg -y -ss %s -t %s -i %s -acodec libfaac -b:a 64K  -pix_fmt yuv420p %s";

    private AudioConvertCommand(String command) {
        super(command);
    }

    public static class Builder implements IBuilder {


        String inputFile;

        String outputFile;


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
            String cmd = cmdFormat(CMD, inputFile, outputFile);
            return new AudioConvertCommand(cmd);
        }
    }
}
