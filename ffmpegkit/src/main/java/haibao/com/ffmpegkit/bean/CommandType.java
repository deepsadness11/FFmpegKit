package haibao.com.ffmpegkit.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * haibao.com.ffmpegkit.bean
 * <p>
 * ${CLASS_NAME}
 *
 * @author Administrator
 * @date 2017/5/5.
 */

public class CommandType implements Parcelable,Serializable{

    public static final Creator<CommandType> CREATOR = new Creator<CommandType>() {
        @Override
        public CommandType createFromParcel(Parcel in) {
            return new CommandType(in);
        }

        @Override
        public CommandType[] newArray(int size) {
            return new CommandType[size];
        }
    };
    /**
     * cmd_type : course_live_begin
     * courseware_id : 1
     * time : 1490062890
     * video_time : 0
     */

    public int cmd_type;
    public int courseware_id;
    public double time;
    public double video_time;
    public String inputPath;

    //添加课件的id
    public String course_id;

    public CommandType() {
    }

    public CommandType(String course_id, int cmd_type, int courseware_id, double time, double video_time, String inputPath) {
        this.course_id=course_id;
        this.cmd_type = cmd_type;
        this.courseware_id = courseware_id;
        this.time = time;
        this.video_time = video_time;
        this.inputPath = inputPath;
    }

    protected CommandType(Parcel in) {
        cmd_type = in.readInt();
        courseware_id = in.readInt();
        time = in.readInt();
        video_time = in.readInt();
        this.inputPath = in.readString();
        course_id=in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(cmd_type);
        dest.writeInt(courseware_id);
        dest.writeDouble(time);
        dest.writeDouble(video_time);
        dest.writeString(inputPath);
        dest.writeString(course_id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommandType{");
        sb.append("cmd_type='").append(cmd_type).append('\'');
        sb.append(", courseware_id=").append(courseware_id);
        sb.append(", time=").append(time);
        sb.append(", video_time=").append(video_time);
        sb.append(", inputPath=").append(inputPath);
        sb.append(", course_id=").append(course_id);
        sb.append('}');
        return sb.toString();
    }
}
