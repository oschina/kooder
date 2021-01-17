package com.gitee.search.indexer;

import com.gitee.search.models.Issue;
import com.gitee.search.models.Repository;
import org.apache.commons.cli.*;
import org.gitlab4j.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 初始导入 Gitlab 数据
 * 3diuse7VS4xcx-m_Ny8y
 * 使用方法： gitlabimporter [options] url
 * url gitlab api 地址
 * 运行参数：
 * -u 用户名
 * -p 密码
 * -t access token
 * @author Winter Lau<javayou@gmail.com>
 */
public class GitlabImporter {

    private final static Logger log = LoggerFactory.getLogger(GitlabImporter.class);
    private final static int itemsPerPage = 10;
    private final static Options options = new Options(){{
        addOption("pt",true, "personal access token");
        addOption("st",false, "secert token");
        addOption("v", "version",   false, "gitlab api version [3|4], default 4");
        addOption("h", "help",      false, "print help");
    }};

    public static void main(String[] args) throws Exception {
        long ct = System.currentTimeMillis();
        GitLabApi gitlab = connectToGitlab(args);
        int pc = indexAllProjects(gitlab);
        int ic = indexAllIssues(gitlab);
        log.info("{} projects and {} issues indexed in {}s", pc, ic, (System.currentTimeMillis() - ct) / 1000);
    }

    /**
     * Index all projects and it source codes
     * @param gitlab
     * @throws GitLabApiException
     */
    private static int indexAllProjects(GitLabApi gitlab) throws IOException, GitLabApiException {
        int pc = 0;
        Pager<org.gitlab4j.api.models.Project> projects = gitlab.getProjectApi().getProjects(itemsPerPage);
        while(projects.hasNext()) {
            for(org.gitlab4j.api.models.Project p : projects.next()) {
                indexProject(gitlab, p);
                pc ++;
            }
        }
        return pc;
    }

    /**
     * Index all issues and it source codes
     * @param gitlab
     * @throws GitLabApiException
     */
    private static int indexAllIssues(GitLabApi gitlab) throws IOException, GitLabApiException {
        int ic = 0;
        Pager<org.gitlab4j.api.models.Issue> issues = gitlab.getIssuesApi().getIssues(itemsPerPage);
        while(issues.hasNext()) {
            for(org.gitlab4j.api.models.Issue i : issues.next()) {
                Issue issue = new Issue(i);
                //TODO 处理 issue 索引
                ic ++;
            }
        }
        return ic;
    }

    /**
     * index single project
     * @param gitlab
     * @param project
     * @throws GitLabApiException
     */
    private static void indexProject(GitLabApi gitlab, org.gitlab4j.api.models.Project project) throws IOException, GitLabApiException {
        Repository p = new Repository(project);
        Map<String, Float> langs = gitlab.getProjectApi().getProjectLanguages(p.getId());
        //TODO 处理仓库索引
        indexProjectCodes(gitlab, project);
    }

    /**
     * index project codes
     * @param gitlab
     * @param project
     * @throws GitLabApiException
     */
    private static void indexProjectCodes(GitLabApi gitlab, org.gitlab4j.api.models.Project project) throws GitLabApiException {
        if(project.getEmptyRepo())
            return ;
        //TODO 处理仓库的代码索引
    }

    /**
     * Connect to gitlab
     * @param args
     * @return
     */
    private static GitLabApi connectToGitlab(String[] args) throws GitLabApiException {
        CommandLine cmd = null;
        CommandLineParser parser = new DefaultParser();
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            printHelp();
            return null;
        }
        String[] urls = cmd.getArgs();
        if (cmd.hasOption("h")) {
            printHelp();
            return null;
        }

        List<String> arglist = Arrays.asList(cmd.getOptions()).stream().map(o -> o.getValue()).collect(Collectors.toList());

        String gitlab_api_url = null;

        for(String a : cmd.getArgs()) {
            if(!arglist.contains(a))
                gitlab_api_url = a;
        }

        if(gitlab_api_url == null) {
            printHelp();
            return null;
        }

        String ptoken = cmd.getOptionValue("pt");
        String version = cmd.getOptionValue("v", "4");
        GitLabApi gitlab = new GitLabApi(version.equals("4") ? GitLabApi.ApiVersion.V4 : GitLabApi.ApiVersion.V3, gitlab_api_url, ptoken);
        // Set the connect timeout to 1 second and the read timeout to 5 seconds
        gitlab.setRequestTimeout(1000, 5000);
        log.info("Connected to GitLab {} at {}" , gitlab.getVersion().getVersion(), gitlab_api_url);
        return gitlab;
    }

    /**
     * Print usage
     */
    private static void printHelp() {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("glimport [OPTIONS] <gitlab-api-url>", "[OPTIONS]", options, "");
    }

}
