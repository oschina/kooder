package com.gitee.search.indexer;

import com.gitee.search.queue.QueueTask;
import com.gitee.search.storage.StorageFactory;
import com.gitee.search.utils.BatchTaskRunner;
import org.apache.commons.cli.*;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 直接导入指定目录下的 json 数据
 * 使用方法：gsimport -p json-path -t repo -a add
 * 注意事项：该工具不能和 indexer 服务同时运行，否则会锁住索引库
 * @author Winter Lau<javayou@gmail.com>
 */
public class PathImporter {

    private final static Logger log = LoggerFactory.getLogger(PathImporter.class);
    private final static int DEFAULT_THREAD_COUNT = 1;
    private final static int MAX_THREAD_COUNT = 50;

    private final static Options options = new Options(){{
        addRequiredOption("p", "path",true, "json path");
        addOption("t", "type", true, "json object type (repo|issue|user|commit|code|pr)");
        addOption("a", "action", true, "action(add|update|delete)");
        addOption("c", "concurrent",true, "concurrent thread count(default:5)");
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
            int thread_count = NumberUtils.toInt(cmd.getOptionValue("c"), DEFAULT_THREAD_COUNT);
            long ct = System.currentTimeMillis();

            int fc = importJsonInPath(type, action, jsonPath, thread_count);

            log.info("{} files in {} imported,time:{}ms", fc, jsonPath.toString(), (System.currentTimeMillis()-ct));
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
     * @param thread_count
     * @return file count
     */
    private static int importJsonInPath(String type, String action, Path path, int thread_count) throws IOException {
        final AtomicInteger fc = new AtomicInteger(0);
        thread_count = Math.min(MAX_THREAD_COUNT, Math.max(thread_count, 1));
        try (
            IndexWriter writer = StorageFactory.getIndexWriter(type);
            TaxonomyWriter taxonomyWriter = StorageFactory.getTaxonomyWriter(type);
        ) {
            List<Path> allFiles = Files.list(path).filter(p -> p.toString().endsWith(".json") && !Files.isDirectory(p)).collect(Collectors.toList());
            int threshold = Math.max(allFiles.size()/thread_count, 1);
            BatchTaskRunner.execute(allFiles, threshold, files -> {
                files.forEach( jsonFile -> {
                    importJsonFile(type, action, jsonFile, writer, taxonomyWriter);
                    fc.addAndGet(1);
                });
            });
        }
        return fc.get();
    }

    /**
     * 读取单个 json 文件并写入索引库
     * @param type
     * @param action
     * @param file
     * @param i_writer
     * @param t_writer
     */
    private static void importJsonFile(String type, String action, Path file, IndexWriter i_writer, TaxonomyWriter t_writer) {
        try {
            long ct = System.currentTimeMillis();
            QueueTask task = new QueueTask();
            task.setType(type);
            task.setAction(action);
            String json = Files.readAllLines(file).stream().collect(Collectors.joining());
            task.setBody(json);
            task.write(i_writer, t_writer);
            log.info("{} imported in {}ms. ({})", file.toString(), (System.currentTimeMillis() - ct), Thread.currentThread().getName());

        } catch (IllegalArgumentException e) {
            //This is an jcseg & lucene bug, just retry to avoid this bug
            log.error("Retry to import file: " + file.toString(), e);
            //java.lang.IllegalArgumentException: startOffset must be non-negative, and endOffset must be >= startOffset
            importJsonFile(type, action, file, i_writer, t_writer);
        } catch (Exception e) {
            log.error("Failed to import file: " + file.toString(), e);
        }
    }

    private static void printHelp() {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(110);
        hf.printHelp("gsimport", options, true);
    }

}
