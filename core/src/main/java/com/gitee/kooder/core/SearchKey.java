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
package com.gitee.kooder.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 搜索关键字封装对象，从关键字中提取一些固定维度信息，例如编程语言等
 * @author Winter Lau (javayou@gmail.com)
 */
public class SearchKey {

    private final static List<String[]> languages = new ArrayList();
    private String key;
    private Map<String, String> facets;

    static {
        try (
                InputStream stream = SearchKey.class.getResourceAsStream("/languages");
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream))
        ) {
            do {
                String line = reader.readLine();
                if(line == null)
                    break;
                line = line.trim();
                if(line.length() == 0 || line.charAt(0) == '#')
                    continue;
                languages.add(line.split("/"));
            } while(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SearchKey(){
        this.facets = new HashMap<>();
    }

    public final static SearchKey parse(String key) {
        SearchKey skey = new SearchKey();
        List<String> keys = SearchHelper.splitKeywords(key);
        keys.removeIf( k -> {
            for(String[] langs : languages) {
                if(Arrays.binarySearch(langs, k) >= 0) {
                    skey.facets.put("lang", k);
                    return true;
                }
            }
            return false;
        });
        skey.key = String.join(" ", keys);
        return skey;
    }

    public String getKey() {
        return key;
    }

    public Map<String, String> getFacets() {
        return facets;
    }

    public static void main(String[] args) {
        SearchKey sk = SearchKey.parse("j2cache java");
        System.out.println(sk.key);
        sk.facets.forEach((k,v)->System.out.printf("%s -> %s\n",k,v));
    }

}
