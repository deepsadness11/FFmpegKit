#include <jni.h>
#include <dlfcn.h>

//extern "C" {

#include <libavcodec/avcodec.h>

#define LOG_TAG "FFmpegWrapper"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__)
#define LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)


JNIEXPORT jint JNICALL
Java_haibao_com_ffmpegkit_FFmpegJNIWrapper_ffmpegWrapper(JNIEnv *env, jclass type,
                                                         jstring nativeLibPath_,
                                                         jobjectArray argv) {
    //Get the second or 'inner' native library path
    char *nativePathPassedIn = (char *) (*env)->GetStringUTFChars(env, nativeLibPath_, 0);
    char ourNativeLibraryPath[256];
    //the name of your ffmpeg library
    snprintf(ourNativeLibraryPath, sizeof(ourNativeLibraryPath), "%s%s", nativePathPassedIn,
             "/libffmpeg_real.so");

    //Open the so library
    void *handle;
    typedef int (*func)(JNIEnv *, jobject, jobjectArray);
    handle = dlopen(ourNativeLibraryPath, RTLD_LAZY);
    if (handle == NULL) {
//        __android_log_print(ANDROID_LOG_VERBOSE, "ZX", "could not open library: %s", dlerror());
        printf("Could not dlopen(\"libffmpeg_real.so\"): %s\n", dlerror());
        return (-1);
    }

    //Call the ffmpeg wrapper functon in the second or 'inner' library
    func reenterable_ffmpegWrapperFunction;
    reenterable_ffmpegWrapperFunction = (func) dlsym(handle,
                                                     "Java_haibao_com_ffmpegkit_FFmpegJniReal_execute");
    reenterable_ffmpegWrapperFunction(env, type, argv); //the original arguments

    //Close the library
    dlclose(handle);

//     return
    return (1);
}

JNIEXPORT jint JNICALL
Java_haibao_com_ffmpegkit_FFmpegJNIWrapper_getProgress(JNIEnv *env, jclass type,
                                                       jstring nativeLibPath_) {
    //Get the second or 'inner' native library path
    char *nativePathPassedIn = (char *) (*env)->GetStringUTFChars(env, nativeLibPath_, 0);
    char ourNativeLibraryPath[256];
    //the name of your ffmpeg library
    snprintf(ourNativeLibraryPath, sizeof(ourNativeLibraryPath), "%s%s", nativePathPassedIn,
             "/libffmpeg_real.so");

    //Open the so library
    //JNIEnv *env, jobject instance
    void *handle;
    typedef int (*func)(JNIEnv *, jobject);
    handle = dlopen(ourNativeLibraryPath, RTLD_LAZY);
    if (handle == NULL) {
//        __android_log_print(ANDROID_LOG_VERBOSE, "ZX", "could not open library: %s", dlerror());
        printf("Could not dlopen(\"libffmpeg_real.so\"): %s\n", dlerror());
        return (-1);
    }

    //Call the ffmpeg wrapper functon in the second or 'inner' library
    func reenterable_ffmpegWrapperFunction;
    reenterable_ffmpegWrapperFunction = (func) dlsym(handle,
                                                     "Java_haibao_com_ffmpegkit_FFmpegJniReal_getProcess");
    int result = reenterable_ffmpegWrapperFunction(env, type); //the original arguments

    //Close the library
    dlclose(handle);
    return result;
}

JNIEXPORT void JNICALL
Java_haibao_com_ffmpegkit_FFmpegJNIWrapper_cancelTask__Ljava_lang_String_2(JNIEnv *env, jclass type,
                                                                           jstring nativeLibPath_) {
    //Get the second or 'inner' native library path
    char *nativePathPassedIn = (char *) (*env)->GetStringUTFChars(env, nativeLibPath_, 0);
    char ourNativeLibraryPath[256];
    //the name of your ffmpeg library
    snprintf(ourNativeLibraryPath, sizeof(ourNativeLibraryPath), "%s%s", nativePathPassedIn,
             "/libffmpeg_real.so");

    //Open the so library
    //JNIEnv *env, jobject instance
    void *handle;
    typedef int (*func)(JNIEnv *, jobject);
    handle = dlopen(ourNativeLibraryPath, RTLD_LAZY);
    if (handle == NULL) {
//        __android_log_print(ANDROID_LOG_VERBOSE, "ZX", "could not open library: %s", dlerror());
        printf("Could not dlopen(\"libffmpeg_real.so\"): %s\n", dlerror());
        return;
    }

    //Call the ffmpeg wrapper functon in the second or 'inner' library
    func reenterable_ffmpegWrapperFunction;
    reenterable_ffmpegWrapperFunction = (func) dlsym(handle,
                                                     "Java_haibao_com_ffmpegkit_FFmpegJniReal_cancelTask");
    reenterable_ffmpegWrapperFunction(env, type); //the original arguments

    //Close the library
    dlclose(handle);
}