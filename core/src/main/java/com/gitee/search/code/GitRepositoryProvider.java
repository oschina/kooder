package com.gitee.search.code;

import com.gitee.search.storage.StorageFactory;
import com.gitee.search.utils.FileClassifier;
import com.gitee.search.utils.SlocCounter;
import com.gitee.search.utils.TextFileUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.InvalidObjectIdException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Git 仓库源
 * @author Winter Lau<javayou@gmail.com>
 */
public class GitRepositoryProvider implements RepositoryProvider {

    private final static Logger log = LoggerFactory.getLogger(GitRepositoryProvider.class);
    private final static SlocCounter slocCounter = new SlocCounter();

    @Override
    public String name() {
        return "git";
    }

    /**
     * 更新仓库
     * @param repo
     * @return
     */
    @Override
    public void pull(CodeRepository repo, FileTraveler traveler) {
        Git git = null;
        try {
            boolean isNewRepository = false;
            File repoFile = new File(repo.getPath());
            if(!repoFile.exists()) {//检查目录不存在就 clone
                log.info("Repository '{}:{}' no exists, re-clone from '{}'", repo.getId(), repo.getName(), repo.getUrl());
                CloneCommand cloneCommand = Git.cloneRepository()
                        .setURI(repo.getUrl())
                        .setDirectory(new File(repo.getPath()))
                        .setCloneAllBranches(true);

                if (repo.useCredentials())
                    cloneCommand.setCredentialsProvider(repo.getCredential());
                git = cloneCommand.call();
                isNewRepository = true;
            }
            else {//目录存在就 pull
                git = Git.open(repoFile);
                PullCommand pullCmd = git.pull();
                if (repo.useCredentials())
                    pullCmd.setCredentialsProvider(repo.getCredential());
                pullCmd.call();
            }

            boolean needRebuildIndexes = true;
            ObjectId oldId = null;
            if(StringUtils.isNotBlank(repo.getLastCommitId())) {
                //Check last commit ref
                try (RevWalk revWalk = new RevWalk(git.getRepository())) {
                    oldId = ObjectId.fromString(repo.getLastCommitId());
                    RevCommit commit = revWalk.parseCommit(oldId);
                    needRebuildIndexes = false;
                } catch (InvalidObjectIdException | MissingObjectException e) {
                }
            }

            if(needRebuildIndexes) {
                log.warn("Rebuilding '{}:{}' indexes", repo.getId(), repo.getName());
                traveler.resetRepository(repo.getId());
                //上一次保持的 commit id 已经失效，可能是强推导致，需要重建仓库索引
                this.indexAllFiles(repo, git, traveler);
                return ;
            }

            Ref newHeadRef = git.getRepository().findRef(Constants.HEAD);
            ObjectId newId = newHeadRef.getObjectId();

            if (!oldId.toString().equals(newId.toString())) {
                List<DiffEntry> entries = diffFiles(git, oldId.name(), newId.name());
                for (DiffEntry entry : entries) {
                    if (entry.getChangeType() == DiffEntry.ChangeType.DELETE) {
                        CodeIndexDocument doc = new CodeIndexDocument(repo.getId(), repo.getName(), entry.getOldPath());
                        traveler.deleteDocument(doc);
                    } else {
                        String path = entry.getNewPath();
                        boolean isBinaryFile = TextFileUtils.isBinaryFile(path);
                        if(!isBinaryFile) { //二进制文件不参与索引
                            CodeIndexDocument doc = buildDocument(repo, git, path, entry.getNewId().toObjectId());
                            if (doc != null) {
                                traveler.updateDocument(doc);
                            }
                        }
                    }
                }
            }
        } catch (IOException | GitAPIException ex) {
            log.error("Failed to pull & index from '" + repo.getUrl() + "'", ex);
        } finally {
            if(git != null)
                git.close();
        }
    }

