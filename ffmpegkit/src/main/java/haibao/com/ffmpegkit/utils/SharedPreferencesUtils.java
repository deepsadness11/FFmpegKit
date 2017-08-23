package haibao.com.ffmpegkit.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Created by Administrator on 2017/8/23 0023.
 */

public class SharedPreferencesUtils {
    //  是否需要将数据保存到数据库
    public static final String is_Save_CourseMap = "is_Save_CourseMap";

    public static final String NAME_SP = "haibao";

    public static SharedPreferences share;

    public static SharedPreferences getSharedPref(Context context) {
        share = context.getSharedPreferences(NAME_SP, 0);
        return share;
    }

    public static String getStringValue(Context context, String key) {
        if (share == null) {
            getSharedPref(context);
        }
        return share.getString(key, "");
    }

    public static void setString(Context context, String key, String value) {
        if (share == null) {
            getSharedPref(context);
        }
        share.edit().putString(key, value).apply();
    }

    public static Set<String> getStringSet(Context context, String key) {
        if (share == null) {
            getSharedPref(context);
        }
        return share.getStringSet(key, null);
    }

    public static void setStringSet(Context context, String key, Set<String> value) {
        if (share == null) {
            getSharedPref(context);
        }
        share.edit().putStringSet(key, value).apply();
    }

    public static void setInt(Context context, String key, int value) {
        if (share == null) {
            getSharedPref(context);
        }
        share.edit().putInt(key, value).apply();
    }

    public static int getIntValue(Context context, String key) {
        return getIntValue(context, key, 0);
    }

    public static int getIntValue(Context context, String key, int defValue) {
        if (share == null) {
            getSharedPref(context);
        }
        return share.getInt(key, defValue);
    }

    public static void setLong(Context context, String key, long value) {
        if (share == null) {
            getSharedPref(context);
        }
        share.edit().putLong(key, value).apply();
    }

    public static long getLongValue(Context context,String key) {
        return getLongValue(context,key, 0L);
    }

    public static long getLongValue(Context context, String key, Long defValue) {
        if (share == null) {
            getSharedPref(context);
        }
        return share.getLong(key, defValue);
    }

    private static Gson gson = new Gson();

    public static void setObject(Context context,String key, Object object) {
        setString(context,key, gson.toJson(object));
    }

    public static <T> T getObject(Context context,String key, Class<T> typeof) {
        String json = getStringValue(context,key);
        if (!android.text.TextUtils.isEmpty(json)) {
            try {
                return gson.fromJson(json, typeof);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static boolean getBooleanValue(Context context,String key, boolean defaultBool) {
        if (share == null) {
            getSharedPref(context);
        }
        return share.getBoolean(key, defaultBool);
    }

    public static void setBoolean(Context context,String key, boolean value) {
        if (share == null) {
            getSharedPref(context);
        }
        share.edit().putBoolean(key, value).apply();
    }


    public static void remove(Context context,String key) {
        if (share == null) {
            getSharedPref(context);
        }
        share.edit().remove(key).commit();
    }

    /**
     * 清除所有数据
     *
     * @param context
     */
    public static void clear(Context context) {
        SharedPreferences.Editor editor = getSharedPref(context).edit();
        editor.clear();
        SharedPreferencesCompat.apply(editor);
    }


    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     *
     * @author zhy
     */
    private static class SharedPreferencesCompat {
        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射查找apply的方法
         *
         * @return
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
            }

            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         *
         * @param editor
         */
        public static void apply(SharedPreferences.Editor editor) {
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
            editor.commit();
        }
    }


}
