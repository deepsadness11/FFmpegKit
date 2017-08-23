package haibao.com.ffmpegkit.business.impl;

/**
 * DESCRIPTION:
 * Author: Cry
 * DATE: 17/6/15 下午10:21
 */

public interface ProgressCallBack {
    //最后完成
    void onFinalTaskCompleted(String outputPath);

    //第一级人物的回调
    void onSingleSlideTaskCompleted(int totalSize);

    //第二级人物的回调
    void onSlideConcatTaskCompleted(int totalSize);

    void onCutVideoTaskCompleted(int totalSize);

    //第三级人物的回调
    void onCutAudioTaskCompleted(int count, int size);

    void onAddAudioTaskCompleted(int count, int size);

    //合成出错
    void onFinalTaskError(String outputPath);
}
