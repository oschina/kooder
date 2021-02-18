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
package com.gitee.kooder.models;

/**
 * The main developer of one source file
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
