package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A container for accumulating and reporting the results of a search operation.
 * It keeps track of individual matching lines as well as the total count of matches.
 */
public class Result {
    static String ANSI_GREEN = "\u001B[32m";
    static String ANSI_YELLOW = "\u001B[33m";
    static String ANSI_RESET = "\u001B[0m";
    // List to hold individual search results
    private final List<LineResult> result;
    // Counter for the total number of matches found
    AtomicInteger count;

    /**
     * Constructs a new, empty Result object.
     */
    public Result(){
        this.result = Collections.synchronizedList( new ArrayList<>());
        this.count = new AtomicInteger(0);
    }

    /**
     * Adds a single line result to this container and increments the match count.
     *
     * @param lineResult The result of a single line search to add.
     */
    public void addResult(LineResult lineResult) {
        result.add(lineResult);
        count.getAndIncrement();
    }

    /**
     * Prints all accumulated line results to the standard output.
     * Each line result is printed on a new line.
     */
    public void printResults() {
        for(LineResult lineResult : result) {
            System.out.println(lineResult);
        }
    }

    /**
     * Prints the total count of matches found to the standard output.
     */
    public void printCount() {
        System.out.println(ANSI_GREEN+"Total matches found: " + count+ANSI_RESET);
    }
}
