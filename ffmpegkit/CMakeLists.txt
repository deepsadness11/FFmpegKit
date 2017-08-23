cmake_minimum_required(VERSION 3.4.1)

# 设置变量，方便底下使用
set(INC_DIR ${PROJECT_SOURCE_DIR}/libs/include)
set(LINK_DIR ${PROJECT_SOURCE_DIR}/libs/${ANDROID_ABI})


# 添加so库对应的头文件目录
include_directories(${INC_DIR})

# 引入so库,IMPORT代表从第三方引入的意思
add_library( avcodec-57 SHARED IMPORTED)
# 设置编译的库文件存放的目录
set_target_properties( avcodec-57 PROPERTIES IMPORTED_LOCATION ${LINK_DIR}/libavcodec-57.so)

add_library( avfilter-6 SHARED IMPORTED)
set_target_properties( avfilter-6 PROPERTIES IMPORTED_LOCATION ${LINK_DIR}/libavfilter-6.so)

add_library( avformat-57 SHARED IMPORTED)
set_target_properties( avformat-57 PROPERTIES IMPORTED_LOCATION ${LINK_DIR}/libavformat-57.so)

add_library( avutil-55 SHARED IMPORTED)
set_target_properties( avutil-55 PROPERTIES IMPORTED_LOCATION ${LINK_DIR}/libavutil-55.so)

add_library( swresample-2 SHARED IMPORTED)
set_target_properties( swresample-2 PROPERTIES IMPORTED_LOCATION ${LINK_DIR}/libswresample-2.so)

add_library( swscale-4 SHARED IMPORTED)
set_target_properties( swscale-4 PROPERTIES IMPORTED_LOCATION ${LINK_DIR}/libswscale-4.so)


# 自己本地的代码所编译的库
add_library( # Sets the name of the library.
             ffmpeg_real

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/ffmpeg_box.c src/main/cpp/cmdutils.c src/main/cpp/ffmpeg.c src/main/cpp/ffmpeg_filter.c src/main/cpp/ffmpeg_opt.c
             )

add_library( # Sets the name of the library.
             ffmpeg_wrapper

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/ffmpeg_wrapper.c
             )


find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# 将其他库文件链接到native-lib上
target_link_libraries(
                       ffmpeg_real
                       ffmpeg_wrapper
                        avcodec-57
                        avfilter-6
                        avformat-57
                        avutil-55
                        swresample-2
                        swscale-4
                       ${log-lib})