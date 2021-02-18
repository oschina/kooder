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
 * 对应 languages.json 中的语言定义
 */
public class FileClassifierResult {

    public String[] extensions;
    public String[] extensionfile;
    public String[] line_comment;
    public String[] complexitychecks;
    public String[][] multi_line;
    public LanguageQuote[] quotes;
    public boolean nestedmultiline;
    public String[] keywords; // Used to identify languages that share extensions
    public String[] filenames;
    public String comment;

    public FileClassifierResult(){}

    public FileClassifierResult(String extensions) {
        this.extensions = extensions.toLowerCase().split(",");
    }

    public FileClassifierResult(String extensions, String keywords) {
        this.extensions = extensions.toLowerCase().split(",");
        this.keywords = keywords.toLowerCase().split(",");
    }
}
