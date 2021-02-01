/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.gitee.kooder.utils;

import java.util.*;

/**
 * 源文件统计，包括总行数，代码行，注释行，空行等，还有代码复杂度评估
 */
public class SlocCounter {

    private final Map<String, FileClassifierResult> database;
    private final ArrayList<List<Integer>> byteOrderMarks;

    public SlocCounter() {
        this.database = FileClassifier.getDatabase();

        // Taken from https://en.wikipedia.org/wiki/Byte_order_mark#Byte_order_marks_by_encoding
        byteOrderMarks = new ArrayList<>(Arrays.asList(
                Arrays.asList(239, 187, 191),       // UTF-8
                Arrays.asList(254, 255),            // UTF-16 BE
                Arrays.asList(255, 254),            // UTF-16 LE
                Arrays.asList(0, 0, 254, 255),      // UTF-32 BE
                Arrays.asList(255, 254, 0, 0),      // UTF-32 LE
                Arrays.asList(43, 47, 118, 56),     // UTF-7
                Arrays.asList(43, 47, 118, 57),     // UTF-7
                Arrays.asList(43, 47, 118, 43),     // UTF-7
                Arrays.asList(43, 47, 118, 47),     // UTF-7
                Arrays.asList(43, 47, 118, 56, 45), // UTF-7
                Arrays.asList(247, 100, 76),        // UTF-7
                Arrays.asList(221, 115, 102, 115),  // UTF-EBCDIC
                Arrays.asList(14, 254, 255),        // SCSU
                Arrays.asList(251, 238, 40),        // BOCU-1
                Arrays.asList(132, 49, 149, 51)     // GB-18030
        ));
    }

    public ArrayList<List<Integer>> getByteOrderMarks() {
        return byteOrderMarks;
    }

