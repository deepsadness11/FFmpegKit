package haibao.com.ffmpegkit.business.impl;

/**
 * DESCRIPTION:
 * Author: Cry
 * DATE: 17/6/15 下午10:21
 */

public interface OnScaleVideoListener {
    //第三级人物的回调
    void onScaleTaskCompleted(String output);

    void onScaleTaskError();
}
