package haibao.com.ffmpegkit.commands;


import haibao.com.ffmpegkit.utils.Constant;

import static haibao.com.ffmpegkit.utils.TextUtils.cmdFormat;

/**
 * FormatConvertCommand
 * <p>
 * format convert
 *
 * @author Administrator
 * @time 2017/4/19 0019
 */
public class FormatConvertCommand extends BaseCommand {

    private static final String CMD = "ffmpeg -y -i %s -c copy -f %s %s";

    private FormatConvertCommand(String command) {
        super(command);
    }

    public static class Builder implements IBuilder {
        String inputFile;

        String outputFile;

        Constant.Format format;

        public Builder setInputFile(String inputFile) {
            this.inputFile = inputFile;
            return this;
        }

        public Builder setOutputFile(String outputFile) {
            this.outputFile = outputFile;
            return this;
        }

        public Builder setFormat(Constant.Format format) {
            this.format = format;
            return this;
        }

        @Override
        public Command build() {
            String cmd = cmdFormat(CMD, inputFile, format.getName(), outputFile);
            return new FormatConvertCommand(cmd);
        }
    }

}
