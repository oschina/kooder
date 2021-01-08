package com.gitee.search.code;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.search.core.Constants;
import com.gitee.search.queue.QueueTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static com.gitee.search.utils.JsonUtils.jsonAttr;

/**
 * 各种仓库源的管理
 * @author Winter Lau<javayou@gmail.com>
 */
public class RepositoryFactory {

    private final static Logger log = LoggerFactory.getLogger(RepositoryFactory.class);

    private final static Map<String, RepositoryProvider> providers = new HashMap<>(){{
        put(CodeRepository.SCM_GIT,     new GitRepositoryProvider());
        put(CodeRepository.SCM_SVN,     new SvnRepositoryProvider());
        put(CodeRepository.SCM_FILE,    new FileRepositoryProvider());
    }};

    /**
     * 根据 scm 获取仓库操作类实例
     * @param scm
     * @return
     */
    public final static RepositoryProvider getProvider(String scm) {
        return providers.get(scm);
    }

    /**
     * 从 task 中提取仓库信息
     * @param task
     * @return
     */
    public static List<CodeRepository> getRepositoryFromTask(QueueTask task) {
        if(!task.isCodeTask())
            throw new IllegalArgumentException("Only accept QueueTask<code>");
        List<CodeRepository> codes = new ArrayList<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            Iterator<JsonNode> objects = mapper.readTree(task.getBody()).withArray(Constants.FIELD_OBJECTS).elements();
            while (objects.hasNext()) {
                JsonNode node = objects.next();
                CodeRepository code = new CodeRepository();
                code.setId(jsonAttr(node, "id", 0L));
                code.setName(jsonAttr(node,"name"));
                code.setUrl(jsonAttr(node, "url"));
                code.setScm(jsonAttr(node, "scm"));
                codes.add(code);
            }
        } catch ( IOException e ) {
            log.error("Failed to parse task body", e);
        }

        return codes;
    }

}
