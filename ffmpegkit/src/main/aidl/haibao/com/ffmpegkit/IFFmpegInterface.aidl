// IFFmpegInterface.aidl
package haibao.com.ffmpegkit;
import haibao.com.ffmpegkit.ICallBackAidlInterface;
// Declare any non-default types here with import statements

interface IFFmpegInterface {
//    /**
//     * Demonstrates some basic types that you can use as parameters
//     * and return values in AIDL.
//     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);

    //只有一个开始的
    void startVideoProcessing();


    void registerCallBack(ICallBackAidlInterface callback);
    void unregisterCallBack();


}
