package org.easytech.pelatologio.helper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class PlaceholderUtil {

    public static Map<String, String> createPlaceholders(Object... dataObjects) {
        System.out.println("--- Creating Placeholders ---");
        Map<String, String> placeholders = new HashMap<>();
        for (Object obj : dataObjects) {
            if (obj == null) continue;
            System.out.println("Processing object of type: " + obj.getClass().getName());

            if (obj instanceof Map) {
                // Handle Map objects
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                    String key = entry.getKey().toString();
                    String value = entry.getValue() != null ? entry.getValue().toString() : "";
                    System.out.println("  Map -> Key: '" + key + "', Value: '" + value + "'");
                    placeholders.put(key, value);
                }
            } else {
                // Handle POJO objects
                String className = obj.getClass().getSimpleName().toLowerCase();
                for (Field field : obj.getClass().getFields()) { // Use getFields() to access only public fields
                    try {
                        Object value = field.get(obj);
                        String key = "{" + className + "." + field.getName() + "}";
                        String valueStr = value != null ? value.toString() : "";
                        System.out.println("  POJO -> Key: '" + key + "', Value: '" + valueStr + "'");
                        placeholders.put(key, valueStr);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("--- Finished Placeholders ---");
        return placeholders;
    }
}
