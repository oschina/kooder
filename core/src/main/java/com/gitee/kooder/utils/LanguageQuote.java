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

/**
 * 对应 languages.json 文件中的 quote 字段
 */
public class LanguageQuote {

    public String start;
    public String end;
    public boolean ignoreescape;
    public boolean docstring;

    public LanguageQuote(){}

    public LanguageQuote(String start, String end, boolean ignoreescape, boolean docstring) {
        this.start = start;
        this.end = end;
        this.ignoreescape = ignoreescape;
        this.docstring = docstring;
    }
}
