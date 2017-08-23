package haibao.com.ffmpegkit.business.impl;

import android.os.Environment;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import haibao.com.ffmpegkit.FFmpegKit;
import haibao.com.ffmpegkit.bean.CommandType;
import haibao.com.ffmpegkit.utils.SharedPreferencesUtils;



/**
 * DESCRIPTION: 命令分发的缓存帮助类.将发送过来的命令缓存起来。需要保存到固定文件夹中
 * Author: Cry
 * DATE: 17/6/16 上午1:08
 */
public class CacheHelper {

    /**
     * 设置最后输入的音频地址。
     *
     * @param course_id
     * @param audio
     */
    public static void setInputAudioPathToSharePref(String course_id, String audio) {
        Gson gson = new Gson();
        String course_input_audio = SharedPreferencesUtils.getStringValue(FFmpegKit.getContext(),"COURSE_INPUT_AUDIO");
        HashMap<String, String> hashMap = null;
        try {
            hashMap = gson.fromJson(course_input_audio, HashMap.class);
        } catch (Exception e) {

        }
        if (hashMap == null) {
            hashMap = new HashMap<>();
        }
        hashMap.put(course_id, audio);
        String s = gson.toJson(hashMap, HashMap.class);
        SharedPreferencesUtils.setString(FFmpegKit.getContext(),"COURSE_INPUT_AUDIO", s);
        //将其保存到文件中
        File file = new File(Environment.getExternalStorageDirectory(), FFmpegKit.getCommonDir() + "/couserAudioInput.txt");

        PrintWriter printWriter = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            printWriter = new PrintWriter(file);
            printWriter.printf(s);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getInputAudioPathToSharePref(String course_id) {
        Gson gson = new Gson();
        String course_input_audio = SharedPreferencesUtils.getStringValue(FFmpegKit.getContext(),"COURSE_INPUT_AUDIO");
        HashMap<String, String> hashMap;
        try {
            hashMap = gson.fromJson(course_input_audio, HashMap.class);
        } catch (Exception e) {
            return null;
        }
        return hashMap.get(course_id);
    }

    /**
     * 将当前的视频地址缓存起来
     *
     * @param course_id
     * @param audio_list
     */
    public static void setInputAudioPathsToSharePref(String course_id, ArrayList<String> audio_list) {
        Gson gson = new Gson();
        String course_input_audio = SharedPreferencesUtils.getStringValue(FFmpegKit.getContext(),"COURSE_INPUT_AUDIO_PATHS");
        HashMap<String, String> hashMap = null;
        try {
            hashMap = gson.fromJson(course_input_audio, HashMap.class);
        } catch (Exception e) {

        }
        if (hashMap == null) {
            hashMap = new HashMap<>();
        }
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        String value = gson.toJson(audio_list, type);
        hashMap.put(course_id, value);
        String resultValue = gson.toJson(hashMap, HashMap.class);
        SharedPreferencesUtils.setString(FFmpegKit.getContext(),"COURSE_INPUT_AUDIO_PATHS", resultValue);

        File file = new File(Environment.getExternalStorageDirectory(), FFmpegKit.getCommonDir() + "/couserAudiosInput.txt");

        PrintWriter printWriter = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            printWriter = new PrintWriter(file);
            printWriter.printf(resultValue);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将当前的视频地址缓存起来
     *
     * @param course_id
     */
    public static ArrayList<String> getInputAudioPathsToSharePref(String course_id) {
        Gson gson = new Gson();
        ArrayList<String> result = null;
        String course_input_audio = SharedPreferencesUtils.getStringValue(FFmpegKit.getContext(),"COURSE_INPUT_AUDIO_PATHS");
        HashMap<String, String> hashMap = null;
        try {
            hashMap = gson.fromJson(course_input_audio, HashMap.class);
        } catch (Exception e) {
            return result;
        }
        if (hashMap == null) {
            hashMap = new HashMap<>();
        }
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        String value = hashMap.get(course_id);
        if (TextUtils.isEmpty(value)) {
            return result;
        } else {
            try {
                result = gson.fromJson(value, type);
            } catch (Exception e) {
                return result;
            }
            return result;
        }
    }

    /**
     * 将当前的命令缓存起来
     *
     * @param lastCourseId
     * @param commandTypes
     * @param command
     */
    public static void setToSharePref(String lastCourseId, ArrayList<CommandType> commandTypes, CommandType command) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<CommandType>>() {
        }.getType();
        HashMap<String, String> command_list = SharedPreferencesUtils.getObject(FFmpegKit.getContext(),"Command_list", HashMap.class);
        commandTypes.add(command);
        String s1 = gson.toJson(commandTypes, type);
        if (command_list == null) {
            command_list = new HashMap<>();
        }
        command_list.put(lastCourseId, s1);
        SharedPreferencesUtils.setObject(FFmpegKit.getContext(),"Command_list", command_list);


        File file = new File(Environment.getExternalStorageDirectory(), FFmpegKit.getCommonDir() + "/command_list.txt");
        String resultValue = gson.toJson(command_list, HashMap.class);
        PrintWriter printWriter = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            printWriter = new PrintWriter(file);
            printWriter.printf(resultValue);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过course_id来获取所有的命令。
     *
     * @param course_id
     * @return
     */
    public static ArrayList<CommandType> getFromSharePref(String course_id) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<CommandType>>() {
        }.getType();
        HashMap<String, String> command_list = SharedPreferencesUtils.getObject(FFmpegKit.getContext(),"Command_list", HashMap.class);
        if (command_list == null) {
            command_list = new HashMap<>();
        }
        String s = command_list.get(course_id);
        ArrayList<CommandType> getFromList = null;

        try {
            getFromList = gson.fromJson(s, type);
        } catch (Exception e) {
            //解析失败。则说明并不存在。

        }
        if (getFromList == null) {
            getFromList = new ArrayList<>();
            //再设置回去。
            String s1 = gson.toJson(getFromList, type);
            command_list.put(course_id, s1);
            SharedPreferencesUtils.setObject(FFmpegKit.getContext(),"Command_list", command_list);
        }
        return getFromList;
    }

}
