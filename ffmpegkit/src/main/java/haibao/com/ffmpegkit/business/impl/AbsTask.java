package haibao.com.ffmpegkit.business.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import haibao.com.ffmpegkit.KitExecutor;

/**
 * AbsTask
 * <p>
 * 任务的抽象类
 *
 * @author zzx
 * @time 2017/5/8
 */
public abstract class AbsTask implements Comparable<AbsTask>, Serializable {
    protected String TAG = this.getClass().getSimpleName();
    /*
        回调
         */
    protected TaskCompletedCallBack mCallBack;
    /**
     * Context
     */
    Context context;
    /**
     * 任务的等级。
     */
    @TaskC.LEVEL
    int level;
    /**
     * 是否正在运行
     */
    boolean isRunning;
    /**
     * 是否完成
     */
    boolean isCompleted;
    /**
     * 输入的地址
     */
    String outputPath;
    /**
     * 该任务的时长
     */
    double duration;
    /**
     * 开始的时间。从0 开始。针对于当个任务的相对时间
     */
    double startTime = 0;

    /**
     * 针对整个课件的真实的开始时间。用来合成和排序
     */
    double realStartTime;
    /**
     * 课件的类型
     */
    @TaskC.TYPE
    int type;

    /**
     * 是否是最后一个task. 通常用来标记最后一个 level1 的任务
     */
    boolean isLastTask;

    public AbsTask() {
    }

    public AbsTask(Context context, @TaskC.LEVEL int level, @TaskC.TYPE int type, TaskCompletedCallBack completedCallBack) {
        this.level = level;
        this.type = type;
        this.mCallBack = completedCallBack;
        this.context = context;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public AbsTask setRunning(boolean running) {
        isRunning = running;
        return this;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public AbsTask setCompleted(boolean completed) {
        isCompleted = completed;
        return this;
    }

    public
    @TaskC.TYPE
    int getType() {
        return type;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public double getDuration() {
        return duration;
    }

    protected void callbackError(Exception... exceptions) {
        Log.i(TAG, "callbackError");
        //如果运行出行错误
        for (int i = 0; i < exceptions.length; i++) {
            exceptions[i].printStackTrace();
        }
        if (mCallBack != null) {
            mCallBack.onTaskError(this);
        }
//        mCallBack = null;
    }


    public void execute() {

        Callable<Boolean> callable = buildCallable();
        if (callable == null) {
            callbackError();
            return;
        }
        isRunning = true;
        FutureTask<Boolean> task =
                KitExecutor.getInstance().runWorker(callable);
        try {
            if (task != null && task.get()) {   //执行完成
                isCompleted = true;
                isRunning = false;
                if (mCallBack != null) {
                    mCallBack.onTaskCompleted(this);
                }
            } else {
                callbackError();
            }
            //全部结束后，将回调移除
//            mCallBack=null;
        } catch (InterruptedException | ExecutionException ignore) {
            callbackError(ignore);
        }
    }

    protected abstract Callable<Boolean> buildCallable();

    protected abstract boolean checkInputParamsNotNull();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbsTask absTask = (AbsTask) o;

        if (level != absTask.level) return false;
        if (Double.compare(absTask.duration, duration) != 0) return false;
        if (startTime != absTask.startTime) return false;
        if (type != absTask.type) return false;
        return outputPath != null ? outputPath.equals(absTask.outputPath) : absTask.outputPath == null;

    }

    @Override
    public int hashCode() {
        int result = level;
        result = 31 * result + (outputPath != null ? outputPath.hashCode() : 0);
        result = (int) (31 * result + (duration != +0.0f ? Double.doubleToLongBits(duration) : 0));
        result = (int) (31 * result + startTime);
        result = 31 * result + type;
        return result;
    }

    //排序使用startTime进行排序
    @Override
    public int compareTo(@NonNull AbsTask task) {
        return (int) (this.realStartTime - task.realStartTime);
    }

    public interface TaskCompletedCallBack {
        void onTaskCompleted(AbsTask task);

        void onTaskError(AbsTask task);
    }
}
