package haibao.com.ffmpegkit;

import android.content.Context;

/**
 * Created by Administrator on 2017/8/23 0023.
 */

public class FFmpegKit {
    /**
     * 需要的 context对象
     */
    private static Context mContext;

    /**
     * 当前app的版本号
     */
    private static String COMMON_DIR;

    private FFmpegKit() {
    }

    /**
     * 初始化 http
     *
     * @param context
     * @throws Exception
     */
    public static void initialize(Context context, String saveDir) {
        if (context == null) {
            throw new NullPointerException("Http初始化失败");
        } else {
            mContext = context;
            COMMON_DIR = saveDir;
        }
    }

    public static Context getContext() {
        return mContext;
    }

    public static String getCommonDir() {
        return COMMON_DIR;
    }

    public static void exit(){
        mContext = null;
        COMMON_DIR = null;
    }
}
