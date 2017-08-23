package haibao.com.ffmpegkit.business;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static haibao.com.ffmpegkit.business.impl.TaskC.saveCoursePath;
import static haibao.com.ffmpegkit.business.impl.TaskC.savePath;

/**
 * haibao.com.ffmpegkit.business.file
 * <p>
 * ${CLASS_NAME}
 *
 * @author Administrator
 * @date 2017/5/8.
 */

public class FileGeneratorHelper {
    private static FileGeneratorHelper INSTANCE;
    private String TAG = this.getClass().getSimpleName();
    private File externalStorageDirectory = Environment.getExternalStorageDirectory();

    private FileGeneratorHelper() {
    }

    public static FileGeneratorHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FileGeneratorHelper();
        }
        return INSTANCE;
    }

    public void release() {
        INSTANCE = null;
    }

    public String getSlideShowOutputFilePath(double realStartTime) {
        File file2 = new File(externalStorageDirectory,
                savePath
                        + realStartTime
                        + "sSlide.mp4");
        return file2.getAbsolutePath();
    }

    public String getScaleImageOutputFilePath() {
        File file2 = new File(externalStorageDirectory,
                saveCoursePath
                        + System.currentTimeMillis()
                        + ".jpg");
        return file2.getAbsolutePath();
    }

    public String getConcatOutputFilePath(double realStartTime) {
        File file2 = new File(externalStorageDirectory,
                savePath
                        + realStartTime
                        + "concat.mp4");
        return file2.getAbsolutePath();
    }

    public String getConcatOutputAudioFilePath(double realStartTime) {
        File file2 = new File(externalStorageDirectory,
                savePath
                        + realStartTime
                        + "concat.wav");
        return file2.getAbsolutePath();
    }

    public String getConcatOutputFilePath() {
        File file2 = new File(externalStorageDirectory,
                savePath
                        + System.currentTimeMillis()
                        + "concat.wav");
        return file2.getAbsolutePath();
    }

    //得到临时的
    public String generateTempAudioFilePathByRealStartTime(double realStartTime) {
        File file2 = new File(externalStorageDirectory,
                savePath
                        + realStartTime
                        + "cAudio.wma");
        return file2.getAbsolutePath();
    }

    public String getConcatInputFilePath(String definedFileContentPath, int index) {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File dirsfile = new File(externalStorageDirectory, "ayb/course/temp");
        if (dirsfile.exists()) {
            dirsfile.mkdirs();
        }
        File file = new File(dirsfile, +index + ".txt");
        boolean newFile = false;
        try {
            newFile = file.createNewFile();
            if (newFile) {
                PrintWriter printWriter = new PrintWriter(file);
                printWriter.print(definedFileContentPath);
                printWriter.close();
            }
            Log.i(TAG, "写入文件成功！！");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "写入文件失败！！");
            return null;
        }
        return file.getAbsolutePath();
    }

    public String getConcatAudioInputFilePath(String definedFileContentPath, int index) {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File dirsfile = new File(externalStorageDirectory, "ayb/course/temp");
        if (dirsfile.exists()) {
            dirsfile.mkdirs();
        }
        File file = new File(dirsfile, +index + ".txt");
        boolean newFile = false;
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            newFile = file.createNewFile();
            if (newFile) {
                PrintWriter printWriter = new PrintWriter(file);
                printWriter.print(definedFileContentPath);
                printWriter.close();
            }
            Log.i(TAG, "写入文件成功！！");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "写入文件失败！！");
            return null;
        }
        return file.getAbsolutePath();
    }

    public String getAddAudioFile(double realStartTime) {
        File file2 = new File(externalStorageDirectory,
                savePath
                        + realStartTime
                        + "videoWithAudio.mp4");
        return file2.getAbsolutePath();
    }

    public String getVideoCutOutputFilePath(double realStartTime) {
        File file2 = new File(externalStorageDirectory,
                savePath
                        + realStartTime
                        + "videoCut.mp4");
        return file2.getAbsolutePath();
    }

    public String getScaleVideoOutFilePath(String scaleVideoPath) {

        if (TextUtils.isEmpty(scaleVideoPath)) {
            return null;
        }
        long time = System.currentTimeMillis() / 1000;
        String word = generateWord(8);
        File file2 = new File(externalStorageDirectory,
                savePath
                        + time
                        + word
                        + ".mp4");
        return file2.getAbsolutePath();
    }


    /**
     * 输出的文件要满足文件名的规律
     * time().(8位随机字符串，数字和字符串，区分大小写).mp4， date(‘Ym’)为年月，time()为时间戳。
     * 14453170751 265481dG.mp4
     *
     * @param targetLength 需要的随机长度
     * @return
     */
    private String generateWord(int targetLength) {
        String[] beforeShuffle = new String[]{"0", "1", "2", "3", "4", "5", "6", "7",
                "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
                "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
                "w", "x", "y", "z"};
        List<String> originList = Arrays.asList(beforeShuffle);

        Collections.shuffle(originList);
        StringBuilder sb = new StringBuilder();

        int size = originList.size();
        if (targetLength < size) {
            for (int i = 0; i < targetLength; i++) {
                sb.append(originList.get(i));
            }
        }

        return sb.toString();
    }
}
