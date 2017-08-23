package haibao.com.ffmpegkit;

import android.content.Context;

/**
 * FFmpegJNIWrapper
 * <p>
 * FFmpeg在Android上没办法两次运行。
 * 因为第二次的有些静态变量已经被初始化。
 * 现在找到的方法是
 * 每次都需要将library进行加载和卸载。Java层不知道如果做到，故用native的方法来完成
 *
 * @author zzx
 * @time 2017/4/19 0019
 */
public class FFmpegJNIWrapper {

    //This class provides a Java wrapper around the exposed JNI ffmpeg functions.

    static {
        //Load the 'first' or 'outer' JNI library so this activity can use it
        System.loadLibrary("ffmpeg_wrapper");
    }

    public static int call_ffmpegWrapper(Context appContext, String[] ffmpegArgs) {
        //Get the native libary path
        String nativeLibPath = appContext.getApplicationInfo().nativeLibraryDir;

        //Call the method in the first or 'outer' library, passing it the
        //native library past as well as the original args
        return ffmpegWrapper(nativeLibPath, ffmpegArgs);
    }

    public static int call_progress(Context appContext) {
        //Get the native libary path
        String nativeLibPath = appContext.getApplicationInfo().nativeLibraryDir;

        //Call the method in the first or 'outer' library, passing it the
        //native library past as well as the original args
        return getProgress(nativeLibPath);
    }

    public static void cancelTask(Context appContext) {
        //Get the native libary path
        String nativeLibPath = appContext.getApplicationInfo().nativeLibraryDir;

        //Call the method in the first or 'outer' library, passing it the
        //native library past as well as the original args
        cancelTask(nativeLibPath);
    }


    // Native methods for ffmpeg functions
    private static native int ffmpegWrapper(String nativeLibPath, String[] argv);

    private static native int getProgress(String nativeLibPath);

    private static native void cancelTask(String nativeLibPath);

}