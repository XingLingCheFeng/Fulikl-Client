package missu.epsilon.client.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.Util;

public class SmartCacheManager {
    private static class CacheEntry {
        String data;
        long lastAccessTime;

        CacheEntry(String data) {
            this.data = data;
            this.lastAccessTime = Util.getMeasuringTimeMs();
        }
    }

    private static final Int2ObjectOpenHashMap<CacheEntry> CACHE = new Int2ObjectOpenHashMap<>();


    private static final long EXPIRATION_TIME_MS = 30000;

    public static void put(int id, String data) {
        CACHE.put(id, new CacheEntry(data));
    }

    public static String get(int id) {
        CacheEntry entry = CACHE.get(id);
        if (entry != null) {

            entry.lastAccessTime = Util.getMeasuringTimeMs();
            return entry.data;
        }
        return null;
    }

    public static void clear() {
        if (!CACHE.isEmpty()) {
            CACHE.clear();
            System.out.println("[Epsilon] 缓存已全部清空");
        }
    }

    public static void pruneOldEntries() {
        if (CACHE.isEmpty()) return;

        long now = Util.getMeasuringTimeMs();

        boolean removed = CACHE.int2ObjectEntrySet().removeIf(entry ->
                now - entry.getValue().lastAccessTime > EXPIRATION_TIME_MS
        );

        if (removed) {

            System.out.println("[Epsilon] 已清理过期缓存");
        }
    }
}
