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
package com.gitee.kooder.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * 编程语言自动识别
 */
public class FileClassifier {

    public final static String UNKNOWN_LANGUAGE = "Unknown";
    public final static String BINARY_LANGUAGE  = "Binary";

    private static Map<String, FileClassifierResult> database;

    static {
        try (InputStream stream = FileClassifier.class.getResourceAsStream("/languages.json")){
            TypeReference<HashMap<String, FileClassifierResult>> typeRef = new TypeReference<HashMap<String, FileClassifierResult>>(){};
            database = Collections.unmodifiableMap(JsonUtils.readValue(stream, typeRef));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Map<String, FileClassifierResult> getDatabase() {
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
        List<String> matches = new ArrayList<>();
        String extension = "";

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
        HashMap<String, Integer> toSort = new HashMap();

        for (String m : matches) {
            toSort.put(m, 0);
            for (String keyword : database.get(m).keywords) {
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

    private static List<String> checkIfExtentionExists(String extension) {
        List<String> matches = new ArrayList<>();

        for (String key : database.keySet()) {
            FileClassifierResult fileClassifierResult = database.get(key);

            for (String ext : fileClassifierResult.extensions) {
                if (extension.equals(ext)) {
                    matches.add(key);
                }
            }
        }

        return matches;
    }

    private static List<String> checkIfFilenameExists(String extension) {
        List<String> matches = new ArrayList<>();

        for (String key : database.keySet()) {
            FileClassifierResult fileClassifierResult = database.get(key);

            if (fileClassifierResult.filenames != null) {
                for (String ext : fileClassifierResult.filenames) {
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
