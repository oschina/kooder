package com.gitee.kooder.indexer;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Build source code indexes for specify repository
 * 用于构建某个仓库源码的全量索引，主要是搜索服务的初始化阶段
 * @author Winter Lau<javayou@gmail.com>
 */
public class RepositoryIndexer {

    private Path repoPath;

    public RepositoryIndexer(Path repoPath) {
        this.repoPath = repoPath;
    }

    public void build() throws IOException, GitAPIException {
        try(Git git = Git.open(repoPath.toFile())) {
            //remotes
            git.remoteList().call().forEach(remote->{
                System.out.printf("%s -> %s\n", remote.getName(), remote.getURIs());
            });
            //tags
            List<Ref> refs = git.tagList().call();
            for(Ref ref : refs) {
                //System.out.printf("%s -> %s\n", ref.getName(), ref.getObjectId());
            }
            //files
           ObjectId lastCommitId = git.getRepository().resolve(Constants.HEAD);
            try(TreeWalk allFiles = new TreeWalk(git.getRepository())) {
                RevCommit commit = git.getRepository().parseCommit(lastCommitId);
                travelTree(git.getRepository(), commit, true);
            }

            //commits
            Iterable<RevCommit> logs = git.log().setMaxCount(10).call();
            for(RevCommit commit : logs) {
                //commit related files
                TreeWalk tw = new TreeWalk(git.getRepository());
                tw.setRecursive(true);
                tw.addTree(commit.getTree());

                for (RevCommit parent : commit.getParents())
                    tw.addTree(parent.getTree());

                while (tw.next()) {
                    int similarParents = 0;
                    for (int i = 1; i < tw.getTreeCount(); i++)
                        if (tw.getFileMode(i) == tw.getFileMode(0) && tw.getObjectId(0).equals(tw.getObjectId(i)))
                            similarParents ++;
                    if (similarParents == 0) {
                        System.out.printf("%s -> %s : %s\n",
                                commit.getName(),
                                commit.getAuthorIdent(),
                                commit.getShortMessage());
                        System.out.printf("\t%s -> depth:%d\n", tw.getPathString(), tw.getDepth());
                    }
                }
            }
        }
    }

    /**
     * 遍历文件
     * @param repo
     * @param commit
     * @param all
     * @throws IOException
     */
    private static void travelTree(Repository repo, RevCommit commit, boolean all) throws IOException {
       // now try to find a specific file
       try (TreeWalk treeWalk = new TreeWalk(repo)) {
           treeWalk.addTree(commit.getTree());
           treeWalk.setRecursive(true);
           while(treeWalk.next()) {
               System.out.println(treeWalk.getPathString());
               if("README.md".equals(treeWalk.getNameString())) {
                   ObjectId objectId = treeWalk.getObjectId(0);
                   ObjectLoader loader = repo.open(objectId);
                   // and then one can the loader to read the file
                   loader.copyTo(System.out);
               }
           }
       }
    }

    public static void main(String[] args) throws Exception {
        RepositoryIndexer ri = new RepositoryIndexer(Paths.get("D:\\WORKDIR\\J2Cache\\.git"));
        long ct = System.currentTimeMillis();
        ri.build();
        System.out.printf("%dms", System.currentTimeMillis()-ct);
    }
}
