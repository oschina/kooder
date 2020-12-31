/*
 * Copyright (c) 2016 Boyter Online Services
 *
 * Use of this software is governed by the Fair Source License included
 * in the LICENSE.TXT file, but will be eventually open under GNU General Public License Version 3
 * see the README.md for when this clause will take effect
 *
 * Version 1.3.15
 */

package com.gitee.search.utils;

import com.google.common.collect.Iterables;
import com.searchcode.app.config.Values;
import com.searchcode.app.dao.Data;
import com.searchcode.app.dto.*;
import com.searchcode.app.service.Singleton;
import com.searchcode.app.util.FileClassifier;
import com.searchcode.app.util.Helpers;
import com.searchcode.app.util.ISpellingCorrector;
import com.searchcode.app.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchCodeLib {

    private static ISpellingCorrector spellingCorrector;
    private static FileClassifier fileClassifier;
    private static int MINIFIED_LENGTH = 255;

    private final static int MAX_SPLIT_LENGTH = 100_000;
    private final static Pattern MULTIPLE_UPPERCASE = Pattern.compile("[A-Z]{2,}");
    private final static boolean GUESS_BINARY = true;
    private final static boolean AND_MATCH = true;

    public static String[] WHITE_LIST = "".split(",");
    public static String[] BLACK_LIST = "woff,eot,cur,dm,xpm,emz,db,scc,idx,mpp,dot,pspimage,stl,dml,wmf,rvm,resources,tlb,docx,doc,xls,xlsx,ppt,pptx,msg,vsd,chm,fm,book,dgn,blines,cab,lib,obj,jar,pdb,dll,bin,out,elf,so,msi,nupkg,pyc,ttf,woff2,jpg,jpeg,png,gif,bmp,psd,tif,tiff,yuv,ico,xls,xlsx,pdb,pdf,apk,com,exe,bz2,7z,tgz,rar,gz,zip,zipx,tar,rpm,bin,dmg,iso,vcd,mp3,flac,wma,wav,mid,m4a,3gp,flv,mov,mp4,mpg,rm,wmv,avi,m4v,sqlite,class,rlib,ncb,suo,opt,o,os,pch,pbm,pnm,ppm,pyd,pyo,raw,uyv,uyvy,xlsm,swf,xz".split(",");

    public SearchCodeLib() {
        this(Singleton.getSpellingCorrector(), new com.searchcode.app.util.FileClassifier(), Singleton.getData(), Singleton.getHelpers());
    }

    public SearchCodeLib(ISpellingCorrector spellingCorrector, FileClassifier fileClassifier, Data data, Helpers helpers) {
        spellingCorrector = spellingCorrector;
        fileClassifier = fileClassifier;
    }

    /**
     * Split "intelligently" on anything over 7 characters long
     * if it only contains [a-zA-Z]
     * split based on uppercase String[] r = s.split("(?=\\p{Upper})");
     * add those as additional words to index on
     * so that things like RegexIndexer becomes Regex Indexer
     * split the string by spaces
     * look for anything over 7 characters long
     * if its only [a-zA-Z]
     * split by uppercase
     */
    public static String splitKeywords(String contents, boolean runningJoin) {
        if (contents == null) {
            return Values.EMPTYSTRING;
        }

        StringBuilder indexContents = new StringBuilder();

        contents = contents.replaceAll("[^a-zA-Z0-9]", " ");

        // Performance improvement hack
        if (contents.length() > MAX_SPLIT_LENGTH) {

            // Add AAA to ensure we dont split the last word if it was cut off
            contents = contents.substring(0, MAX_SPLIT_LENGTH) + "AAA";
        }

        for (String splitContents : contents.split(" ")) {
            if (splitContents.length() >= 7) {
                Matcher m = MULTIPLE_UPPERCASE.matcher(splitContents);

                if (!m.find()) {
                    String[] splitStrings = splitContents.split("(?=\\p{Upper})");

                    if (splitStrings.length > 1) {
                        indexContents.append(" ").append(StringUtils.join(splitStrings, " "));

                        if (runningJoin) {
                            StringBuilder running = new StringBuilder();
                            for (String split : splitStrings) {
                                running.append(split);
                                indexContents.append(" ").append(running.toString());
                            }
                        }
                    }
                }
            }
        }

        return indexContents.toString();
    }

    public static String findInterestingKeywords(String contents) {
        if (contents == null) {
            return "";
        }

        StringBuilder indexContents = new StringBuilder();

        // Performance improvement hack
        if (contents.length() > MAX_SPLIT_LENGTH) {
            // Add AAA to ensure we dont split the last word if it was cut off
            contents = contents.substring(0, MAX_SPLIT_LENGTH) + "AAA";
        }

        // Finds versions with words at the front, eg linux2.7.4
        Matcher m = Pattern.compile("[a-z]+(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)").matcher(contents);

        while (m.find()) {
            indexContents.append(" ");
            indexContents.append(m.group());
        }

        return indexContents.toString();
    }

    public static String findInterestingCharacters(String contents) {
        if (contents == null) {
            return "";
        }

        String replaced = contents.replaceAll("\\w", "");

        StringBuilder stringBuilder = new StringBuilder();
        for (char c : replaced.toCharArray()) {
            stringBuilder.append(c).append(" ");
        }

        return stringBuilder.toString();
    }

    /**
     * List of languages to ignore displaying the cost for
     * TODO move this into the database so it is configurable
     */
    public static boolean languageCostIgnore(String languagename) {

        boolean ignore;

        switch (languagename) {
            case "Unknown":
            case "Text":
            case "JSON":
            case "Markdown":
            case "INI File":
            case "ReStructuredText":
            case "Configuration":
                ignore = true;
                break;
            default:
                ignore = false;
                break;
        }

        return ignore;
    }

    /**
     * Adds a string into the spelling corrector.
     * TODO move this into the spelling corrector class itself
     */
    public static void addToSpellingCorrector(String contents) {
        if (contents == null) {
            return;
        }

        // Limit to reduce performance impacts
        if (contents.length() > MAX_SPLIT_LENGTH) {
            contents = contents.substring(0, MAX_SPLIT_LENGTH);
        }

        List<String> splitString = Arrays.asList(contents.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase().split(" "));

        // Only the first 10000 to avoid causing too much slow-down
        if (splitString.size() > 10_000) {
            splitString = splitString.subList(0, 10_000);
        }

        for (String s : splitString) {
            if (s.length() >= 3) {
                spellingCorrector.putWord(s);
            }
        }
    }

    /**
     * Determine if a List<String> which is used to represent a code file contains a code file that is
     * suspected to be minified. This is for the purposes of excluding it from the index.
     */
    public static boolean isMinified(List<String> codeLines, String fileName) {

        var lowerFileName = fileName.toLowerCase();

        for (var extension : WHITE_LIST) {
            if (lowerFileName.endsWith("." + extension)) {
                return false;
            }
        }

        var average = codeLines.stream().map(x -> x.trim().replace(" ", "")).mapToInt(String::length).average();
        if (average.isPresent() && average.getAsDouble() > MINIFIED_LENGTH) {
            return true;
        }

        return false;
    }

    /**
     * Determine if a List<String> which is used to represent a code file contains a code file that is
     * suspected to be ascii or non ascii. This is for the purposes of excluding it from the index.
     */
    public static BinaryFinding isBinary(List<String> codeLines, String fileName) {
        if (codeLines.isEmpty()) {
            return new BinaryFinding(true, "file is empty");
        }

        var lowerFileName = fileName.toLowerCase();
        // Check against user set whitelist
        for (var extension : WHITE_LIST) {
            if (lowerFileName.endsWith("." + extension)) {
                return new BinaryFinding(false, "appears in extension whitelist");
            }
        }

        // Check against user set blacklist
        for (var extension : BLACK_LIST) {
            if (lowerFileName.endsWith("." + extension) || lowerFileName.equals(extension)) {
                return new BinaryFinding(true, "appears in extension blacklist");
            }
        }

        // Check if whitelisted extension IE what we know about
        var database = fileClassifier.getDatabase();
        for (var key : database.keySet()) {
            var fileClassifierResult = database.get(key);
            for (var extension : fileClassifierResult.extensions) {
                if (lowerFileName.endsWith("." + extension)) {
                    return new BinaryFinding(false, "appears in internal extension whitelist");
                }
            }
        }

        // If we aren't meant to guess then assume it isn't binary
        if (!GUESS_BINARY) {
            return new BinaryFinding(false, Values.EMPTYSTRING);
        }

        // GNU Grep, ripgrep and git all take the approach that if a file as a nul
        // byte in it then it is binary. If its good enough for those giants
        // its good enough for us.
        for (int i = 0; i < codeLines.size(); i++) {
            var line = codeLines.get(i);
            for (int j = 0; j < line.length(); j++) {
                if (line.charAt(j) == 0) {
                    return new BinaryFinding(true, "nul byte found");
                }
            }
        }

        return new BinaryFinding(false, Values.EMPTYSTRING);
    }

    /**
     * Determines who owns a piece of code weighted by time based on current second (IE time now)
     * NB if a commit is very close to this time it will always win
     */
    public static String codeOwner(List<CodeOwner> codeOwners) {
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
     * Cleans and formats the code into something that can be indexed by lucene while supporting searches such as
     * i++ matching for(int i=0;i<100;i++;){
     */
    public static String codeCleanPipeline(String originalContents) {
        if (originalContents == null)
            return "";

        String modifiedContents = originalContents;

        StringBuilder indexContents = new StringBuilder();

        // Change how we replace strings
        // Modify the contents to match strings correctly
        char[] firstReplacements = {'<', '>', ')', '(', '[', ']', '|', '=', ',', ':'};
        for (char c : firstReplacements) {
            modifiedContents = modifiedContents.replace(c, ' ');
        }
        indexContents.append(" ").append(modifiedContents);

        char[] otherReplacements = {'.'};
        for (char c : otherReplacements) {
            modifiedContents = modifiedContents.replace(c, ' ');
        }
        indexContents.append(" ").append(modifiedContents);

        char[] secondReplacements = {';', '{', '}', '/'};
        for (char c : secondReplacements) {
            modifiedContents = modifiedContents.replace(c, ' ');
        }
        indexContents.append(" ").append(modifiedContents);

        char[] forthReplacements = {'"', '\''};
        for (char c : forthReplacements) {
            modifiedContents = modifiedContents.replace(c, ' ');
        }
        indexContents.append(" ").append(modifiedContents);

        // Now do it for other characters
        char[] replacements = {'\'', '"', '.', ';', '=', '(', ')', '[', ']', '_', ';', '@', '#'};
        for (char c : replacements) {
            modifiedContents = modifiedContents.replace(c, ' ');
        }
        indexContents.append(" ").append(modifiedContents);

        char[] thirdReplacements = {'-'};
        for (char c : thirdReplacements) {
            modifiedContents = modifiedContents.replace(c, ' ');
        }
        indexContents.append(" ").append(modifiedContents);

        // Issue 188 Fixes
        modifiedContents = originalContents;
        char[] replacements188 = {'(', ')', '<', '>'};
        for (char c : replacements188) {
            modifiedContents = modifiedContents.replace(c, ' ');
        }
        indexContents.append(" ").append(modifiedContents);


        return indexContents.toString();
    }

    /**
     * Parse the query and escape it as per Lucene but without affecting search operators such as AND OR and NOT
     */
    public static String formatQueryString(String query) {
        return AND_MATCH ? formatQueryStringAndDefault(query) : formatQueryStringOrDefault(query);
    }

    public static String formatQueryStringAndDefault(String query) {
        String[] split = query.trim().split("\\s+");

        List<String> stringList = new ArrayList<>();

        String and = " AND ";
        String or = " OR ";
        String not = " NOT ";

        for (String term : split) {
            switch (term) {
                case "AND":
                    if (Iterables.getLast(stringList, null) != null && !Iterables.getLast(stringList).equals(and)) {
                        stringList.add(and);
                    }
                    break;
                case "OR":
                    if (Iterables.getLast(stringList, null) != null && !Iterables.getLast(stringList).equals(or)) {
                        stringList.add(or);
                    }
                    break;
                case "NOT":
                    if (Iterables.getLast(stringList, null) != null && !Iterables.getLast(stringList).equals(not)) {
                        stringList.add(not);
                    }
                    break;
                default:
                    if (Iterables.getLast(stringList, null) == null ||
                            Iterables.getLast(stringList).equals(and) ||
                            Iterables.getLast(stringList).equals(or) ||
                            Iterables.getLast(stringList).equals(not)) {
                        stringList.add(" " + QueryParser.escape(term.toLowerCase()).replace("\\(", "(").replace("\\)", ")").replace("\\*", "*") + " ");
                    } else {
                        stringList.add(and + QueryParser.escape(term.toLowerCase()).replace("\\(", "(").replace("\\)", ")").replace("\\*", "*") + " ");
                    }
                    break;
            }
        }
        String temp = StringUtils.join(stringList, " ");
        return temp.trim();
    }

    public static String formatQueryStringOrDefault(String query) {
        String[] split = query.trim().split("\\s+");

        StringBuilder sb = new StringBuilder();

        String and = " AND ";
        String or = " OR ";
        String not = " NOT ";

        for (String term : split) {
            switch (term) {
                case "AND":
                    sb.append(and);
                    break;
                case "OR":
                    sb.append(or);
                    break;
                case "NOT":
                    sb.append(not);
                    break;
                default:
                    sb.append(" ");
                    sb.append(QueryParser.escape(term.toLowerCase()).replace("\\(", "(").replace("\\)", ")").replace("\\*", "*"));
                    sb.append(" ");
                    break;
            }
        }

        return sb.toString().trim();
    }

    /**
     * Given a query attempts to create alternative queries that should be looser and as such produce more matches
     * or give results where none may exist for the current query.
     */
    public static List<String> generateAltQueries(String query) {
        List<String> altQueries = new ArrayList<>();
        query = query.trim().replaceAll(" +", " ");
        String altquery = query.replaceAll("[^A-Za-z0-9 ]", " ").trim().replaceAll(" +", " ");

        if (!altquery.equals(query) && !Values.EMPTYSTRING.equals(altquery)) {
            altQueries.add(altquery);
        }

        altquery = splitKeywords(query, false).trim();
        if (!altquery.equals("") && !altquery.equals(query) && !altQueries.contains(altquery)) {
            altQueries.add(altquery);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String word : query.replaceAll(" +", " ").split(" ")) {
            if (!word.trim().equals("AND") && !word.trim().equals("OR") && !word.trim().equals("NOT")) {
                stringBuilder.append(" ").append(spellingCorrector.correct(word));
            }
        }
        altquery = stringBuilder.toString().trim();

        if (!altquery.toLowerCase().equals(query.toLowerCase()) && !altQueries.contains(altquery)) {
            altQueries.add(altquery);
        }

        altquery = query.replace(" AND ", " OR ");
        if (!altquery.toLowerCase().equals(query.toLowerCase()) && !altQueries.contains(altquery)) {
            altQueries.add(altquery);
        }

        altquery = query.replace(" AND ", " ");
        if (!altquery.toLowerCase().equals(query.toLowerCase()) && !altQueries.contains(altquery)) {
            altQueries.add(altquery);
        }

        altquery = query.replace(" NOT ", " ");
        if (!altquery.toLowerCase().equals(query.toLowerCase()) && !altQueries.contains(altquery)) {
            altQueries.add(altquery);
        }

        return altQueries;
    }


    public static String generateBusBlurb(ProjectStats projectStats) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("In this repository ").append(projectStats.getRepoFacetOwner().size());

        if (projectStats.getRepoFacetOwner().size() == 1) {
            stringBuilder.append(" committer has contributed to ");
        } else {
            stringBuilder.append(" committers have contributed to ");
        }

        if (projectStats.getTotalFiles() == 1) {
            stringBuilder.append(projectStats.getTotalFiles()).append(" file. ");
        } else {
            stringBuilder.append(projectStats.getTotalFiles()).append(" files. ");
        }

        List<CodeFacetLanguage> codeFacetLanguages = projectStats.getCodeFacetLanguages();

        if (codeFacetLanguages.size() == 1) {
            stringBuilder.append("The most important language in this repository is ").append(codeFacetLanguages.get(0).getLanguageName()).append(". ");
        } else {
            stringBuilder.append("The most important languages in this repository are ");

            if (!codeFacetLanguages.isEmpty()) {
                if (codeFacetLanguages.size() > 3) {
                    codeFacetLanguages = codeFacetLanguages.subList(0, 3);
                }
                for (int i = 0; i < codeFacetLanguages.size() - 1; i++) {
                    stringBuilder.append(codeFacetLanguages.get(i).getLanguageName()).append(", ");
                }
                stringBuilder.append(" and ").append(codeFacetLanguages.get(codeFacetLanguages.size() - 1).getLanguageName()).append(". ");
            }
        }

        if (!projectStats.getRepoFacetOwner().isEmpty()) {
            if (projectStats.getRepoFacetOwner().size() < 5) {
                stringBuilder.append("The project has a low bus factor of ").append(projectStats.getRepoFacetOwner().size());
                stringBuilder.append(" and will be in trouble if ").append(projectStats.getRepoFacetOwner().get(0).getOwner()).append(" is hit by a bus. ");
            } else if (projectStats.getRepoFacetOwner().size() < 15) {
                stringBuilder.append("The project has bus factor of ").append(projectStats.getRepoFacetOwner().size()).append(". ");
            } else {
                stringBuilder.append("The project has high bus factor of ").append(projectStats.getRepoFacetOwner().size()).append(". ");
            }
        }

        List<String> highKnowledge = new ArrayList<>();
        double sumAverageFilesWorked = 0;
        for (CodeFacetOwner codeFacetOwner : projectStats.getRepoFacetOwner()) {
            double currentAverage = (double) codeFacetOwner.getCount() / (double) projectStats.getTotalFiles();
            sumAverageFilesWorked += currentAverage;

            if (currentAverage > 0.1) {
                highKnowledge.add(codeFacetOwner.getOwner());
            }
        }

        int averageFilesWorked = (int) (sumAverageFilesWorked / projectStats.getRepoFacetOwner().size() * 100);

        stringBuilder.append("The average person who commits this project has ownership of ");
        stringBuilder.append(averageFilesWorked).append("% of files. ");

        if (!highKnowledge.isEmpty()) {
            stringBuilder.append("The project relies on the following people; ");
            stringBuilder.append(StringUtils.join(highKnowledge, ", ")).append(". ");
        }

        return stringBuilder.toString().replace(",  and", " and");
    }

}

