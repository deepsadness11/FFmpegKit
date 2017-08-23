package haibao.com.ffmpegkit.cmd;


import haibao.com.ffmpegkit.commands.BaseCommand;
import haibao.com.ffmpegkit.commands.Command;

import static haibao.com.ffmpegkit.utils.TextUtils.cmdFormat;

/**
 * ScaleCommand
 * <p>
 * 对视频进行缩放的Command
 *
 * @author zzx
 * @time 2017/4/19 0019
 */
public class ScaleCommand extends BaseCommand {
    private static final String CMD = "ffmpeg -y -i %s -vf %s  -acodec aac -vcodec h264 -b:v 1000K -b:a 64K %s";
    private static final String CMD2 = "ffmpeg -y -i %s -vf %s  -acodec libfaac -vcodec h264 -b:v 1000K -b:a 64K %s";
    String target = "ffmpeg -i ping20s.mp4 -vf scale=800:600,transpose=2 -acodec aac -vcodec h264 -b:v 1000K -b:a 64K scale7.mp4";

    private ScaleCommand(String command) {
        super(command);
    }

    public static class Builder implements BaseCommand.IBuilder {
        public static final int V43 = 0;
        public static final int V169 = 1;
        /**
         * 是否需要旋转 旋转的角度
         */
        int transposeAngle;
        /**
         * 4:3  or 16:9
         * 800:600 950535-->修改为 950 540
         */
        int scaleType;

        String inputFile;

        String outputFile;


        public Builder setTransposeAngle(int transposeAngle) {
            this.transposeAngle = transposeAngle;
            return this;
        }

        public Builder setScaleType(int scaleType) {
            this.scaleType = scaleType;
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
            StringBuilder scaleFormat = new StringBuilder();
            //暂时只是缩放，而不是保持原来的比例，进行剪切
            scaleFormat.append(scaleType == V43 ? "scale=800:600" : "scale=800:600");

            if (transposeAngle == 90) {
                scaleFormat.append(",")
                        .append("transpose=1");

            } else if (transposeAngle == 180) {
                scaleFormat.append(",")
                        .append("transpose=2");
            }
            //去除旋转的信息
            if (transposeAngle != 0) {
                scaleFormat.append(" ").append("-metadata:s:v rotate=\"\"");
            }

            String cmd = cmdFormat(CMD, inputFile, scaleFormat.toString(), outputFile);
            return new ScaleCommand(cmd);
        }
    }

}
