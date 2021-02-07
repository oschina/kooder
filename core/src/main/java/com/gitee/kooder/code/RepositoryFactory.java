package com.gitee.kooder.code;

import com.gitee.kooder.models.CodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 各种仓库源的管理
 * @author Winter Lau<javayou@gmail.com>
 */
public class RepositoryFactory {

    private final static Logger log = LoggerFactory.getLogger(RepositoryFactory.class);

    private final static Map<String, RepositoryProvider> providers = new HashMap(){{
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

}
