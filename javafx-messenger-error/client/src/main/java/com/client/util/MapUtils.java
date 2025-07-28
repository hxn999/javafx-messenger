package com.client.util;

    import java.util.Map;
    import java.util.Objects;

    public class MapUtils {
        public static <K, V> K getKeyFromValue(Map<K, V> map, V value) {
            if (map == null) {
                return null; // map is null, return null
            }
            for (Map.Entry<K, V> entry : map.entrySet()) {
                if (Objects.equals(entry.getValue(), value)) {
                    return entry.getKey();
                }
            }
            return null; // not found
        }
    }