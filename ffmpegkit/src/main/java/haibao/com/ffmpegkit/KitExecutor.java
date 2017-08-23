package haibao.com.ffmpegkit;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * haibao.com.ffmpegkit
 * <p>
 * ${CLASS_NAME}
 *
 * @author Administrator
 * @date 2017/5/8.
 */

public class KitExecutor {

    private static final KitExecutor INSTANCE = new KitExecutor();
    String TAG = this.getClass().getSimpleName();
    private ExecutorService mExecutorService;

    private KitExecutor() {
    }

    public static KitExecutor getInstance() {
        return INSTANCE;
    }

    public void runWorker(@NonNull Runnable runnable) {
        ensureWorkerHandlerNotNull();
        try {
            mExecutorService.execute(runnable);
        } catch (Exception e) {
            Log.d(TAG, "runnable stop running unexpected. " + e.getMessage());
        }
    }

    @Nullable
    public FutureTask<Boolean> runWorker(@NonNull Callable<Boolean> callable) {
        ensureWorkerHandlerNotNull();
        FutureTask<Boolean> task = null;
        try {
            task = new FutureTask<>(callable);
            mExecutorService.submit(task);
            return task;
        } catch (Exception e) {
            Log.d(TAG, "callable stop running unexpected. " + e.getMessage());
        }
        return task;
    }

    public void runUI(@NonNull Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
            return;
        }
        Handler handler = ensureUiHandlerNotNull();
        try {
            handler.post(runnable);
        } catch (Exception e) {
            Log.d(TAG, "update UI task fail. " + e.getMessage());
        }
    }

    private void ensureWorkerHandlerNotNull() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newCachedThreadPool();
        }
    }

    private Handler ensureUiHandlerNotNull() {
        return new Handler(Looper.getMainLooper());
    }

}