package haibao.com.ffmpegkit;


import android.text.TextUtils;

import haibao.com.ffmpegkit.commands.Command;

/**
 * FFmpegJniReal
 * <p>
 * 如果APp过程中只调用一次FFmpeg，则可以调用这里的方法
 *
 * @author zzx
 * @time 2017/4/19 0019
 */
public class FFmpegJniReal {

    private static FFmpegJniReal sInstance;

    static {
        System.loadLibrary("ffmpeg_real");
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avdevice-57");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avformat-57");
        System.loadLibrary("avutil-55");
        System.loadLibrary("postproc-54");
        System.loadLibrary("swresample-2");
        System.loadLibrary("swscale-4");

    }

    private FFmpegJniReal() {
    }

    public static FFmpegJniReal getInstance() {
        if (sInstance == null) sInstance = new FFmpegJniReal();
        return sInstance;
    }

    public void execute(Command command) {
        if (command == null || TextUtils.isEmpty(command.getCommand())) return;

        String[] commands = command.getCommand().split("\\s");
        execute(commands);
    }

    public void execute(String nativeLibPath, String cmd) {

        String[] commands = cmd.split(" ");
        execute(commands);
    }

    private native int execute(String[] commonds);

    public native String avFormatInfo();

    public native String urlProtocolInfo();

    public native String avCodecInfo();

    public native String avFilterInfo();

    //    public native void setCallBackToNative(JniListener listener);
    public native int getProcess();

    public native void cancelTask();

}
