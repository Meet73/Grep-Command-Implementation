package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This class performs file content searching based on regular expressions.
 */
public class Grep {
    String pwd;
    List<Pattern> regexPatterns;
    List<String> files;
    List<String> directories;
    MyRegexOptions options;
    Result result;
    LineResult.Builder builder;

    /**
     * Constructor to initialize the Grep object with necessary parameters.
     *
     * @param pwd            The base directory path to search files in.
     * @param regexPatterns  List of regular expression patterns to match.
     * @param files          List of files to search within.
     * @param directories    List of directories to search recursively.
     * @param options        Options for search behavior.
     * @throws IllegalArgumentException If no files are provided for search.
     * @throws IOException              If an I/O error occurs during directory traversal.
     */
    public Grep(String pwd, List<Pattern> regexPatterns, List<String> files, List<String> directories, MyRegexOptions options) throws IllegalArgumentException, IOException {
        this.pwd = pwd;
        this.options = options;
        this.regexPatterns = regexPatterns;
        this.files = files;
        this.directories = directories;
        this.result = new Result();
        builder = new LineResult.Builder(this.options);
        if (options.dirSearch) {
            addFilesRecursive();
        }

        if (files.isEmpty()) {
            throw new IllegalArgumentException("{ No files to search }");
        }
    }

    /**
     * Executes the file content search based on the provided parameters and options.
     */
    public void execute() {
        if (options.invertedSearch) {
            for (String file : files) {
                invertedProcessFile(file);
            }
        } else {
            for (String file : files) {
                processFile(file);
            }
        }
    }

    /**
     * Recursively adds files from the specified directory and its subdirectories to the search list.
     *
     * @throws IOException If an I/O error occurs during directory traversal.
     */
    private void addFilesRecursive() throws IOException {
        Path dir = Paths.get(pwd);
        addFileRecursiveUtil(dir);
    }

    /**
     * Utility method to recursively add files from the given directory and its subdirectories.
     *
     * @param dir The directory path to search recursively.
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
                        result.addResult(lineResult);
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
}
