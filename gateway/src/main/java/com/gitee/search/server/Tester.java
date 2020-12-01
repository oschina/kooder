package com.gitee.search.server;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Tester {

    public static void main(String[] args) throws IOException {
        List<String> repos = new ArrayList<>();
        for(int p = 1; p <= 4; p++) {
            Document doc = Jsoup.connect("https://github.com/EdgexFoundry?page=" + p).get();
            Elements elems = doc.select("a[itemprop]");
            elems.forEach( e -> {
                String url = e.attr("href");
                if(url.startsWith("/edgexfoundry")) {
                    //System.out.println(url);
                    repos.add(url);
                }
            });
        }

        repos.forEach(repo -> {
            String url = "https://gitee.com" + StringUtils.replace(repo, "edgexfoundry", "EdgexFoundry");
            try {
                Jsoup.connect(url).get();
            } catch (IOException e) {
                System.out.println(repo);
            }
            //System.out.println(url);
        });

        System.out.println(repos.size());
    }

}
