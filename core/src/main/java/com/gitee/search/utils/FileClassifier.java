package com.gitee.search.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 编程语言自动识别
 */
public class FileClassifier {

    public final static String UNKNOWN_LANGUAGE = "Unknown";
    private static HashMap<String, FileClassifierResult> database;

    static {
        try (InputStream stream = FileClassifier.class.getResourceAsStream("/languages.json")){
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            TypeReference<HashMap<String, FileClassifierResult>> typeRef = new TypeReference<>(){};
            database = mapper.readValue(stream, typeRef);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static HashMap<String, FileClassifierResult> getDatabase() {
        return database;
    }

    public static String getExtension(String fileName) {
        return FilenameUtils.getExtension(fileName);
    }

    /**
     * Given a filename guesses the file type
     */
    public static String languageGuess(String fileName, String content) {
        fileName = fileName.toLowerCase();
        var matches = new ArrayList<String>();
        var extension = "";

        // Try finding based on full name match
        matches = checkIfFilenameExists(fileName);

        // Try finding using the whole name EG LICENSE
        if (matches.isEmpty()) {
            matches = checkIfExtentionExists(fileName);
        }

        // Try matching based on one level EG d.ts OR ts
        if (matches.isEmpty()) {
            extension = getExtension(fileName);
            matches = checkIfExtentionExists(extension);
        }

        // Catch all if the above did not work, IE turn d.ts into ts
        if (matches.isEmpty()) {
            extension = getExtension(extension);
            matches = checkIfExtentionExists(extension);
        }

        // If no idea at point return that we don't know
        if (matches.isEmpty()) {
            return UNKNOWN_LANGUAGE;
        }

        // If we have a single match then return it
        if (matches.size() == 1) {
            return matches.get(0);
        }

        // We have multiple matches, so try to work out which one is the most likely result
        var toSort = new HashMap<String, Integer>();

        for (var m : matches) {
            toSort.put(m, 0);
            for (var keyword : database.get(m).keywords) {
                if (content.contains(keyword)) {
                    toSort.put(m, toSort.get(m) + 1);
                }
            }
        }

        return sortByValue(toSort).keySet().stream().findFirst().orElse(UNKNOWN_LANGUAGE);
    }

    /**
     * Sorts a map by value taken from
     * http://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
     */
    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        Map<K, V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();
        st.sorted(Map.Entry.comparingByValue()).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    private static ArrayList<String> checkIfExtentionExists(String extension) {
        var matches = new ArrayList<String>();

        for (String key : database.keySet()) {
            var fileClassifierResult = database.get(key);

            for (var ext : fileClassifierResult.extensions) {
                if (extension.equals(ext)) {
                    matches.add(key);
                }
            }
        }

        return matches;
    }

    private static ArrayList<String> checkIfFilenameExists(String extension) {
        var matches = new ArrayList<String>();

        for (String key : database.keySet()) {
            var fileClassifierResult = database.get(key);

            if (fileClassifierResult.filenames != null) {
                for (var ext : fileClassifierResult.filenames) {
                    if (extension.equals(ext)) {
                        matches.add(key);
                    }
                }
            }
        }

        return matches;
    }

    public static void main(String[] args) {
        System.out.println(FileClassifier.languageGuess("Hello.java","import java.util.*;  public class Hello {}"));
    }

}
