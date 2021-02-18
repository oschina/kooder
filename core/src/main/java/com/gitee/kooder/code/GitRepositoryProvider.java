/**
 * Copyright (c) 2021, OSChina (oschina.net@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitee.kooder.code;

import com.gitee.kooder.core.GiteeSearchConfig;
import com.gitee.kooder.models.*;
import com.gitee.kooder.storage.StorageFactory;
import com.gitee.kooder.utils.FileClassifier;
import com.gitee.kooder.utils.SlocCounter;
import com.gitee.kooder.utils.TextFileUtils;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.InvalidObjectIdException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Git 仓库源
 * @author Winter Lau<javayou@gmail.com>
 */
public class GitRepositoryProvider implements RepositoryProvider {

    private final static Logger log = LoggerFactory.getLogger("[GIT]");

    private final static SlocCounter slocCounter = new SlocCounter();
    private CredentialsProvider credentialsProvider;
    private TransportConfigCallback transportConfigCallback;

    public GitRepositoryProvider() {
        //http authenticator
        String username = GiteeSearchConfig.getProperty("git.username");
        String password = GiteeSearchConfig.getProperty("git.password");
        if(StringUtils.isBlank(password)) {
            password = GiteeSearchConfig.getProperty("gitlab.personal_access_token");
            if(StringUtils.isBlank(password))
                password = GiteeSearchConfig.getProperty("gitee.personal_access_token");
        }
        if(StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password))
            this.credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);

        //ssh authenticator
        String sshkey   = GiteeSearchConfig.getProperty("git.ssh.key");     //私钥文件
        String keypass  = GiteeSearchConfig.getProperty("git.ssh.keypass"); //密钥对应的密码
        if(StringUtils.isNotBlank(sshkey)) {
            this.transportConfigCallback = new TransportConfigCallback() {
                @Override
                public void configure(Transport transport) {
                    if(transport instanceof SshTransport) {
                        SshTransport sshTransport = (SshTransport) transport;
                        sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {
                            @Override
                            protected JSch createDefaultJSch(FS fs) throws JSchException {
                                JSch defaultJSch = super.createDefaultJSch(fs);
                                String keypath = GiteeSearchConfig.getPath(sshkey).toString();
                                if (StringUtils.isNotBlank(keypass))
                                    defaultJSch.addIdentity(keypath, keypass.trim());
                                else
                                    defaultJSch.addIdentity(keypath);
                                return defaultJSch;
                            }
                        });
                    }
                }
            };
        }
    }

    @Override
    public String name() {
        return "git";
    }

    /**
     * 更新仓库
     * @param repo
     * @return 返回索引的文件数
     */
    @Override
    public int pull(CodeRepository repo, FileTraveler traveler) {
        Git git = null;
        try {
            long ct = System.currentTimeMillis();
            File repoFile = StorageFactory.getRepositoryPath(repo.getRelativePath()).toFile();
            if (!repoFile.exists()) {//检查目录不存在就 clone
                git = justClone(repo.getUrl(), repoFile);
                log.info("Repository '{}:{}' no exists, re-clone from '{}' in {}ms",
                        repo.getId(), repo.getName(), repo.getUrl(), System.currentTimeMillis() - ct);
            } else {//目录存在就 pull，如果使用 bare 模式克隆仓库，对应的是 git fetch
                git = Git.open(repoFile);
                FetchCommand fetchCmd = git.fetch();
                List<RemoteConfig> remotes = git.remoteList().call();
                boolean needReClone = false;
                for (RemoteConfig remote : remotes) {
                    if (remote.getName().equals(fetchCmd.getRemote())) {
                        if (remote.getURIs().get(0).toString().equals(repo.getUrl())) {
                            //remote url no changed, just fetch it
                            this.autoSetCredential(fetchCmd);
                            fetchCmd.call();
                            log.info("Repository '{}:{}' pulled from '{}' in {}ms",
                                    repo.getId(), repo.getName(), repo.getUrl(), System.currentTimeMillis() - ct);
                            break;
                        } else
                            needReClone = true;
                    }
                }
                if (needReClone) {//仓库地址变成另外一个不相关的仓库时候重新克隆？
                    git.close();
                    FileUtils.forceDelete(repoFile);
                    git = justClone(repo.getUrl(), repoFile);
                    log.info("Repository '{}:{}' mismatch local objects, re-clone from '{}' in {}ms",
                            repo.getId(), repo.getName(), repo.getUrl(), System.currentTimeMillis() - ct);
                }
                //fetchCmd.setForceUpdate(true);
            }

            boolean needRebuildIndexes = true;
            ObjectId oldId = null;
            if (StringUtils.isNotBlank(repo.getLastCommitId())) {
                //Check last commit ref
                try (RevWalk revWalk = new RevWalk(git.getRepository())) {
                    revWalk.setRetainBody(false);
                    oldId = ObjectId.fromString(repo.getLastCommitId());
                    revWalk.parseCommit(oldId); //check if last commit id exists
                    revWalk.dispose();
                    needRebuildIndexes = false;
                } catch (InvalidObjectIdException | MissingObjectException e) {
                }
            }

            repo.saveStatus(CodeRepository.STATUS_FETCH);

            if (needRebuildIndexes) {
                long cti = System.currentTimeMillis();
                if (traveler != null)
                    traveler.resetRepository(repo.getId());
                //上一次保持的 commit id 已经失效，可能是强推导致，需要重建仓库索引
                int fc = this.indexAllFiles(repo, git, traveler);
                log.info("Rebuilding '{}<{}>' {} indexes in {}ms", repo.getName(), repo.getId(), fc, System.currentTimeMillis() - cti);
                return fc;
            }

            int fileCount = 0;
            Ref newHeadRef = git.getRepository().findRef(Constants.HEAD);
            ObjectId newId = newHeadRef.getObjectId();
            repo.setLastCommitId(newId.name()); //保存仓库最新提交信息

            if (!oldId.toString().equals(newId.toString())) {
                List<DiffEntry> entries = diffFiles(git, oldId.name(), newId.name());
                for (DiffEntry entry : entries) {
                    if (entry.getChangeType() == DiffEntry.ChangeType.DELETE && traveler != null) {
                        SourceFile doc = new SourceFile(repo.getId(), repo.getName(), entry.getOldPath());
                        traveler.deleteDocument(doc);
                    } else {
                        addFileToDocument(repo, git, entry.getNewPath(), entry.getNewId().toObjectId(), traveler);
                        fileCount++;
                    }
                }
            }
            return fileCount;
        } catch (IOException | GitAPIException ex) {
            log.error("Failed to pull & index from '" + repo.getUrl() + "'", ex);
        } finally {
            if(git != null)
                git.close();
        }
        return -1;
    }

    /**
     * Auto set credential for git
     * @param command
     */
    private void autoSetCredential(TransportCommand command) {
        if(this.credentialsProvider != null)
            command.setCredentialsProvider(this.credentialsProvider);
        if(this.transportConfigCallback != null)
            command.setTransportConfigCallback(this.transportConfigCallback);
    }

    /**
     * clone git repository
     * @param fromUrl
     * @param toPath
     * @return
     * @throws GitAPIException
     */
    private Git justClone(String fromUrl, File toPath) throws GitAPIException {
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(fromUrl)
                .setDirectory(toPath)
                .setCloneAllBranches(true);
        this.autoSetCredential(cloneCommand);
        cloneCommand.setCloneSubmodules(false);
        cloneCommand.setBare(true);//只克隆 git 数据库，不克隆文件
        return cloneCommand.call();
    }

    /**
     * 重建代码仓索引
     * @param repo
     * @param git
     * @param traveler
     * @throws IOException
     * @throws GitAPIException
     */
    private int indexAllFiles(CodeRepository repo, Git git, FileTraveler traveler) throws IOException, GitAPIException {
        int fileCount = 0;
        Ref head = git.getRepository().findRef(Constants.HEAD);
        if(head != null && head.getObjectId() != null) {
            repo.setLastCommitId(head.getObjectId().name());//回调最新 commit id 信息
            RevCommit commit = git.getRepository().parseCommit(head.getObjectId());
            try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
                treeWalk.addTree(commit.getTree());
                treeWalk.setRecursive(true);
                while(treeWalk.next()) {
                    addFileToDocument(repo, git, treeWalk.getPathString(), treeWalk.getObjectId(0), traveler);
                    fileCount ++;
                }
            }
        }
        return fileCount;
    }

    /**
     * Index source file
     * @param repo
     * @param git
     * @param path
     * @param objectId
     * @param traveler
     * @throws IOException
     * @throws GitAPIException
     */
    private void addFileToDocument(CodeRepository repo, Git git, String path, ObjectId objectId, FileTraveler traveler)
            throws IOException, GitAPIException
    {
        boolean isBinaryFile = TextFileUtils.isBinaryFile(path);
        if(!isBinaryFile) { //Text file
            SourceFile doc = buildDocument(repo, git, path, objectId);
            if (doc != null && traveler != null) {
                traveler.updateDocument(doc);
            }
        }
        else { // Binary file
            SourceFile doc = buildBinaryDocument(repo, git, path, objectId);
            traveler.updateDocument(doc);
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
    public void delete(CodeRepository repo) {
        try {
            Path path = StorageFactory.getRepositoryPath(repo.getRelativePath());
            FileUtils.forceDelete(path.toFile());
        } catch (IOException e) {
            log.error("Failed to delete repo: " + repo.getRelativePath(), e);
        }
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
    private SourceFile buildDocument(CodeRepository repo, Git git, String path, ObjectId objectId)
            throws IOException, GitAPIException
    {
        ObjectLoader loader = git.getRepository().open(objectId);
        try(InputStream stream = loader.openStream()) {
            List<String> codeLines = TextFileUtils.readFileLines(stream, 20000);
            String contents = String.join("\n", codeLines);

            SourceFile doc = new SourceFile();
            doc.setRepository(new Relation(repo.getId(), repo.getName(), repo.getUrl()));
            doc.setBranch(git.getRepository().getBranch());
            doc.setName(FilenameUtils.getName(path));                       //文件名
            doc.setLocation(path);
            doc.setLanguage(FileClassifier.languageGuess(path, contents));  //语言
            doc.setContents(contents);                                      //源码
            doc.setCodeOwner(getCodeOwner(git, path));                      //开发者  TODO 如何能支持多个开发者
            SlocCounter.SlocCount slocCount = slocCounter.countStats(contents, doc.getLanguage());
            doc.setLines(slocCount.linesCount);                             //代码行统计
            doc.setCommentLines(slocCount.commentCount);
            doc.setBlankLines(slocCount.blankCount);
            doc.setCodeLines(slocCount.codeCount);
            doc.setComplexity(slocCount.complexity);
            doc.setHash(DigestUtils.sha1Hex(contents));
            doc.setRevision(objectId.name());

            doc.generateUuid(); //calculate file uuid
            doc.generateUrl();  // calculate file url

            return doc;
        }
    }

    /**
     * 从二进制文件中构建文档
     * @param repo
     * @param git
     * @param path
     * @param objectId
     * @return
     * @throws IOException
     */
    private SourceFile buildBinaryDocument(CodeRepository repo, Git git, String path, ObjectId objectId) throws IOException {
        SourceFile doc = new SourceFile();

        doc.setRepository(new Relation(repo.getId(), repo.getName(), repo.getUrl()));
        doc.setBranch(git.getRepository().getBranch());
        doc.setName(FilenameUtils.getName(path));     //文件名
        doc.setLocation(path);                        //完整的项目内路径
        doc.setLanguage(FileClassifier.BINARY_LANGUAGE);  //语言
        doc.setRevision(objectId.name());

        doc.generateUuid(); //calculate file uuid
        doc.generateUrl();  // calculate file url

        return doc;
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

    /**
     * 测试仓库索引
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        GitRepositoryProvider grp = new GitRepositoryProvider();

        int id = 1000;
        String[] repos = {"https://gitee.com/ld/J2Cache",
                          "https://gitee.com/harmonyhub/dtbutton",
                          "https://gitee.com/DogGodGit/FlaxEngine",
                          "https://gitee.com/vnpy/vnpy"};

        try(
            IndexWriter writer = StorageFactory.getIndexWriter("code");
            TaxonomyWriter twriter = StorageFactory.getTaxonomyWriter("code"))
        {
            for(String repoUrl : repos) {
                CodeRepository repo = new CodeRepository();
                String repoName = repoUrl.substring(repoUrl.lastIndexOf('/')+1);
                if(id == 1000)
                    repoName = "J2Cache";
                repo.setId(id++);
                repo.setName(repoName);
                repo.setUrl(repoUrl);
                repo.setScm("git");
                //repo.setLastCommitId("b80348427425628d8dca9b60cf69af01a5005982");//2

                CodeFileTraveler traveler = new CodeFileTraveler(writer, twriter);
                grp.pull(repo, traveler);
                RepositoryManager.INSTANCE.save(repo);
            }
        }
    }
}
