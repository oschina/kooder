package com.gitee.search.server;

import com.gitee.search.core.GiteeSearchConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Properties;

/**
 * HTTP static file service
 * @author Winter Lau<javayou@gmail.com>
 */
public class StaticFileService {

    private final static Logger log = LoggerFactory.getLogger(StaticFileService.class);

    public final static Properties CONTENT_TYPES = new Properties();

    private final static Path webRoot;

    static {
        String sWebroot = GiteeSearchConfig.getProperty("http.webroot");
        webRoot = GiteeSearchConfig.getPath(sWebroot);
        try (InputStream stream = Response.class.getResourceAsStream("/mime-types.properties")) {
            CONTENT_TYPES.load(stream);
        } catch(IOException e) {
            log.error("Failed to loading mime-types.properties", e);
        }
    }

    /**
     * 根据文件请求地址获取对应的 Mime-Type
     * @param path
     * @return
     */
    public final static String getMimeType(String path) {
        if(path == null)
            return null;
        int idx = path.lastIndexOf(".");
        return (idx > 0)?CONTENT_TYPES.getProperty(path.substring(idx + 1).toLowerCase()):null;
    }

    /**
     * Check if request is for static file
     * @param req
     * @return
     */
    public final static boolean isStatic(Request req) {
        String path = req.getPath().toLowerCase();
        return getMimeType(path) != null;
    }

    /**
     * 读取文件内容
     * @param path
     * @return
     * @throws IOException
     */
    public static ByteBuf read(String path) throws IOException {
        while(path.charAt(0) == '/')
            path = path.substring(1);
        Path file = webRoot.resolve(path).normalize();
        if(!file.toString().startsWith(webRoot.toString()))//不能越过 webroot 定义的目录，避免安全问题
            throw new NoSuchFileException(path);
        return Unpooled.copiedBuffer(Files.readAllBytes(file));
    }

    public static void main(String[] args) throws IOException {
        read("/css/main.css");
    }
}
