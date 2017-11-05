package eu.baron_online.homework;

import android.app.Activity;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.StringTokenizer;

public class DataInterchange {

    public static final String PREFS_NAME = "eu.barononline.homework.persistentmemory";

    private static HashMap<String, Object> entrys = new HashMap<>();
    private static SharedPreferences settings;

    public static void addValue(String key, Object value) {
        entrys.put(key, value);
    }

    public  static Object getValue(String key) {
        return entrys.get(key);
    }

    public static void removeValue(String key) {
        entrys.remove(key);
    }

    public static boolean containsKey(String key) {
        return entrys.containsKey(key);
    }

    public static boolean existsPersistent(String key) {
        settings = ToolbarActivity.instance.getSharedPreferences(PREFS_NAME, 0);
        return settings.contains(key);
    }
    public static void removePersisten(String key) {
        settings = ToolbarActivity.instance.getSharedPreferences(PREFS_NAME, 0);
        settings.edit().remove(key).apply();
    }

    public static void addPersistent(String key, String value) {
        settings = ToolbarActivity.instance.instance.getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putString(key, value).apply();
    }
    public static void addPersistent(String key, int value) {
        settings = ToolbarActivity.instance.instance.getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putInt(key, value).apply();
    }
    public static void addPersistent(String key, boolean value) {
        settings = ToolbarActivity.instance.instance.getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putBoolean(key, value).apply();
    }

    public static String getPersistentString(String key) {
        settings = LoginActivity.instance.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(key, "");
    }
    public static int getPersistentInt(String key) {
        settings = LoginActivity.instance.getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt(key, 0);
    }
    public static boolean getPersistentBoolean(String key) {
        settings = LoginActivity.instance.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(key, false);
    }
}
