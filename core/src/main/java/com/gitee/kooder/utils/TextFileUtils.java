package com.gitee.kooder.utils;

import com.glaforge.i18n.io.CharsetToolkit;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 文件工具包
 * @author Winter Lau<javayou@gmail.com>
 */
public class TextFileUtils {

    public static final long DEFAULT_MAX_FILE_LENGTH_READ = 30_000_000;

    private static String[] binaryExtensions = {"png","gif","jpg","jpeg","bmp","ico",
                                                "mp4","3gp","mpeg","flv","avi","wmv","mov",
                                                "zip","gz","tar","rar","7z",
                                                "exe","bin","lib","dll","so","img","class","deb","rpm","msi","pch",
                                                "pdf","doc","ppt","xls", "docx","pptx","xlsx","keynote",
                                                "woff","ttf","eot","cur","dm","xpm","emz","db","scc","idx",
                                                "mpp","dot","pspimage","stl","dml","wmf","rvm","resources",
                                                "tlb","tmp","DS_Store"
                                                };

    public static boolean isBinaryFile(String filename) {
        return FilenameUtils.isExtension(filename, binaryExtensions);
    }

    /**
     * Generic file paths that should be ignored
     */
    public static boolean ignoreFiles(String fileParent) {
        if (fileParent.endsWith("/.git") || fileParent.contains("/.git/") || fileParent.contains(".git/") || fileParent.equals(".git")) {
            return true;
        }

        if (fileParent.endsWith("/.svn") || fileParent.contains("/.svn/")) {
            return true;
        }

        /*
        if (!this.isNullEmptyOrWhitespace((String) this.properties.get(Values.DIRECTORY_BLACK_LIST))) {
            String[] toIgnoreArray = ((String) this.properties.get(Values.DIRECTORY_BLACK_LIST)).split(",");

            for (String toIgnore : toIgnoreArray) {
                if (fileParent.endsWith("/" + toIgnore) || fileParent.endsWith("/" + toIgnore + "/")) {
                    return true;
                }
            }
        }*/

        return false;
    }

    /**
     * 从文本文件中按行读入字符串，通过 DEFAULT_MAX_FILE_LENGTH_READ 值来防止内存溢出。
     * 该方法会自动识别文件的编码
     * @param file 文本文件对象
     * @param maxFileLineDepth 只返回最开始的 N 行，如果该值为负数，则返回所有行
     */
    public static List<String> readFileLines(File file, int maxFileLineDepth) throws IOException {
        return readFileLines(new FileInputStream(file), maxFileLineDepth);
        /*
        Charset charset = guessCharset(file);
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset))){
            int i = 0;
            int count = 0;
            char[] buffer = new char[128];
            do {
                int rc = reader.read(buffer);
                if(rc > 0) {
                    stringBuilder.append(buffer, 0, rc);
                    count += rc;
                }
                if(rc == -1 || count >= DEFAULT_MAX_FILE_LENGTH_READ)
                    break;
            } while (true);
        }

        String[] lines = stringBuilder.toString().split("\\r\\n|\\n|\\r");
        return (lines.length > maxFileLineDepth && maxFileLineDepth > 0)?Arrays.asList(lines).subList(0, maxFileLineDepth):Arrays.asList(lines);
        */
    }

    /**
     * FIXME 先不管编码的问题
     * @param stream
     * @param maxFileLineDepth
     * @return
     * @throws IOException
     */
    public static List<String> readFileLines(InputStream stream, int maxFileLineDepth) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))){
            int i = 0;
            int count = 0;
            char[] buffer = new char[128];
            do {
                int rc = reader.read(buffer);
                if(rc > 0) {
                    stringBuilder.append(buffer, 0, rc);
                    count += rc;
                }
                if(rc == -1 || count >= DEFAULT_MAX_FILE_LENGTH_READ)
                    break;
            } while (true);
        }

        String[] lines = stringBuilder.toString().split("\\r\\n|\\n|\\r");
        return (lines.length > maxFileLineDepth && maxFileLineDepth > 0)?Arrays.asList(lines).subList(0, maxFileLineDepth):Arrays.asList(lines);
    }

    /**
     * 检查文件的编码
     * @param file
     * @return
     * @throws IOException
     */
    public static Charset guessCharset(File file) throws IOException {
        return CharsetToolkit.guessEncoding(file,4096, StandardCharsets.UTF_8);
    }

    /**
     * Calculate MD5 for a file. Using other methods for this (so this is actually dead code)
     * but we may want to use it in the future so keeping here for the moment.
     */
    public static String sha1(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)){
            return DigestUtils.sha1Hex(fileInputStream);
        } catch (IOException ex) {}
        return "";
    }

}
