package com.gitee.kooder.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gitee.kooder.core.GiteeSearchConfig;
import com.gitee.kooder.exception.GiteeException;
import com.gitee.kooder.models.gitee.EnterpriseHook;
import com.gitee.kooder.models.gitee.Issue;
import com.gitee.kooder.models.gitee.Repository;
import com.gitee.kooder.utils.HttpUtils;
import com.gitee.kooder.utils.JsonUtils;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author zhanggx
 */
public class GiteeApi {

    private final static Logger log = LoggerFactory.getLogger(GiteeApi.class);

    private static final String URL_CREATE_ENTERPRISE_HOOKS = "/api/v5/enterprises/enterprise/hooks/create";
    private static final String URL_GET_ENTERPRISE_HOOKS = "/api/v5/enterprises/enterprise/hooks";
    private static final String URL_GET_REPOS = "/api/v5/enterprises/enterprise/repos";
    private static final String URL_GET_ISSUES = "/api/v5/enterprises/enterprise/issues";

    public final String giteeUrl;
    private final String presonalAccessToken;

    private GiteeApi() {
        giteeUrl = GiteeSearchConfig.getProperty("gitee.url");
        presonalAccessToken = GiteeSearchConfig.getProperty("gitee.personal_access_token");
    }

    public static GiteeApi getInstance() {
        return Singleton.INSTANCE;
    }

    public void createEnterpriseHooks(String url,
                                      String secretToken,
                                      boolean pushEvents,
                                      boolean repoEvents,
                                      boolean tagPushEvents,
                                      boolean issuesEvents,
                                      boolean noteEvents,
                                      boolean mergeRequestsEvents) throws GiteeException {
        Map<String, String> requestParamsMap = getRequestParamsMap();
        requestParamsMap.put("url", url);
        requestParamsMap.put("password", secretToken);
        requestParamsMap.put("push_events", String.valueOf(pushEvents));
        requestParamsMap.put("repo_events", String.valueOf(repoEvents));
        requestParamsMap.put("tag_push_events", String.valueOf(tagPushEvents));
        requestParamsMap.put("issues_events", String.valueOf(issuesEvents));
        requestParamsMap.put("note_events", String.valueOf(noteEvents));
        requestParamsMap.put("merge_requests_events", String.valueOf(mergeRequestsEvents));
        try (Response response = HttpUtils.postJson(giteeUrl + URL_CREATE_ENTERPRISE_HOOKS, requestParamsMap)) {
            if (!response.isSuccessful()) {
                throw new GiteeException(response.body().string());
            }
        } catch (Exception e) {
            log.warn("Create gitee enterprise hooks error: {}", e.getMessage());
            throw new GiteeException(e.getMessage());
        }
    }

    public List<EnterpriseHook> getEnterpriseHooks() throws GiteeException {
        int pageNo = 1, pageSize = 50;
        List<EnterpriseHook> res = new ArrayList<>();
        List<EnterpriseHook> enterpriseHookList;
        do {
            res.addAll(enterpriseHookList = getEnterpriseHooks(pageNo++, pageSize));
        } while (enterpriseHookList.size() == pageSize);
        return res;
    }

    private List<EnterpriseHook> getEnterpriseHooks(int pageNo, int pageSize) throws GiteeException {
        Map<String, String> requestParamsMap = getRequestParamsMap();
        requestParamsMap.put("page", String.valueOf(pageNo));
        requestParamsMap.put("per_page", String.valueOf(pageSize));
        try (Response response = HttpUtils.get(giteeUrl + URL_GET_ENTERPRISE_HOOKS, requestParamsMap)) {
            if (response.isSuccessful()) {
                List<EnterpriseHook> enterpriseHookList = JsonUtils.readValue(response.body().string(), new TypeReference<List<EnterpriseHook>>() {
                });
                if (Objects.nonNull(enterpriseHookList)) {
                    return enterpriseHookList;
                }
            }
            throw new GiteeException(response.body().string());
        } catch (Exception e) {
            log.warn("Get gitt enterprise hooks error: {}", e.getMessage());
            throw new GiteeException(e.getMessage());
        }
    }

    public List<Repository> getRepos(int afterId) throws GiteeException {
        int pageNo = 1, pageSize = 50;
        List<Repository> res = new ArrayList<>();
        List<Repository> repositoryList;
        do {
            repositoryList = getRepos(pageNo++, pageSize);
            for (Repository repository : repositoryList) {
                if (repository.getId() > afterId) {
                    res.add(repository);
                }
            }
        } while (repositoryList.size() == pageSize);
        return res;
    }

    private List<Repository> getRepos(int pageNo, int pageSize) throws GiteeException {
        Map<String, String> requestParamsMap = getRequestParamsMap();
        requestParamsMap.put("type", "all");
        requestParamsMap.put("direct", "false");
        requestParamsMap.put("page", String.valueOf(pageNo));
        requestParamsMap.put("per_page", String.valueOf(pageSize));
        try (Response response = HttpUtils.get(giteeUrl + URL_GET_REPOS, requestParamsMap)) {
            if (response.isSuccessful()) {
                List<Repository> repositoryList = JsonUtils.readValue(response.body().string(), new TypeReference<List<Repository>>() {
                });
                if (Objects.nonNull(repositoryList)) {
                    return repositoryList;
                }
            }
            throw new GiteeException(response.body().string());
        } catch (Exception e) {
            log.warn("Get gitee repositorys error: {}", e.getMessage());
            throw new GiteeException(e.getMessage());
        }
    }

    public List<Issue> getIssues(int afterId) throws GiteeException {
        int pageNo = 1, pageSize = 50;
        List<Issue> res = new ArrayList<>();
        List<Issue> issueList;
        do {
            issueList = getIssues(pageNo++, pageSize);
            for (Issue issue : issueList) {
                if (issue.getId() > afterId) {
                    res.add(issue);
                }
            }
        } while (issueList.size() == pageSize);
        return res;
    }

    private List<Issue> getIssues(int pageNo, int pageSize) throws GiteeException {
        Map<String, String> requestParamsMap = getRequestParamsMap();
        requestParamsMap.put("state", "all");
        requestParamsMap.put("direction", "asc");
        requestParamsMap.put("page", String.valueOf(pageNo));
        requestParamsMap.put("per_page", String.valueOf(pageSize));
        try (Response response = HttpUtils.get(giteeUrl + URL_GET_ISSUES, requestParamsMap)) {
            if (response.isSuccessful()) {
                List<Issue> repositoryList = JsonUtils.readValue(response.body().string(), new TypeReference<List<Issue>>() {
                });
                if (Objects.nonNull(repositoryList)) {
                    return repositoryList;
                }
            }
            throw new GiteeException(response.body().string());
        } catch (Exception e) {
            log.warn("Get gitee issues error: {}", e.getMessage());
            throw new GiteeException(e.getMessage());
        }
    }

    private Map<String, String> getRequestParamsMap() {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", presonalAccessToken);
        return map;
    }

    private static class Singleton {
        private static final GiteeApi INSTANCE = new GiteeApi();
    }

}
