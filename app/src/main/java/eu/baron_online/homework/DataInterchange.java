package eu.baron_online.homework;

import java.util.HashMap;

public class DataInterchange {

    private static HashMap<String, Object> entrys = new HashMap<>();

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
}
