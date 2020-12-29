package com.gitee.search.code;

import com.gitee.search.utils.TextFileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Git 仓库源
 * @author Winter Lau<javayou@gmail.com>
 */
public class GitRepositoryProvider implements RepositoryProvider {

    private final static Logger log = LoggerFactory.getLogger(GitRepositoryProvider.class);

    @Override
    public String name() {
        return "git";
    }

    /**
     * 遍历所有源码文件
     * @param repo
     * @param traveler
     */
    @Override
    public void travel(Repository repo, RepositoryChanged changed, FileTraveler traveler) {
        try (Git git = Git.open(new File(repo.getPath()))){
            ObjectId lastCommitId = git.getRepository().resolve(Constants.HEAD);
            RevCommit commit = git.getRepository().parseCommit(lastCommitId);
            try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
                treeWalk.addTree(commit.getTree());
                treeWalk.setRecursive(true);
                while(treeWalk.next()) {
                    String filePath = treeWalk.getPathString();
                    System.out.println(treeWalk.getPathString());
                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = git.getRepository().open(objectId);
                    // and then one can the loader to read the file
                    /*
                    var codeIndexDocument = new CodeIndexDocument()
                            .setRepoLocationRepoNameLocationFilename(fileToString)
                            .setRepoName(this.repoResult.getName())
                            .setFileName(treeWalk.getNameString())
                            .setFileLocation(fileLocation)
                            .setFileLocationFilename(fileLocationFilename)
                            .setMd5hash(md5Hash)
                            .setLanguageName(languageName)
                            .setCodeLines(slocCount.codeCount)
                            .setBlankLines(slocCount.blankCount)
                            .setCommentLines(slocCount.commentCount)
                            .setLines(slocCount.linesCount)
                            .setComplexity(slocCount.complexity)
                            .setContents(StringUtils.join(codeLinesReturn.getCodeLines(), "\n"))
                            .setRepoRemoteLocation(repoRemoteLocation)
                            .setCodeOwner(codeOwner)
                            .setSchash(Values.EMPTYSTRING)
                            .setDisplayLocation(displayLocation)
                            .setSource(this.repoResult.getData().source);

                    traveler.newDocument(codeIndexDocument, !TextFileUtils.isBinaryFile(filePath));
                    */
                }
            }
        } catch (IOException e) {
            log.error("Failed to clone '" + repo.getUrl() + "'", e);
        }
    }

    /**
     * TODO: 为源码文件生成文档
     * @param file
     * @param isBinaryFile
     * @return
     */
    public CodeIndexDocument fileToDocument(File file, boolean isBinaryFile) {
        if(isBinaryFile)
            return null;
        try {
            List<String> lines = TextFileUtils.readFileLines(file, 20000);
            if(lines.isEmpty())
                return null;
            CodeIndexDocument doc = new CodeIndexDocument();

            return doc;
        } catch (IOException e) {
            log.error("Failed to read file contents of '" + file.getAbsolutePath() + "'", e);
        }
        return null;
    }

    /**
     * 将仓库克隆到指定目录
     * @param repo
     * @return
     */
    @Override
    public RepositoryChanged clone(Repository repo) {
        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(repo.getUrl());
        cloneCommand.setDirectory(new File(repo.getPath()));
        cloneCommand.setCloneAllBranches(true);

        boolean successful = false;

        if (repo.useCredentials())
            cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(repo.getUsername(), repo.getPassword()));
        cloneCommand.setProgressMonitor(new TextProgressMonitor());

        try (Git call = cloneCommand.call()){
            Ref head = call.getRepository().findRef(Constants.HEAD);
            //TODO save last head
            head.getObjectId().getName();
            successful = true;
        } catch (GitAPIException e) {
            log.error("Failed to clone '" + repo.getUrl() + "'", e);
        } catch (IOException e) {
            log.error("Failed to clone '" + repo.getUrl() + "'", e);
        }
        RepositoryChanged repositoryChanged = new RepositoryChanged(successful);
        repositoryChanged.setClone(true);
        return repositoryChanged;
    }

    /**
     * 更新仓库
     * @param repo
     * @return
     */
    @Override
    public RepositoryChanged pull(Repository repo) {
        boolean changed = false;
        List<String> changedFiles = new ArrayList<>();
        List<String> deletedFiles = new ArrayList<>();

        try (Git git = Git.open(new File(repo.getPath()))){

            Ref oldHeadRef = git.getRepository().findRef(Constants.HEAD);
            git.reset();
            git.clean();

            PullCommand pullCmd = git.pull();
            if (repo.useCredentials())
                pullCmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider(repo.getUsername(), repo.getPassword()));
            pullCmd.call();

            Ref newHeadRef = git.getRepository().findRef(Constants.HEAD);
            ObjectId oldId = oldHeadRef.getObjectId();
            ObjectId newId = newHeadRef.getObjectId();

            if (!oldId.toString().equals(newId.toString())) {
                changed = true;
                // Get the differences between the the heads which we updated at
                // and use these to just update the differences between them
                ObjectId oldHead = git.getRepository().resolve(oldId.getName() + "^{tree}");
                ObjectId newHead = git.getRepository().resolve(newId.getName() + "^{tree}");

                ObjectReader reader = git.getRepository().newObjectReader();

                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                oldTreeIter.reset(reader, oldHead);

                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                newTreeIter.reset(reader, newHead);

                List<DiffEntry> entries = git.diff()
                        .setNewTree(newTreeIter)
                        .setOldTree(oldTreeIter)
                        .call();

                for (DiffEntry entry : entries) {
                    if ("DELETE".equals(entry.getChangeType().name())) {
                        deletedFiles.add(FilenameUtils.separatorsToUnix(entry.getOldPath()));
                    } else {
                        changedFiles.add(FilenameUtils.separatorsToUnix(entry.getNewPath()));
                    }
                }
            }

        } catch (IOException | GitAPIException ex) {
            log.error("Failed to pull from '" + repo.getUrl() + "'", ex);

        }

        return new RepositoryChanged(changed, changedFiles, deletedFiles);
    }

    /**
     * 读取所有开发者信息
     * @param repo
     * @param fileName
     * @return
     */
    @Override
    public List<CodeOwner> codeOwners(Repository repo, String fileName) {
        List<CodeOwner> codeOwners = null;
        try (Git git = Git.open(new File(repo.getPath()))){
            BlameCommand blamer = git.blame();
            ObjectId lastCommitId = git.getRepository().resolve(Constants.HEAD);
            if(lastCommitId == null){
                log.error("Unabled to resolve HEAD of " + repo.getPath() + " ,Filename = " + fileName);
                return codeOwners;
            }
            // Somewhere in here appears to be wrong...
            blamer.setStartCommit(lastCommitId);
            blamer.setFilePath(fileName);
            BlameResult blame = blamer.call();

            if (blame != null) {
                // Get all the owners their number of commits and most recent commit
                HashMap<String, CodeOwner> owners = new HashMap<>();
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

                codeOwners = new ArrayList(owners.values());
            }

        } catch (IOException | GitAPIException | IllegalArgumentException ex) {
            //this.logger.severe(String.format("8b6da512::error in class %s exception %s for repository %s", ex.getClass(), ex.getMessage(), repoName));
        }

        return (codeOwners==null)?new ArrayList():codeOwners;
    }

    public static void main(String[] args) {
        GitRepositoryProvider grp = new GitRepositoryProvider();
        grp.travelPath(new File("D:\\j2cache"), null);
        /*
        Repository repo = new Repository();
        repo.setName("j2cache");
        repo.setUrl("https://gitee.com/ld/J2Cache");
        repo.setPath("D:\\j2cache");
        grp.travel(repo, null, null);
        for (CodeOwner codeOwner : grp.codeOwners(repo, "docs/UPGRADE.md")) {
            System.out.println(codeOwner);
        }*/
    }
}
