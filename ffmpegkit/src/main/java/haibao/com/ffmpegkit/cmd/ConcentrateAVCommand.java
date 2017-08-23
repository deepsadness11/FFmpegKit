package haibao.com.ffmpegkit.cmd;


import java.util.ArrayList;

import haibao.com.ffmpegkit.commands.BaseCommand;
import haibao.com.ffmpegkit.commands.Command;

import static haibao.com.ffmpegkit.utils.TextUtils.cmdFormat;

/**
 * ConcentrateAVCommand
 * <p>
 * 视频进行拼接的Command
 *
 * @author zzx
 * @time 2017/4/19 0019
 */
public class ConcentrateAVCommand extends BaseCommand {
    //注意，这里只能剪切 wma
    private static final String CMD = "ffmpeg -y -i \"concat:%s\" -c copy %s";
    private static final String CMD2 = "ffmpeg -y -safe 0 -f concat -i %s -vcodec libx264 -b:v 1000K -b:a 64K -pix_fmt yuv420p %s";
    private static final String CMD3 = "ffmpeg -y -safe 0 -f concat -i %s %s";
    String target = "ffmpeg -i \"concat:out2.mp4|out3.mp4\" -c copy output-final.mp4";

    private ConcentrateAVCommand(String command) {
        super(command);
    }

    public static class Builder implements IBuilder {

        public static final int INPUT_FILE = 1;
        public static final int INPUT_MULTI_IMAGE = 2;
        public static final int INPUT_AUDIO = 3;

        /**
         * 生成模式。
         * 1---> 1张图片 1个音频生成
         * 2---> 多张图片 0个音频生成
         * 3---> 多个音频的合成
         */
        int generateMode;

        ArrayList<String> inputVideoFiles;

        String outputFile;
        String inputDefinedFile;

        public Builder setGenerateMode(int generateMode) {
            this.generateMode = generateMode;
            return this;
        }

        public Builder setInputVideoFiles(ArrayList<String> inputVideoFiles) {
            this.inputVideoFiles = inputVideoFiles;
            return this;
        }

        public Builder setOutputFile(String outputFile) {
            this.outputFile = outputFile;
            return this;
        }

        public Builder setInputDefinedFile(String inputDefinedFile) {
            this.inputDefinedFile = inputDefinedFile;
            return this;
        }

        @Override
        public Command build() {
            StringBuilder cmdsb = new StringBuilder();
            String cmd = "";
            if (generateMode == INPUT_FILE) {
                cmd = cmdFormat(CMD2, inputDefinedFile, outputFile);
            } else if (generateMode == INPUT_AUDIO) {
                cmd = cmdFormat(CMD3, inputDefinedFile, outputFile);
            } else {
                if (inputVideoFiles.size() < 2) {
                    throw new RuntimeException("inputVideoFiles size must be more than 1");
                }
                for (int i = 0; i < inputVideoFiles.size(); i++) {

                    if (i != 0) {
                        cmdsb.append("|");
                    }
                    cmdsb.append(inputVideoFiles.get(i));
                }
                cmd = cmdFormat(CMD, cmdsb.toString(), outputFile);
            }


            return new ConcentrateAVCommand(cmd);
        }
    }
}
