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
 * Grep class for executing pattern-based searches on files and directories using ThreadPoolExecutor.
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
     * Constructor for Grep class.
     *
     * @param pwd           The base directory path.
     * @param regexPatterns List of regular expression patterns to search for.
     * @param files         List of files to search within.
     * @param directories   List of directories to search recursively.
     * @param options       Options for the search.
     * @throws IllegalArgumentException If no files are provided for search.
     * @throws IOException              If an I/O error occurs.
     */
    public Grep(String pwd, List<Pattern> regexPatterns, List<String> files, List<String> directories, MyRegexOptions options) throws IllegalArgumentException, IOException {
        this.pwd=pwd;
        this.options=options;
        this.regexPatterns=regexPatterns;
        this.files=files;
        this.directories=directories;
        this.result=new Result();
        this.builder=new LineResult.Builder(this.options);
        if(options.dirSearch){
            addFilesRecursive();
        }

        if(files.isEmpty() && !options.dirSearch){
            throw new IllegalArgumentException("{ No files to search }");
        }
    }

    /**
     * Executes the search using a ThreadPoolExecutor with the provided executor service.
     *
     * @param executor The executor service to use for execution.
     */
    public void executeUtil(ExecutorService executor){
        if (options.invertedSearch) {
            for(String file : files) {
                executor.submit(()->{
                    invertedProcessFile(file);
                });
            }
        } else {
            for(String file : files) {
                executor.submit(()->{
                    processFile(file);
                });
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes the search using a fixed thread pool with a specific number of threads.
     */
    public void execute1()  {
        int threadPoolSize=30;
        System.out.println("creating fixed thread pool of size: "+threadPoolSize);
        ExecutorService executor= Executors.newFixedThreadPool(threadPoolSize);
        executeUtil(executor);
    }

    /**
     * Executes the search using a ThreadPoolExecutor with a custom GrowPolicy2 for handling rejected tasks.
     */
    public void execute2(){
        int corePoolSize=30;
        int maximumPoolSize=30;
        int queueSize=50;
        System.out.println("creating thread pool of corePoolSize: "+corePoolSize+" maximumPoolSize: "+maximumPoolSize+" queue of size "+queueSize+" custom Rejection Policy: GrowPolicy2 (maximum Retry:2 , BackOffTime; 5ms) ");
        ThreadPoolExecutor executor=new ThreadPoolExecutor(corePoolSize,maximumPoolSize,0,
                TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(queueSize),new GrowPolicy2(2,5));

        executeUtil(executor);
    }

    /**
     * Executes the search using a ThreadPoolExecutor with a custom GrowPolicy for handling rejected tasks.
     */
    public void execute3(){
        int corePoolSize=30;
        int maximumPoolSize=30;
        int queueSize=50;
        System.out.println("creating thread pool of corePoolSize: "+corePoolSize+" maximumPoolSize: "+maximumPoolSize+" queue of size "+queueSize+" custom Rejection Policy: GrowPolicy");

        ThreadPoolExecutor executor=new ThreadPoolExecutor(corePoolSize,maximumPoolSize,0,
                TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(queueSize),new GrowPolicy());

        executeUtil(executor);
    }

    /**
     * Executes the search using a ThreadPoolExecutor with a CallerRunsPolicy for handling rejected tasks.
     */
    public void execute4(){
        int corePoolSize=8;
        int maximumPoolSize=10;
        int queueSize=50;
        System.out.println("creating thread pool of corePoolSize: "+corePoolSize+" maximumPoolSize: "+maximumPoolSize+" queue of size "+queueSize+" Rejection Policy: CallerRunsPolicy");

        ThreadPoolExecutor executor=new ThreadPoolExecutor(corePoolSize,maximumPoolSize,0,
                TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(queueSize),new ThreadPoolExecutor.CallerRunsPolicy());

        executeUtil(executor);
    }

    /**
     * Executes the search using a ThreadPoolExecutor with default policies for handling rejected tasks.
     */
//    public void execute5(){
//        int corePoolSize=30;
//        int maximumPoolSize=30;
//        int queueSize=50;
//        System.out.println("creating thread pool of corePoolSize: "+corePoolSize+" maximumPoolSize: "+maximumPoolSize+" queue of size "+queueSize);
//
//        ThreadPoolExecutor executor=new ThreadPoolExecutor(corePoolSize,maximumPoolSize,0,
//                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
//
//        executeUtil(executor);
//    }


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
                    String relFilePath=path.toString().substring(pwd.length()+1);
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
        try (BufferedReader reader = new BufferedReader(new FileReader(pwd+"/"+file))) {
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                for (Pattern pattern : regexPatterns) {
                    Matcher matcher = pattern.matcher(line);
                    while(matcher.find()) {
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
        try (BufferedReader reader = new BufferedReader(new FileReader(pwd+"/"+file))) {
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                for (Pattern pattern : regexPatterns) {
                    Matcher matcher = pattern.matcher(line);
                    if (!matcher.find()) {
                        LineResult lineResult = builder.build(lineNumber, line, file,pattern);
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
