package haibao.com.ffmpegkit.business.impl;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * haibao.com.ffmpegkit.common
 * <p>
 * ${CLASS_NAME}
 *
 * @author Administrator
 * @date 2017/5/8.
 */
public interface TaskC {

    int SLIDE_SHOW = 0;  //幻灯片，不带声音
    int CUT_VIDEO = 1;  //剪切的视频，不带混音
    int CUT_AUDIO = 2;  //剪切的视频，不带混音
    int C_SLIDE = 3;  //合并的幻灯片，不带声音
    int C_SLIDE_AUDIO = 4;  //合并幻灯片，带声音
    int C_CUT_VIDEO = 5;  //合并幻灯片，带声音
    int F_SLIDE_VIDEO = 6;  //最后合成的结果
    int SCALE_VIDEO = 7;
    int CONCAT_AUDIO = 8;   //合并音频
    int SCALE_IMG = 8;   //合并音频

    int SINGLE_LEVEL = 0;  //只有单个SLideShow 是这个level
    int TO_AUDIO_LEVEL = 1;  //准备进行混音的level 包括合成后的SLideShow 和 剪切后的视频
    int AUDIO_LEVEL = 2;  //合成视频后的level
    int OUT_LEVEL = 3;  //最后合成的结果
    //存储的路径
    //必须在这里写死，不知道为什么会遇到这个坑
    String savePath = "/ayb/course/";
    String saveCoursePath = "/ayb/course/img";


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SLIDE_SHOW, CUT_VIDEO, C_SLIDE, C_SLIDE_AUDIO, C_CUT_VIDEO, F_SLIDE_VIDEO, CUT_AUDIO, SCALE_VIDEO, CONCAT_AUDIO, SCALE_IMG})
    @interface TYPE {

    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SINGLE_LEVEL, TO_AUDIO_LEVEL, AUDIO_LEVEL, OUT_LEVEL})
    @interface LEVEL {

    }
}
