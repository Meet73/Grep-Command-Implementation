package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A proxy for the Grep class that implements caching of results.
 * This class intercepts the execution of the grep operation to check
 * if the result is already cached based on a generated key. If the result
 * is found in the cache, it is returned immediately without re-executing
 * the grep operation.
 */
public class ResultCacheProxy extends Grep {
    static String ANSI_GREEN = "\u001B[32m";
    static String ANSI_YELLOW = "\u001B[33m";
    static String ANSI_RESET = "\u001B[0m";

    /**
     * Constructs a new ResultCacheProxy instance.
     *
     * @param pwd            The current working directory.
     * @param regexPatterns  The list of regular expression patterns to search for.
     * @param files          The list of files to be searched.
     * @param directories    The list of directories to be searched.
     * @param options        The grep options.
     * @throws IllegalArgumentException if an argument is not valid.
     * @throws IOException if an I/O error occurs.
     */
    public ResultCacheProxy(String pwd, List<Pattern> regexPatterns, List<String> files, List<String> directories, MyRegexOptions options) throws IllegalArgumentException, IOException {
        super(pwd, regexPatterns, files, directories, options);
    }

    /**
     * Executes the grep operation with caching. Checks the cache first using a generated
     * key based on the search parameters. If the result is cached, it is returned immediately.
     * Otherwise, the grep operation is executed, and the result is cached before returning.
     *
     * @return The result of the grep operation, either from the cache or freshly computed.
     * @throws InterruptedException if the operation is interrupted.
     */
    @Override
    public Result execute() throws InterruptedException {
        String cacheKey = generateCacheKey();
        CacheManager cacheManager = CacheManager.getInstance();
        Result cachedResult = cacheManager.getResultFromCache(cacheKey);
        System.out.println("----");

        if (cachedResult != null) {
            System.out.println("Returning cached result for key: " + cacheKey);
            return cachedResult;
        } else {
            Result result = super.execute();
            cacheManager.addResultToCache(cacheKey, result);
            return result;
        }
    }

    /**
     * Generates a cache key based on the grep operation's parameters. This key
     * is used to uniquely identify the set of parameters for caching purposes.
     *
     * @return A String representing the cache key.
     */
    private String generateCacheKey() {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("Patterns:");
        for (Pattern pattern : getRegexPatterns()) {
            keyBuilder.append(pattern.toString()).append(",");
        }
        keyBuilder.append("|Files:");
        for (String file : getFiles()) {
            keyBuilder.append(file).append(",");
        }
        keyBuilder.append("|Dirs:");
        for (String dir : getDirectories()) {
            keyBuilder.append(dir).append(",");
        }
        // Generate a SHA-256 hash of the keyBuilder's content for a more compact and unique key representation
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(keyBuilder.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash); // Convert to base64 for readability
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