    /**
     * 重建代码仓索引
     * @param repo
     * @param git
     * @param traveler
     * @throws IOException
     * @throws GitAPIException
     */
    private void indexAllFiles(CodeRepository repo, Git git, FileTraveler traveler) throws IOException, GitAPIException {
        Ref head = git.getRepository().findRef(Constants.HEAD);
        repo.setLastCommitId(head.getObjectId().name());//回调最新 commit id 信息
        RevCommit commit = git.getRepository().parseCommit(head.getObjectId());
        try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
            treeWalk.addTree(commit.getTree());
            treeWalk.setRecursive(true);
            while(treeWalk.next()) {
                String path = treeWalk.getPathString();
                boolean isBinaryFile = TextFileUtils.isBinaryFile(path);
                if(!isBinaryFile) { //二进制文件不参与索引
                    CodeIndexDocument doc = buildDocument(repo, git, path, treeWalk.getObjectId(0));
                    if (doc != null) {
                        traveler.updateDocument(doc);
                    }
                }
            }
        }
    }

    private static @NonNull
    List<DiffEntry> diffFiles(Git git, String oldCommit, String newCommit) throws IOException, GitAPIException {
        return git.diff()
                .setOldTree(prepareTreeParser(git, oldCommit))
                .setNewTree(prepareTreeParser(git, newCommit))
                .call();
    }

    private static AbstractTreeIterator prepareTreeParser(Git git, String objectId) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        //noinspection Duplicates
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            RevCommit commit = walk.parseCommit(git.getRepository().resolve(objectId));
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = git.getRepository().newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            return treeParser;
        }
    }

    /**
     * 删除仓库
     * @param repo
     */
    @Override
    public void delete(CodeRepository repo) throws IOException {
        FileUtils.forceDelete(new File(repo.getPath()));
    }

    /**
     * 从文件中构建文档
     * @param repo
     * @param git
     * @param path
     * @param objectId
     * @return
     * @throws IOException
     */
    private CodeIndexDocument buildDocument(CodeRepository repo, Git git, String path, ObjectId objectId)
            throws IOException, GitAPIException
    {
        ObjectLoader loader = git.getRepository().open(objectId);
        try(InputStream stream = loader.openStream()) {
            List<String> codeLines = TextFileUtils.readFileLines(stream, 20000);
            String contents = String.join("\n", codeLines);

            CodeIndexDocument doc = new CodeIndexDocument();

            doc.setRepoId(repo.getId());
            doc.setRepoName(repo.getName());                                    //仓库名
            doc.setRepoURL(repo.getUrl());                                      //仓库地址
            doc.setFileName(FilenameUtils.getName(path));                       //文件名
            doc.setFileLocation(path);                                          //完整的项目内路径
            doc.setLanguage(FileClassifier.languageGuess(path, contents));      //语言
            doc.setContents(contents);                                          //源码
            doc.setCodeOwner(getCodeOwner(git, path));                          //开发者  TODO 应该支持多个开发者
            var slocCount = slocCounter.countStats(contents, doc.getLanguage());
            doc.setLines(slocCount.linesCount);                                 //代码行统计
            doc.setCommentLines(slocCount.commentCount);
            doc.setBlankLines(slocCount.blankCount);
            doc.setCodeLines(slocCount.codeCount);
            doc.setComplexity(slocCount.complexity);
            doc.setSha1Hash(DigestUtils.sha1Hex(contents));
            doc.setRevision(objectId.name());
            doc.setScm(repo.getScm());

            //calculate document uuid
            doc.generateUuid();

            return doc;
        }
    }

    /**
     * 读取所有开发者信息
     * @param git
     * @param fileName
     * @return
     */
    private static String getCodeOwner(Git git, String fileName) throws IOException, GitAPIException {
        BlameCommand blamer = git.blame();
        ObjectId lastCommitId = git.getRepository().resolve(Constants.HEAD);
        // Somewhere in here appears to be wrong...
        blamer.setStartCommit(lastCommitId);
        blamer.setFilePath(fileName);
        BlameResult blame = blamer.call();

        HashMap<String, CodeOwner> owners = new HashMap<>();

        if (blame != null) {
            // Get all the owners their number of commits and most recent commit
            RevCommit commit;

            int linec = blame.getResultContents().size();
            for (int i = 0; i < linec; i++) {
                commit = blame.getSourceCommit(i);
                PersonIdent pident = commit.getAuthorIdent();
                if (owners.containsKey(pident.getName())) {
                    CodeOwner codeOwner = owners.get(pident.getName());
                    codeOwner.incrementLines();
                    int timestamp = codeOwner.getMostRecentUnixCommitTimestamp();
                    if (commit.getCommitTime() > timestamp)
                        codeOwner.setMostRecentUnixCommitTimestamp(commit.getCommitTime());
                    owners.put(pident.getName(), codeOwner);
                } else {
                    owners.put(pident.getName(), new CodeOwner(pident.getName(), 1, commit.getCommitTime()));
                }
            }
        }

        return codeOwner(owners.values());
    }

    /**
     * Determines who owns a piece of code weighted by time based on current second (IE time now)
     * NB if a commit is very close to this time it will always win
     */
    private static String codeOwner(Collection<CodeOwner> codeOwners) {
        long currentUnix = System.currentTimeMillis() / 1_000L;

        double best = 0;
        String owner = "Unknown";

        for (CodeOwner codeOwner : codeOwners) {
            double age = (currentUnix - codeOwner.getMostRecentUnixCommitTimestamp()) / 60 / 60;
            double calc = codeOwner.getNoLines() / Math.pow((age), 1.8);

            if (calc > best) {
                best = calc;
                owner = codeOwner.getName();
            }
        }

        return owner;
    }

    public static void main(String[] args) throws IOException {
        GitRepositoryProvider grp = new GitRepositoryProvider();

        CodeRepository repo = new CodeRepository();
        repo.setName("j2cache");
        repo.setUrl("https://gitee.com/ld/J2Cache");
        repo.setPath("D:\\j2cache");
        //repo.setLastCommitId("b80348427425628d8dca9b60cf69af01a5005982");//2

        try(
            IndexWriter writer = StorageFactory.getIndexWriter("code");
            TaxonomyWriter twriter = StorageFactory.getTaxonomyWriter("code")
            ) {
            CodeFileTraveler traveler = new CodeFileTraveler(writer, twriter);
            grp.pull(repo, traveler);
        }
    }
}
