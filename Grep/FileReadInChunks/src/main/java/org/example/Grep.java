package org.example;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * The Grep class is responsible for searching for patterns in files and directories using multiple threads.
 */
public class Grep {
    String pwd; // The current working directory path
    List<Pattern> regexPatterns; // List of regex patterns to search for
    List<String> files; // List of files to search in
    List<String> directories; // List of directories to search in
    MyRegexOptions options; // Options for regex search
    Result result; // Result container for search matches
    LineResult.Builder builder; // Builder for LineResult objects

    // Maximum chunk size for file reading (300 MB)
    static long MAX_CHUNK_SIZE = 300L * 1024L * 1024L;

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
        this.result = new Result();
        this.builder = new LineResult.Builder(this.options);

        if (files.isEmpty()) {
            throw new IllegalArgumentException("{ No files to search }");
        }
    }

    /**
     * Executes the grep operation by searching for patterns in files using multiple threads.
     *
     * @throws Exception If an error occurs during the execution.
     */
    public void execute() throws Exception {

        String file = files.get(0);

        System.out.println("file: " + file);

        try (FileInputStream fileInputStream = new FileInputStream(file);
             FileChannel channel = fileInputStream.getChannel()) {
            long fileSize = channel.size();
            System.out.println("filesize: " + fileSize);

            int threadPoolSize = 10;

            long chunkSize = fileSize / threadPoolSize;
            System.out.println("Calculated Chunk size: " + chunkSize);
            if (chunkSize > MAX_CHUNK_SIZE) {
                chunkSize = MAX_CHUNK_SIZE;
            }
            System.out.println("Final Chunk size: " + chunkSize);

            BlockingQueue<Runnable> taskQueue = new ArrayBlockingQueue<>(100);

            RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();

            ExecutorService executor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0, TimeUnit.MILLISECONDS, taskQueue, rejectedExecutionHandler);

            long startLocation = 0;
            int sequenceNumber = 0;

            while (startLocation < fileSize) {
                long endLocation = startLocation + chunkSize - 1;
                if (endLocation >= fileSize) {
                    endLocation = fileSize - 1;
                } else {
                    endLocation = adjustEndLocation(channel, endLocation);
                }

                System.out.println("Start: " + startLocation + ", End: " + endLocation);
                executor.submit(new FileReadInChunks(channel, startLocation, endLocation, sequenceNumber++, regexPatterns, builder, result, files, options));

                startLocation = endLocation + 1;
            }

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            System.out.println("Finished all Threads");
        }
    }

    /**
     * Adjusts the end location of a chunk to ensure that it ends at a valid line boundary.
     *
     * @param channel     The file channel.
     * @param endLocation The original end location of the chunk.
     * @return The adjusted end location after ensuring it ends at a newline boundary.
     * @throws Exception If an error occurs during the adjustment.
     */
    private static long adjustEndLocation(FileChannel channel, long endLocation) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        while (endLocation < channel.size() - 1) {
            channel.read(buffer, endLocation);
            buffer.flip();
            if (buffer.get() == '\n') {
                break;
            }
            buffer.clear();
            endLocation++;
        }
        return endLocation;
    }
}