    public boolean checkForMatch(char currentByte, int index, int endPoint, String[] matches, String content) {
        if (matches == null) {
            return false;
        }

        for (int i = 0; i < matches.length; i++) { // For each match
            if (currentByte == matches[i].charAt(0)) { // If the first character matches
                boolean potentialMatch = true;

                for (int j = 0; j < matches[i].length(); j++) { // Check if the rest match
                    if (index + j <= endPoint && matches[i].charAt(j) != content.charAt(index + j)) {
                        potentialMatch = false;
                    }
                }

                if (potentialMatch) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean checkForMatchSingle(char currentByte, int index, int endPoint, String match, String content) {
        if (match == null) {
            return false;
        }

        if (match.length() != 0 && currentByte == match.charAt(0)) { // If the first character matches
            boolean potentialMatch = true;

            for (int j = 0; j < match.length(); j++) { // Check if the rest match
                if (index + j <= endPoint && match.charAt(j) != content.charAt(index + j)) {
                    potentialMatch = false;
                }
            }

            return potentialMatch;
        }

        return false;
    }

    public String checkForMatchMultiOpen(char currentByte, int index, int endPoint, String[][] matches, String content) {
        if (matches == null) {
            return null;
        }

        for (int i = 0; i < matches.length; i++) { // For each match
            if (currentByte == matches[i][0].charAt(0)) { // If the first character matches
                boolean potentialMatch = true;

                for (int j = 0; j < matches[i][0].length(); j++) { // Check if the rest match
                    if (index + j <= endPoint && matches[i][0].charAt(j) != content.charAt(index + j)) {
                        potentialMatch = false;
                        break;
                    }
                }

                if (potentialMatch) {
                    // Refers to the closing condition for the matching open
                    return matches[i][1];
                }
            }
        }

        return null;
    }

    public String checkForMatchMultiOpenQuote(char currentByte, int index, int endPoint, LanguageQuote[] matches, String content) {
        if (matches == null) {
            return null;
        }

        for (int i = 0; i < matches.length; i++) { // For each match
            if (currentByte == matches[i].start.charAt(0)) { // If the first character matches
                boolean potentialMatch = true;

                for (int j = 0; j < matches[i].start.length(); j++) { // Check if the rest match
                    if (index + j <= endPoint && matches[i].start.charAt(j) != content.charAt(index + j)) {
                        potentialMatch = false;
                        break;
                    }
                }

                if (potentialMatch) {
                    // Refers to the closing condition for the matching open
                    return matches[i].end;
                }
            }
        }

        return null;
    }

    public boolean isWhitespace(char currentByte) {
        return currentByte == ' ' || currentByte == '\t' || currentByte == '\n' || currentByte == '\r';
    }

    /**
     * Reimplementation of scc https://github.com/boyter/scc/ 1.9.0 ported from
     * Go into Java and specific for the searchcode project.
     */
    public SlocCount countStats(String contents, String languageName) {
        if (contents == null || contents.isEmpty()) {
            return new SlocCount();
        }

        FileClassifierResult fileClassifierResult = this.database.get(languageName);

        if (fileClassifierResult == null) {
            return new SlocCount(contents.split("\n").length, 0, 0, 0, 0);
        }

        State currentState = State.S_BLANK;

        int endPoint = contents.length() - 1;
        String endString = null;
        ArrayList<String> endComments = new ArrayList<>();
        int linesCount = 0;
        int blankCount = 0;
        int codeCount = 0;
        int commentCount = 0;
        int complexity = 0;

        int start = this.checkBomSkip(contents);

        for (int index = start; index < contents.length(); index++) {
            if (!isWhitespace(contents.charAt(index))) {
                switch (currentState) {
                    case S_CODE:
                        if (fileClassifierResult.nestedmultiline || endComments.isEmpty()) {
                            endString = this.checkForMatchMultiOpen(contents.charAt(index), index, endPoint, fileClassifierResult.multi_line, contents);
                            if (endString != null) {
                                index += endString.length() - 1;
                                endComments.add(endString);
                                currentState = State.S_MULTICOMMENT_CODE;
                                break;
                            }
                        }

                        if (this.checkForMatch(contents.charAt(index), index, endPoint, fileClassifierResult.line_comment, contents)) {
                            currentState = State.S_COMMENT_CODE;
                            break;
                        }

                        endString = this.checkForMatchMultiOpenQuote(contents.charAt(index), index, endPoint, fileClassifierResult.quotes, contents);
                        if (endString != null) {
                            currentState = State.S_STRING;
                            break;
                        } else if (this.checkForMatch(contents.charAt(index), index, endPoint, fileClassifierResult.complexitychecks, contents)) {
                            complexity++;
                        }
                        break;
                    case S_STRING:
                        if (contents.charAt(index - 1) != '\\' && this.checkForMatchSingle(contents.charAt(index), index, endPoint, endString, contents)) {
                            currentState = State.S_CODE;
                        }
                        break;
                    case S_MULTICOMMENT:
                    case S_MULTICOMMENT_CODE:
                        if (fileClassifierResult.nestedmultiline || endComments.isEmpty()) {
                            endString = this.checkForMatchMultiOpen(contents.charAt(index), index, endPoint, fileClassifierResult.multi_line, contents);
                            if (endString != null) {
                                index += endString.length() - 1;
                                endComments.add(endString);
                                currentState = State.S_MULTICOMMENT_CODE;
                                break;
                            }
                        }

                        if (this.checkForMatchSingle(contents.charAt(index), index, endPoint, endComments.get(endComments.size() - 1), contents)) {
                            index += endComments.get(endComments.size() - 1).length() - 1;
                            endComments.remove(endComments.size() - 1);

                            if (endComments.isEmpty()) {
                                if (currentState == State.S_MULTICOMMENT_CODE) {
                                    currentState = State.S_CODE;
                                } else {
                                    currentState = State.S_MULTICOMMENT_BLANK;
                                }
                            }
                        }
                        break;
                    case S_BLANK:
                    case S_MULTICOMMENT_BLANK:
                        if (this.checkForMatch(contents.charAt(index), index, endPoint, fileClassifierResult.line_comment, contents)) {
                            currentState = State.S_COMMENT;
                            break;
                        }

                        if (fileClassifierResult.nestedmultiline || endComments.isEmpty()) {
                            endString = this.checkForMatchMultiOpen(contents.charAt(index), index, endPoint, fileClassifierResult.multi_line, contents);
                            if (endString != null) {
                                index += endString.length() - 1;
                                endComments.add(endString);
                                currentState = State.S_MULTICOMMENT;
                                break;
                            }
                        }

                        endString = this.checkForMatchMultiOpenQuote(contents.charAt(index), index, endPoint, fileClassifierResult.quotes, contents);
                        if (endString != null) {
                            currentState = State.S_STRING;
                            break;
                        }

                        currentState = State.S_CODE;
                        if (this.checkForMatch(contents.charAt(index), index, endPoint, fileClassifierResult.complexitychecks, contents)) {
                            complexity++;
                        }
                        break;
                }
            }

            // This means the end of processing the line so calculate the stats according to what state
            // we are currently in
            if (index >= contents.length() || contents.charAt(index) == '\n' || index == endPoint) {
                linesCount++;

                switch (currentState) {
                    case S_BLANK:
                        blankCount++;
                        break;
                    case S_COMMENT:
                    case S_MULTICOMMENT:
                    case S_MULTICOMMENT_BLANK:
                        commentCount++;
                        break;
                    case S_CODE:
                    case S_STRING:
                    case S_COMMENT_CODE:
                    case S_MULTICOMMENT_CODE:
                        codeCount++;
                        break;
                }


                if (currentState == State.S_MULTICOMMENT || currentState == State.S_MULTICOMMENT_CODE) {
                    currentState = State.S_MULTICOMMENT;
                } else if (currentState == State.S_STRING) {
                    currentState = State.S_STRING;
                } else {
                    currentState = State.S_BLANK;
                }
            }
        }

        return new SlocCount(linesCount, blankCount, codeCount, commentCount, complexity);
    }

    public int checkBomSkip(String contents) {
        int start = 0;

        for (List<Integer> bom : byteOrderMarks) {
            if (contents.length() >= bom.size()) {
                boolean isMatch = true;
                for (int i = 0; i < bom.size(); i++) {
                    if (contents.charAt(i) != bom.get(i)) {
                        isMatch = false;
                    }
                }

                if (isMatch) {
                    start = bom.size();
                }
            }
        }

        return start;
    }

    // Used to hold the state of the pointer so we know what type of code we
    // are dealing with
    public enum State {
        S_BLANK,
        S_CODE,
        S_COMMENT,
        S_COMMENT_CODE,
        S_MULTICOMMENT,
        S_MULTICOMMENT_CODE,
        S_MULTICOMMENT_BLANK,
        S_STRING,
    }

    /**
     * Object SlocCounter returns which contains the details of what was
     * found inside the file it was asked to count.
     */
    public class SlocCount {
        public int linesCount = 0;
        public int blankCount = 0;
        public int codeCount = 0;
        public int commentCount = 0;
        public int complexity = 0;

        public SlocCount() {
        }

        public SlocCount(int linesCount, int blankCount, int codeCount, int commentCount, int complexity) {
            this.linesCount = linesCount;
            this.blankCount = blankCount;
            this.codeCount = codeCount;
            this.commentCount = commentCount;
            this.complexity = complexity;
        }
    }
}
