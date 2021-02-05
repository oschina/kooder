package com.gitee.kooder.gitee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gitee.kooder.core.GiteeSearchConfig;
import com.gitee.kooder.utils.HttpUtils;
import com.gitee.kooder.utils.JsonUtils;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * gitee open api
 *
 * @author zhanggx
 */
public class GiteeApi {

    private final static Logger log = LoggerFactory.getLogger(GiteeApi.class);

    private static final String URL_GET_ENTERPRISE = "/api/v5/enterprises/enterprise";
    private static final String URL_CREATE_ENTERPRISE_HOOKS = "/api/v5/enterprises/enterprise/hooks/create";
    private static final String URL_GET_ENTERPRISE_HOOKS = "/api/v5/enterprises/enterprise/hooks";
    private static final String URL_GET_REPOS = "/api/v5/enterprises/enterprise/repos";
    private static final String URL_GET_ISSUES = "/api/v5/enterprises/enterprise/issues";

    /**
     * gitee server url
     */
    public final String giteeUrl;
    /**
     * personal access token
     */
    private final String personalAccessToken;

    private GiteeApi() {
        giteeUrl = GiteeSearchConfig.getProperty("gitee.url");
        personalAccessToken = GiteeSearchConfig.getProperty("gitee.personal_access_token");
    }

    public static GiteeApi getInstance() {
        return Singleton.INSTANCE;
    }

    /**
     * get enterprise info
     *
     * @return
     */
    public Enterprise getEnterprise() throws GiteeException {
        Map<String, String> requestParamsMap = getRequestParamsMap();
        try (Response response = HttpUtils.get(giteeUrl + URL_GET_ENTERPRISE, requestParamsMap)) {
            if (response.isSuccessful()) {
                return JsonUtils.readValue(response.body().string(), Enterprise.class);
            }
            throw new GiteeException(response.body().string());
        } catch (Exception e) {
            log.warn("Create gitee enterprise hooks error: {}", e.getMessage());
            throw new GiteeException(e.getMessage());
        }
    }

    /**
     * create enterprise hook
     *
     * @param url                 enterprise hook url
     * @param secretToken         enterprise hook password
     * @param pushEvents          notice push event
     * @param repoEvents          notice repo event
     * @param tagPushEvents       notice tag push event
     * @param issuesEvents        notice issue event
     * @param noteEvents          notice note event
     * @param mergeRequestsEvents notice merge request event
     * @throws GiteeException request gitee server error
     */
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

    /**
     * get all enterprise hook
     *
     * @return all enterprise hook
     * @throws GiteeException
     */
    public List<EnterpriseHook> getEnterpriseHooks() throws GiteeException {
        int pageNo = 1, pageSize = 50;
        List<EnterpriseHook> res = new ArrayList<>();
        List<EnterpriseHook> enterpriseHookList;
        do {
            res.addAll(enterpriseHookList = getEnterpriseHooks(pageNo++, pageSize));
        } while (enterpriseHookList.size() == pageSize);
        return res;
    }

    /**
     * paging get enterprise hook
     *
     * @param pageNo
     * @param pageSize
     * @return
     * @throws GiteeException
     */
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

    /**
     * get all repo
     *
     * @param afterId
     * @return
     * @throws GiteeException
     */
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

    /**
     * paging get repo
     *
     * @param pageNo
     * @param pageSize
     * @return
     * @throws GiteeException
     */
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

    /**
     * get all issue
     *
     * @param afterId
     * @return
     * @throws GiteeException
     */
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

    /**
     * paging get issue
     *
     * @param pageNo
     * @param pageSize
     * @return
     * @throws GiteeException
     */
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

    /**
     * get request params map with personal access token
     *
     * @return
     */
    private Map<String, String> getRequestParamsMap() {
        Map<String, String> map = new HashMap<>();
        map.put("access_token", personalAccessToken);
        return map;
    }

    private static class Singleton {
        private static final GiteeApi INSTANCE = new GiteeApi();
    }

}
