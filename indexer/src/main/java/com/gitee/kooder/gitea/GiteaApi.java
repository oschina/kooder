package com.gitee.kooder.gitea;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gitee.kooder.core.KooderConfig;
import com.gitee.kooder.utils.HttpUtils;
import com.gitee.kooder.utils.JsonUtils;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author zhanggx
 */
public class GiteaApi {

    private final static Logger log = LoggerFactory.getLogger(GiteaApi.class);

    private static final String URL_LIST_USER_REPOSITORY = "/api/v1/user/repos";
    private static final String URL_LIST_REPOSITORY_ISSUE = "/api/v1/repos/%s/%s/issues";

    /**
     * gitea server url
     */
    public final String giteaUrl;
    /**
     * personal access token
     */
    private final String personalAccessToken;

    private GiteaApi() {
        giteaUrl = KooderConfig.getProperty("gitea.url");
        personalAccessToken = KooderConfig.getProperty("gitea.personal_access_token");
    }

    public static GiteaApi getInstance() {
        return GiteaApi.Singleton.INSTANCE;
    }

    /**
     * get all repo
     *
     * @param afterId
     * @return
     * @throws GiteaException
     */
    public List<Repository> listUserRepository(int afterId) throws GiteaException {
        int pageNo = 1, pageSize = 50;
        List<Repository> res = new ArrayList<>();
        List<Repository> repositoryList;
        do {
            repositoryList = listUserRepository(pageNo++, pageSize);
            for (Repository repository : repositoryList) {
                if (repository.getId() > afterId) {
                    res.add(repository);
                }
            }
        } while (repositoryList.size() == pageSize);
        return res;
    }

    /**
     * paging get all repo
     *
     * @param pageNo
     * @param pageSize
     * @return
     * @throws GiteaException
     */
    private List<Repository> listUserRepository(int pageNo, int pageSize) throws GiteaException {
        Map<String, String> requestParamsMap = getRequestParamsMap();
        requestParamsMap.put("page", String.valueOf(pageNo));
        requestParamsMap.put("limit", String.valueOf(pageSize));
        try (Response response = HttpUtils.get(giteaUrl + URL_LIST_USER_REPOSITORY, requestParamsMap)) {
            if (response.isSuccessful()) {
                List<Repository> repositoryList = JsonUtils.readValue(response.body().string(), new TypeReference<List<Repository>>() {
                });
                if (Objects.nonNull(repositoryList)) {
                    return repositoryList;
                }
            }
            throw new GiteaException(response.body().string());
        } catch (Exception e) {
            log.warn("Get gitea user repository error: {}", e.getMessage());
            throw new GiteaException(e.getMessage());
        }
    }

    /**
     * get all repository issue
     *
     * @param repository
     * @return
     * @throws GiteaException
     */
    public List<Issue> listRepositoryIssue(Repository repository, int afterId) throws GiteaException {
        int pageNo = 1, pageSize = 50;
        List<Issue> res = new ArrayList<>();
        List<Issue> issueList;
        do {
            issueList = listRepositoryIssue(repository.getOwner().getUsername(), repository.getName(), pageNo++, pageSize);
            for (Issue issue : issueList) {
                if (issue.getId() > afterId) {
                    res.add(issue);
                }
            }
        } while (issueList.size() == pageSize);
        return res;
    }

    /**
     * paging get all repository issue
     *
     * @param pageNo
     * @param pageSize
     * @return
     * @throws GiteaException
     */
    private List<Issue> listRepositoryIssue(String owner, String repo,
                                            int pageNo, int pageSize) throws GiteaException {
        Map<String, String> requestParamsMap = getRequestParamsMap();
        requestParamsMap.put("page", String.valueOf(pageNo));
        requestParamsMap.put("limit", String.valueOf(pageSize));
        requestParamsMap.put("state", "all");
        try (Response response = HttpUtils.get(giteaUrl + String.format(URL_LIST_REPOSITORY_ISSUE, owner, repo), requestParamsMap)) {
            if (response.isSuccessful()) {
                List<Issue> issueList = JsonUtils.readValue(response.body().string(), new TypeReference<List<Issue>>() {
                });
                if (Objects.nonNull(issueList)) {
                    return issueList;
                }
            }
            throw new GiteaException(response.body().string());
        } catch (Exception e) {
            log.warn("Get gitea repository issue error: {}", e.getMessage());
            throw new GiteaException(e.getMessage());
        }
    }

    /**
     * get request params map with personal access token
     *
     * @return
     */
    private Map<String, String> getRequestParamsMap() {
        Map<String, String> map = new HashMap<>();
        map.put("token", personalAccessToken);
        return map;
    }

    private static class Singleton {
        private static final GiteaApi INSTANCE = new GiteaApi();
    }

}
