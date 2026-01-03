package org.example;

import java.util.regex.Pattern;

/**
 * Represents a single result line from the grep operation.
 * It may include details such as the line number, the line content,
 * the file name, and the pattern matched.
 */
public class LineResult {
    Integer lineNumber; // Line number where the match was found
    String line; // The content of the matching line
    String file; // File name where the match was found
    String pattern; // The pattern that was matched

    /**
     * Overrides the toString method to provide a string representation
     * of the line result, including any relevant details that are not null.
     *
     * @return A string representation of the LineResult instance.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LineResult{");
        if (file != null) {
            sb.append("file='").append(file).append("' , ");
        }
        if (lineNumber != null) {
            sb.append("lineNumber=").append(lineNumber).append(", ");
        }
        if (pattern != null) {
            sb.append("pattern='").append(pattern).append("', ");
        }
        if (line != null) {
            sb.append("line='").append(line).append("'");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Private constructor for creating a LineResult instance.
     * This is used internally by the Builder class.
     *
     * @param lineNumber The line number where the match was found.
     * @param line The content of the matching line.
     * @param file The file name where the match was found.
     * @param pattern The pattern that was matched.
     */
    private LineResult(Integer lineNumber, String line, String file, Pattern pattern) {
        this.lineNumber = lineNumber;
        this.line = line;
        this.file = file;
        this.pattern = (pattern != null) ? String.valueOf(pattern) : null;
    }

    /**
     * Builder class for constructing LineResult instances with specific options.
     */
    public static class Builder {

        private MyRegexOptions options; // The options affecting the result format

        /**
         * Constructor for the Builder class.
         *
         * @param options The options that determine what details are included in the LineResult.
         */
        public Builder(MyRegexOptions options) {
            this.options = options;
        }

        /**
         * Builds a LineResult instance based on the specified parameters and the provided options.
         * This allows for flexible creation of LineResult objects based on the grep operation's options.
         *
         * @param lineNumber The line number of the match.
         * @param line The content of the matching line.
         * @param file The file name where the match was found.
         * @param pattern The pattern matched.
         * @return A new LineResult instance configured according to the specified options.
         */
        public LineResult build(Integer lineNumber, String line, String file, Pattern pattern) {
            if (options.showLineNumbers && options.showLines && options.showFiles && options.showPattern) {
                return new LineResult(lineNumber, line, file, pattern);
            } else if (options.showLineNumbers && options.showLines && options.showFiles) {
                return new LineResult(lineNumber, line, file, null);
            } else if (options.showLineNumbers && options.showLines && options.showPattern) {
                return new LineResult(lineNumber, line, null, pattern);
            } else if (options.showLineNumbers && options.showFiles && options.showPattern) {
                return new LineResult(lineNumber, null, file, pattern);
            } else if (options.showLineNumbers && options.showPattern) {
                return new LineResult(lineNumber, null, null, pattern);
            } else if (options.showLines && options.showFiles && options.showPattern) {
                return new LineResult(null, line, file, pattern);
            } else if (options.showLines && options.showPattern) {
                return new LineResult(null, line, null, pattern);
            } else if (options.showFiles && options.showPattern) {
                return new LineResult(null, null, file, pattern);
            } else if (options.showLineNumbers && options.showLines) {
                return new LineResult(lineNumber, line, null, null);
            } else if (options.showLineNumbers && options.showFiles) {
                return new LineResult(lineNumber, null, file, null);
            } else if (options.showLineNumbers) {
                return new LineResult(lineNumber, null, null, null);
            } else if (options.showLines && options.showFiles) {
                return new LineResult(null, line, file, null);
            } else if (options.showLines) {
                return new LineResult(null, line, null, null);
            } else if (options.showFiles) {
                return new LineResult(null, null, file, null);
            } else if (options.showPattern) {
                return new LineResult(null, null, null, pattern);
            } else {
                return new LineResult(null, null, null, null);
            }
        }
    };
}
