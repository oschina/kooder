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
package com.gitee.kooder.query;

import com.gitee.kooder.models.QueryResult;
import com.gitee.kooder.models.Searchable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 查询接口
 * @author Winter Lau<javayou@gmail.com>
 */
public interface IQuery {

    /**
     * 索引类型
     * @return
     */
    public String type();

    /**
     * 搜索关键字
     * @param key
     */
    IQuery setSearchKey(String key);

    /**
     * 是否解析查询字符串
     * @param parseSearchKey
     * @return
     */
    IQuery setParseSearchKey(boolean parseSearchKey);

    /**
     * 排序方法
     * @param sort
     * @return
     */
    IQuery setSort(String sort);

    /**
     * 页码
     * @param page
     * @return
     */
    IQuery setPage(int page);

    /**
     * 页大小
     * @param pageSize
     * @return
     */
    IQuery setPageSize(int pageSize);

    /**
     * 添加扩展属性
     * @param name
     * @param value
     * @return
     */
    IQuery addFacets(String name, String value);

    /**
     * 获取扩展属性
     * @return
     */
    Map<String, String[]> getFacets();

    /**
     * 添加过滤条件
     * @param filterQueryString
     * @return
     */
    IQuery addFilter(String filterQueryString);

    /**
     * 获取所有过滤条件
     * @return
     */
    List<String> getFilters();

    /**
     * execute query
     * @return
     * @throws IOException
     */
    QueryResult execute() throws IOException;

    /**
     * Get max object indexed
     * @return
     */
    public Searchable getLastestObject() ;
}
