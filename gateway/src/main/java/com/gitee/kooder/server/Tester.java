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
package com.gitee.kooder.server;

import org.apache.commons.lang3.StringUtils;
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
