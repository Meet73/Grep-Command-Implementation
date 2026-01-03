package org.example;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The CacheManager class manages a cache of results with expiration capability.
 */
public class CacheManager implements Serializable {
    static String ANSI_GREEN = "\u001B[32m";
    static String ANSI_YELLOW = "\u001B[33m";
    static String ANSI_RESET = "\u001B[0m";
    static final String ANSI_RED = "\u001B[31m";
    private static final long CACHE_EXPIRATION_TIME_MS = 60000; // 1 minute expiration time
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>(); // Cache storage
    private final String cacheFilePath = "/Users/vrajk/Desktop/cache.dat"; // File path to store cache data

    /**
     * Retrieves the cache instance.
     *
     * @return The ConcurrentHashMap representing the cache.
     */
    public ConcurrentHashMap<String, CacheEntry> getCache() {
        return cache;
    }

    /**
     * Represents an entry in the cache containing the result and timestamp.
     */
    static class CacheEntry implements Serializable {
        final Result result; // Cached result
        final long timestamp; // Timestamp of cache entry creation

        CacheEntry(Result result, long timestamp) {
            this.result = result;
            this.timestamp = timestamp;
        }
    }

    // Singleton instance
    private static final CacheManager instance = new CacheManager();

    private CacheManager() {
        loadCacheFromFile(); // Load cache from file on startup
    }

    /**
     * Retrieves the singleton instance of CacheManager.
     *
     * @return The CacheManager instance.
     */
    public static CacheManager getInstance() {
        return instance;
    }

    /**
     * Adds a result to the cache with the specified key.
     *
     * @param key    The key for caching the result.
     * @param result The Result object to cache.
     */
    public void addResultToCache(String key, Result result) {
        CacheEntry newEntry = new CacheEntry(result, System.currentTimeMillis());
        cache.put(key, newEntry);
        saveCacheToFile(); // Save cache to file after adding entry
    }

    /**
     * Retrieves a result from the cache based on the key.
     *
     * @param key The key to look up in the cache.
     * @return The Result object if found and not expired, or null if not found or expired.
     */
    public Result getResultFromCache(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && (System.currentTimeMillis() - entry.timestamp < CACHE_EXPIRATION_TIME_MS)) {
            System.out.println(ANSI_GREEN+"Cache hit for key: " + key+ANSI_RESET);
            return entry.result;
        } else if (entry != null) {
            cache.remove(key);
            System.out.println(ANSI_GREEN+"Cache entry expired and removed for key: " + key+ANSI_RESET);
            saveCacheToFile();
        }
        return null; // Cache miss
    }

    /**
     * Saves the cache to a file.
     */
    void saveCacheToFile() {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(cacheFilePath))) {
            outputStream.writeObject(cache);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the cache from a file.
     */
    private void loadCacheFromFile() {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(cacheFilePath))) {
            Object obj = inputStream.readObject();
            if (obj instanceof ConcurrentHashMap) {
                cache.putAll((ConcurrentHashMap<String, CacheEntry>) obj);
            }
            System.out.println(cache.size());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
