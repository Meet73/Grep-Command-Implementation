package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The Grep class is responsible for searching for patterns in files and directories using multiple threads.
 */
public class Grep {
    private final String pwd; // The current working directory path
    private final List<Pattern> regexPatterns; // List of regex patterns to search for
    private final List<String> files; // List of files to search in
    private final List<String> directories; // List of directories to search in
    private final MyRegexOptions options; // Options for regex search
    private final Result result; // Result container for search matches
    private final LineResult.Builder builder; // Builder for LineResult objects

    /**
     * Constructs a Grep object with the specified parameters.
     *
     * @param pwd           The current working directory path.
     * @param regexPatterns List of regex patterns to search for.
     * @param files         List of files to search in.
     * @param directories   List of directories to search in.
     * @param options       Options for regex search.
     * @throws IllegalArgumentException If no files are provided for searching.
     * @throws IOException              If an I/O error occurs.
     */
    public Grep(String pwd, List<Pattern> regexPatterns, List<String> files, List<String> directories, MyRegexOptions options) throws IllegalArgumentException, IOException {
        this.pwd = pwd;
        this.options = options;
        this.regexPatterns = regexPatterns;
        this.files = files;
        this.directories = directories;
        builder = new LineResult.Builder(options);
        this.result = new Result();

        if (options.dirSearch) {
            addFilesRecursive();
        }

        if (files.isEmpty()) {
            throw new IllegalArgumentException("{ No files to search }");
        }
    }

    /**
     * Executes the grep operation by searching for patterns in files using multiple threads.
     *
     * @return The Result object containing search matches.
     */
    public Result execute() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(25);
        for (String file : files) {
            executorService.submit(() -> {
                if (options.invertedSearch) {
                    invertedProcessFile(file);
                } else {
                    processFile(file);
                }
            });
        }

        executorService.shutdown();

        executorService.awaitTermination(Long.MAX_VALUE,TimeUnit.MILLISECONDS);
        return result; // Return the result after processing all files
    }

    /**
     * Recursively adds files from directories to the search list.
     *
     * @throws IOException If an I/O error occurs during directory traversal.
     */
    private void addFilesRecursive() throws IOException {
        Path dir = Paths.get(pwd);
        addFileRecursiveUtil(dir);
    }

    /**
     * Utility method to add files from a directory recursively.
     *
     * @param dir The directory path to traverse.
     * @throws IOException If an I/O error occurs during directory traversal.
     */
    private void addFileRecursiveUtil(Path dir) throws IOException {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.forEach(path -> {
                if (Files.isRegularFile(path)) {
                    String relFilePath = path.toString().substring(pwd.length() + 1);
                    files.add(relFilePath);
                }
            });
        }
    }

    /**
     * Processes the content of a file line by line using the specified regular expression patterns.
     *
     * @param file The file path to process.
     */
    private void processFile(String file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(pwd + "/" + file))) {
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                for (Pattern pattern : regexPatterns) {
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        LineResult lineResult = builder.build(lineNumber, matcher.group(), file, pattern);
                        synchronized (result) {
                            result.addResult(lineResult);
                        }
                    }
                }
                lineNumber++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes the content of a file line by line using inverted search based on specified regular expression patterns.
     *
     * @param file The file path to process with inverted search.
     */
    private void invertedProcessFile(String file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(pwd + "/" + file))) {
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                for (Pattern pattern : regexPatterns) {
                    Matcher matcher = pattern.matcher(line);
                    if (!matcher.find()) {
                        LineResult lineResult = builder.build(lineNumber, line, file, pattern);
                        result.addResult(lineResult);
                    }
                }
                lineNumber++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getters for caching key generation in the proxy
    public String getPwd() {
        return pwd;
    }

    public List<Pattern> getRegexPatterns() {
        return regexPatterns;
    }

    public List<String> getFiles() {
        return files;
    }

    public List<String> getDirectories() {
        return directories;
    }

    public MyRegexOptions getOptions() {
        return options;
    }
}
