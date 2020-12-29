package com.gitee.search.code;

import java.util.ArrayList;
import java.util.List;

/**
 * 仓库变更信息
 * @author Winter Lau<javayou@gmail.com>
 */
public class RepositoryChanged {

    private boolean changed;
    private List<String> changedFiles;
    private List<String> deletedFiles;
    private boolean clone;

    public RepositoryChanged(boolean changed) {
        this.clone = false;
        this.changed = changed;
        this.changedFiles = new ArrayList<>();
        this.deletedFiles = new ArrayList<>();
    }

    public RepositoryChanged(boolean changed, List<String> changedFiles, List<String> deletedFiles) {
        this.clone = false;
        this.changed = changed;
        this.changedFiles = changedFiles;
        this.deletedFiles = deletedFiles;
    }

    public boolean isChanged() {
        return changed;
    }

    public List<String> getChangedFiles() {
        return changedFiles;
    }

    public List<String> getDeletedFiles() {
        return deletedFiles;
    }

    public boolean isClone() {
        return clone;
    }

    public void setClone(boolean clone) {
        this.clone = clone;
    }
}
