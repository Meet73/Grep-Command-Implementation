package org.example;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.toIntExact;

/**
 * Implements a runnable task designed to read a file in chunks and process these chunks to find matches
 * based on a list of regex patterns. This is particularly useful for processing large files that cannot
 * be loaded into memory all at once.
 */


public class FileReadInChunks implements Runnable {
    private FileChannel channel;
    private long startLocation;
    private long endLocation;
    private int sequenceNumber;
    List<Pattern> regexPatterns;
    LineResult.Builder builder;
    Result result;
    List<String> files;
    MyRegexOptions options;


    /**
     * Constructs a FileReadInChunks task.
     *
     * @param channel        The FileChannel to read the file.
     * @param startLocation  The starting byte position in the file from where to begin reading.
     * @param endLocation    The ending byte position in the file at which to stop reading.
     * @param sequenceNumber An identifier for the chunk sequence.
     * @param regexPatterns  A list of compiled regex patterns to match against the file's content.
     * @param builder        A builder to construct line results.
     * @param result         A container for accumulating the results.
     * @param files          A list of file names being processed.
     * @param options        Options defining how the regex search should be conducted.
     */
    public FileReadInChunks(FileChannel channel, long startLocation, long endLocation, int sequenceNumber, List<Pattern> regexPatterns, LineResult.Builder builder, Result result, List<String> files, MyRegexOptions options) {
        this.channel = channel;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.sequenceNumber = sequenceNumber;
        this.regexPatterns = regexPatterns;
        this.builder = builder;
        this.result = result;
        this.files = files;
        this.options = options;
    }

    public void setRegexPatterns(List<Pattern> regexPatterns) {
        this.regexPatterns = regexPatterns;
    }

    public void setBuilder(LineResult.Builder builder) {
        this.builder = builder;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public void setOptions(MyRegexOptions options) {
        this.options = options;
    }

    public FileReadInChunks(){

    }

    /**
     * Executes the task of reading the file in chunks and processing each chunk.
     * It reads the file segment assigned to this task and processes it line by line to find matches.
     */
    @Override
    public void run() {
        try {
            long size = endLocation - startLocation + 1;
            while (size > Integer.MAX_VALUE) {
                ByteBuffer buffer = ByteBuffer.allocate(toIntExact(Integer.MAX_VALUE));
                channel.read(buffer, startLocation);


                // Convert the buffer to a String
                buffer.flip(); // Flip the buffer for reading

//                System.out.println("sequence no. " + sequenceNumber + " size : " + size);

                processBuffer(buffer);
                size -= Integer.MAX_VALUE;
            }

            if (size > 0) {
                ByteBuffer buffer = ByteBuffer.allocate(toIntExact(size));
                channel.read(buffer, startLocation);

                // Convert the buffer to a String
                buffer.flip(); // Flip the buffer for reading

//                System.out.println("sequence no. " + sequenceNumber + " size : " + size);

                processBuffer(buffer);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes the content of the ByteBuffer as a sequence of lines, looking for regex matches.
     *
     * @param buffer The ByteBuffer containing the chunk of the file to process.
     */
    void processBuffer(ByteBuffer buffer) {
        int startPosition = 0;
        int currentPosition = 0;

        while (currentPosition < buffer.limit()) {
            byte currentByte = buffer.get();
            currentPosition++;

            if (currentByte == '\n') {
                // Process the line from startPosition to currentPosition
                processLine(buffer, startPosition, currentPosition - 1);

                // Move to the next line
                startPosition = currentPosition;
            }
        }

        // Process the last line if it doesn't end with a newline character
        if (startPosition < currentPosition) {
            processLine(buffer, startPosition, currentPosition);
        }
    }

    /**
     * Processes a single line from the ByteBuffer.
     *
     * @param buffer The ByteBuffer containing the file's data.
     * @param start  The starting position of the line in the buffer.
     * @param end    The ending position of the line in the buffer.
     */
    void processLine(ByteBuffer buffer, long start, long end) {
        // Extract and process the line bytes from start to end (exclusive)
        byte[] lineBytes = new byte[(int) (end - start)];
        buffer.position((int) start);
        buffer.get(lineBytes);

        // Perform operations on lineBytes as needed
        processChunks(lineBytes);
    }

    /**
     * Processes a chunk of bytes representing a portion of a file. It converts the byte array into a String
     * using UTF-8 encoding and then searches for matches against a list of regex patterns. Depending on the
     * search options, it constructs LineResult objects for each match and adds them to the result container.
     *
     * @param chunk The byte array representing a chunk of the file content.
     */
    private void processChunks(byte[] chunk) {
        String fileName = files.get(0);
        String line = new String(chunk, StandardCharsets.UTF_8); // Convert line bytes to String
        for (Pattern pattern : regexPatterns) {
            Matcher matcher = pattern.matcher(line);
            if (options.invertedSearch) {
                while (!matcher.find()) {
                    LineResult lineResult = builder.build(null, line, fileName, pattern);
                    result.addResult(lineResult);
                }
            } else {
                while (matcher.find()) {
                    LineResult lineResult = builder.build(null, line, fileName, pattern);
                    result.addResult(lineResult);
                }
            }
        }
    }
}