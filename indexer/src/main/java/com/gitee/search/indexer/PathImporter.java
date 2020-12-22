package com.gitee.search.indexer;

import com.gitee.search.queue.QueueTask;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 直接导入指定目录下的 json 数据
 * 使用方法：gsimport -p json-path -t repo -a add
 * 注意事项：该工具不能和 indexer 服务同时运行，否则会锁住索引库
 * @author Winter Lau<javayou@gmail.com>
 */
public class PathImporter {

    private final static Logger log = LoggerFactory.getLogger(PathImporter.class);
    private final static int DEFAULT_THREAD_COUNT = 5;
    private final static int MAX_THREAD_COUNT = 50;

    private final static Options options = new Options(){{
        addRequiredOption("p", "path",true, "json path");
        addOption("t", "type", true, "json object type (repo|issue|user|commit|code|pr)");
        addOption("a", "action", true, "action(add|update|delete)");
        addOption("h", "help",true, "print help");
    }};

    public static void main(String[] args) {
        CommandLine cmd = null;
        CommandLineParser parser = new DefaultParser();
        Path jsonPath = null;
        try {
            cmd = parser.parse(options, args);
            String path = cmd.getOptionValue("p");
            if (cmd.hasOption("h") || path == null) {
                printHelp();
                return;
            }

            jsonPath = Paths.get(path);

            String type = cmd.getOptionValue("t", "repo");
            String action = cmd.getOptionValue("a", "add");

            if(!QueueTask.isAvailType(type) || !QueueTask.isAvailAction(action)){
                printHelp();
                return;
            }

            long ct = System.currentTimeMillis();

            int fc = importPath(type, action, jsonPath);

            log.info("\"{}\" {} files imported, time:{}ms", jsonPath.toString(), fc, (System.currentTimeMillis()-ct));

        } catch (IOException e) {
            log.error("Failed to import path:" + jsonPath.toString(), e);
        } catch (ParseException e) {
            printHelp();
        }
    }

    /**
     * 读取指定目录下所有 json 文件并写入索引
     * @param type
     * @param action
     * @param path
     */
    private static int importPath(String type, String action, Path path) throws IOException {
        final AtomicInteger fc = new AtomicInteger(0);
        Stream<Path> files = Files.list(path).filter(p -> p.toString().endsWith(".json") && !Files.isDirectory(p));
        files.forEach(jsonFile -> {
            long ct = System.currentTimeMillis();
            importFile(type, action, jsonFile);
            log.info("{} imported in {}ms. ({})", jsonFile.toString(), System.currentTimeMillis() - ct, Thread.currentThread().getName());
            fc.addAndGet(1);
        });
        return fc.get();
    }

    /**
     * 读取单个 json 文件并写入索引库
     * @param type
     * @param action
     * @param file
     */
    private static void importFile(String type, String action, Path file) {
        try {
            String json = Files.readAllLines(file).stream().collect(Collectors.joining());
            QueueTask task = new QueueTask();
            task.setType(type);
            task.setAction(action);
            task.setBody(json);
            task.write();
        } catch (IOException e) {
            log.error("Failed to import file: " + file.toString(), e);
        }
    }

    /**
     * 显示帮助
     */
    private static void printHelp() {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(110);
        hf.printHelp("gsimport", options, true);
    }

}
