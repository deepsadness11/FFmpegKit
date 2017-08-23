package haibao.com.ffmpegkit.temp;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * haibao.com.course.common
 * <p>
 * ${CLASS_NAME}
 *
 * @author Administrator
 * @date 2017/4/11 0011.
 */

public interface CourseAwareCmd {
    int COURSE_LIVE_BEGIN = 0;  //课程直播开始
    int COURSE_LIVE_END = 1;    //课程直播结束
    int COURSEWARE_MOVE = 2;    //课件移动-->移动
    int COURSEWARE_PLAY = 3;    //课件开始播放--->开始播放
    int COURSEWARE_PAUSE = 4;   //课件播放暂停-->暂停在那个画面
    int COURSEWARE_VIDEO_END = 5;   //课件播放结束-->显示图片封面
    int COURSEWARE_UPLOAD_COMPLETE = 6; //课件上传完成
    int COURSE_LIVE_CRASH = 101;    //课件意外暂停


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({COURSE_LIVE_CRASH, COURSE_LIVE_BEGIN, COURSE_LIVE_END, COURSEWARE_MOVE, COURSEWARE_PLAY, COURSEWARE_PAUSE, COURSEWARE_VIDEO_END})
    @interface CMD_TYPE {

    }
}
