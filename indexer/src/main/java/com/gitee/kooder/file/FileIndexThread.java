package com.gitee.kooder.file;

import com.gitee.kooder.core.Constants;
import com.gitee.kooder.core.KooderConfig;
import com.gitee.kooder.models.CodeRepository;
import com.gitee.kooder.models.Repository;
import com.gitee.kooder.queue.QueueTask;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * index from file
 *
 * @author zhanggx
 */
public class FileIndexThread extends Thread {

    private final static Logger log = LoggerFactory.getLogger(FileIndexThread.class);

    @Override
    public void run() {
        String filePath = KooderConfig.getProperty("file.index.path");
        String vender = KooderConfig.getProperty("file.index.vender");

        if (StringUtils.isBlank(vender))
            vender = "gitee";

        long start = System.currentTimeMillis();
        AtomicLong id = new AtomicLong(start);
        AtomicInteger success = new AtomicInteger(), error = new AtomicInteger();
        try {
            Files.lines(Paths.get(filePath))
                    .filter(StringUtils::isNotBlank)
                    .forEach(repoUrl -> {
                        try {
                            Repository repo = new Repository();
                            repo.setId(id.getAndIncrement());
                            repo.setName(repoUrl.substring(repoUrl.lastIndexOf("/") + 1, repoUrl.lastIndexOf(".git")));
                            repo.setUrl(repoUrl);
                            QueueTask.add(Constants.TYPE_REPOSITORY, repo);
                            CodeRepository codes = new CodeRepository();
                            codes.setId(repo.getId());
                            codes.setScm(CodeRepository.SCM_GIT);
                            codes.setName(repo.getName());
                            codes.setUrl(repo.getUrl());
                            codes.setVender(vender);
                            QueueTask.add(Constants.TYPE_CODE, codes);
                            success.incrementAndGet();
                        } catch (Exception e) {
                            error.incrementAndGet();
                            log.warn("{} index error:", repoUrl, e.getMessage());
                        }
                    });
            log.info(
                    "index from file: {} repositories indexed, success: {}, error: {}, using {} ms",
                    success.get() + error.get(),
                    success.get(),
                    error.get(),
                    System.currentTimeMillis() - start
            );
        } catch (IOException e) {
            log.error("fail to index from file", e);
        }

    }

}
