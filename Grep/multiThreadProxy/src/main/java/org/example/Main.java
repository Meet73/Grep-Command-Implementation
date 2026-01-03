package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
/**
 * The main class for a custom grep-like application.
 * This class handles parsing of command-line arguments and initiates the search process
 * based on provided patterns, files, and options.
 */
public class Main {
    static String ANSI_GREEN = "\u001B[32m";
    static String ANSI_YELLOW = "\u001B[33m";
    static String ANSI_RESET = "\u001B[0m";
    static final String ANSI_RED = "\u001B[31m";
    /**
     * The entry point of the application.
     *
     * @param args The command-line arguments.
     * @throws IOException If an I/O error occurs.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        String pwd = System.getProperty("user.dir");
        if(args.length<2){
            throw new IllegalArgumentException("Usage : Grep.java -p <pattern1> <pattern2> ... -f <file1> <file2> ..  [ -c | -n | -l |  ] ");
        }
        System.out.println("ran command from = "+pwd);

        List<Pattern> regexPatterns = new ArrayList<>();
        List<String> options = new ArrayList<>();
        List<String> files = new ArrayList<>();
        List<String> dirs=new ArrayList<>();

        for(int i = 0; i < args.length; i++) {
            System.out.println("parsing args["+i+"]="+args[i]);
            switch(args[i]){
                case "-p":
                    i++;
                    while (i < args.length && !args[i].startsWith("-")) {
                        try {
                            regexPatterns.add(Pattern.compile(args[i]));
                        } catch (PatternSyntaxException e ){
                            System.out.println(ANSI_RED+"Regex pattern is not valid: " + e.getMessage()+ANSI_RESET);
                            System.out.println(" [ignored]  + " + args[i] + " pattern");
                        }
                        i++;
                    }
                    i--;
                    break;
                case "-f":
                    i++;
                    while (i < args.length && !args[i].startsWith("-")) {
                        try{
                            String filePath = pwd + "/" + args[i];
                            // Check if the file exists
                            boolean exists = Files.exists(Paths.get(filePath));
                            if (exists) {
                                files.add(args[i]);
                            } else {
                                System.out.println("File "+args[i]+ " does not exist. [IGNORING] : filePath="+filePath);
                            }
                        } catch (Exception e) {
                                System.out.println("[ERROR] opening file "+args[i] + " : " + e);
                        }
                        i++;
                    }
                    i--;
                    break;
                case "-n":
                    //to show line number
                    options.add("-n");
                    break;
                case "-l":
                    //to show the line
                    options.add("-l");
                    break;
                case "-sf":
                    //to show the file
                    options.add("-sf");
                    break;
                case "-sp":
                    //to show the file
                    options.add("-sp");
                    break;
                case "-v":
                    //inverted search
                    options.add("-v");
                    break;
                case "-i":
                    //case insensitive
                    options.add("-i");
                    break;
                case "-c":
                    //only count
                    options.add("-c");
                    break;
                case "-R":
                    options.add("-R");
                    break;
                default:
                    System.out.println(ANSI_RED+"Usage: Grep.java -p <pattern1> <pattern2> ... -f <file1> <file2> ..  [-c | -n | -v ] "+ANSI_RESET);
                    System.out.println(ANSI_RED+"... unknown option " + args[i] + "  ignoring... "+ANSI_RESET);
                    break;
            }
        }

        System.out.println("arguments processed.");
        System.out.print("Patterns = [");
        for(Pattern regexPattern : regexPatterns){
            System.out.print(regexPattern + ",");
        }
        System.out.println("]");

        //now process files and throw excpetions if any
        System.out.print("Files gotten = [");
        for(String file : files){
            System.out.print(file +",");
        }


        System.out.print("Options = [");
        for(String option : options){
            System.out.print(option +",");
        }
        System.out.println("]");

        MyRegexOptions optionsObj=new MyRegexOptions(options);

        List<Pattern> patternsToPass=new ArrayList<>();
        if(optionsObj.caseInsensitive){
            System.out.println("making patterns in insensitive");
            for(Pattern pattern : regexPatterns) {
                patternsToPass.add(Pattern.compile(pattern.pattern(), pattern.flags() | Pattern.CASE_INSENSITIVE));
            }
        } else {
            for(Pattern pattern : regexPatterns) {
                patternsToPass.add(Pattern.compile(pattern.pattern() ));
            }
        }

        long begin = System.currentTimeMillis();
        ResultCacheProxy cacheProxy = new ResultCacheProxy(pwd, patternsToPass, files, dirs, optionsObj);
        Result result = cacheProxy.execute();
        long end = System.currentTimeMillis();

        System.out.println();
        result.printCount();

        System.out.println(ANSI_YELLOW+" ... took " + (end - begin) + "ms"+ANSI_RESET);
    }
}
