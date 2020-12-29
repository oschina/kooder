package com.gitee.search.code;

/**
 * 代码提交者信息
 * @author Winter Lau<javayou@gmail.com>
 */
public class CodeOwner {

    private int noLines;
    private String name;
    private int mostRecentUnixCommitTimestamp;

    public CodeOwner(String name, int noLines, int mostRecentUnixCommitTimestamp) {
        this.setName(name);
        this.setNoLines(noLines);
        this.setMostRecentUnixCommitTimestamp(mostRecentUnixCommitTimestamp);
    }

    public void incrementLines() {
        this.noLines++;
    }

    public int getNoLines() {
        return noLines;
    }

    public void setNoLines(int noLines) {
        this.noLines = noLines;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMostRecentUnixCommitTimestamp() {
        return mostRecentUnixCommitTimestamp;
    }

    public void setMostRecentUnixCommitTimestamp(int mostRecentUnixCommitTimestamp) {
        this.mostRecentUnixCommitTimestamp = mostRecentUnixCommitTimestamp;
    }

    @Override
    public String toString() {
        return String.format("Name: %s, Time: %d, LINES: %d", name, mostRecentUnixCommitTimestamp, noLines);
    }
}
