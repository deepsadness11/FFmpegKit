package haibao.com.ffmpegkit.commands;


import static haibao.com.ffmpegkit.utils.TextUtils.cmdFormat;

/**
 * ScreenshotCommand
 * <p>
 * Screen shot
 *
 * @author Administrator
 * @time 2017/4/19 0019
 */
public class ScreenshotCommand extends BaseCommand {

    private final static String CMD = "ffmpeg -y -ss %d -i %s -vframes 1 -f mjpeg %s";

    private ScreenshotCommand(String command) {
        super(command);
    }

    public static class Builder implements IBuilder {

        String videoFile;

        String imageFile;

        int time;

        public Builder setVideoFile(String videoFile) {
            this.videoFile = videoFile;
            return this;
        }

        public Builder setImageFile(String imageFile) {
            this.imageFile = imageFile;
            return this;
        }

        public Builder setTime(int time) {
            this.time = time;
            return this;
        }

        @Override
        public Command build() {
            String cmd = cmdFormat(CMD, time, videoFile, imageFile);
            return new ScreenshotCommand(cmd);
        }
    }
}
