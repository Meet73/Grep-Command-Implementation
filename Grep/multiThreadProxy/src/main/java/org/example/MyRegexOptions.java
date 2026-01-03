package org.example;

import java.util.List;

/**
 * Represents the options that can be specified for the custom grep operation.
 * These options control how the search operation is performed and how its results
 * are presented to the user.
 */
public class MyRegexOptions {
    final boolean onlyCountLines; // Option to only count the lines that match the pattern
    final boolean invertedSearch; // Option for inverted search (selecting lines that do not match)
    final boolean caseInsensitive; // Option for case-insensitive search
    final boolean showLineNumbers; // Option to show line numbers in search results
    final boolean showLines; // Option to show the matching lines
    final boolean showFiles; // Option to show files where matches are found
    final boolean dirSearch; // Option to enable recursive directory search
    final boolean showPattern; // Option to show the pattern used for matching in the output

    /**
     * Constructs an instance of MyRegexOptions based on the specified list of option flags.
     * Validates the combination of options to ensure they are compatible.
     *
     * @param options List of strings representing the option flags specified by the user.
     * @throws IllegalArgumentException if an incompatible combination of options is provided.
     */
    public MyRegexOptions(List<String> options) {
        this.onlyCountLines = options.contains("-c");
        this.invertedSearch = options.contains("-v");
        this.showLineNumbers = options.contains("-n");
        this.showLines = options.contains("-l");
        this.showFiles = options.contains("-sf");
        this.caseInsensitive = options.contains("-i");
        this.dirSearch = options.contains("-R");
        this.showPattern = options.contains("-sp");

        // Validate options to ensure -c is not used with options that affect output formatting.
        if (onlyCountLines && (showLines || showLineNumbers || showFiles || showPattern)) {
            throw new IllegalArgumentException("-c cannot be used together with [ -n | -l | -sf | -sp ]");
        }
    }
}
