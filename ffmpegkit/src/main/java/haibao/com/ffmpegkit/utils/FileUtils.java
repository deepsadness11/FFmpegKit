package haibao.com.ffmpegkit.utils;

import java.io.File;

/**
 * Created by Administrator on 2017/8/23 0023.
 */

public class FileUtils {
    public static boolean isFilexists(String filePath) {
        return new File(filePath).exists();
    }
}
