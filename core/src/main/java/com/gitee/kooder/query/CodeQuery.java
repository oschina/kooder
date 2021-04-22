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

import com.gitee.kooder.core.AnalyzerFactory;
import com.gitee.kooder.core.Constants;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.util.Arrays;
import java.util.List;

/**
 * 源码搜索
 * @author Winter Lau (javayou@gmail.com)
 */
public class CodeQuery extends QueryBase {

    /**
     * 索引类型
     *
     * @return
     */
    @Override
    public String type() {
        return Constants.TYPE_CODE;
    }

    /**
     * 构建查询对象
     * @return
     */
    @Override
    protected Query buildUserQuery() {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        QueryParser parser = new QueryParser(Constants.FIELD_SOURCE, AnalyzerFactory.getCodeAnalyzer());
        parser.setDefaultOperator(QueryParser.Operator.AND);
        Query q_source = null;
        try {
            q_source = parser.parse(searchKey);
        } catch (ParseException e) {
            try {
                q_source = parser.parse(QueryParser.escape(searchKey));
            } catch (ParseException ee) {}
        }
        builder.add(q_source, BooleanClause.Occur.SHOULD);

        builder.add(new BoostQuery(new WildcardQuery(new Term(Constants.FIELD_FILE_NAME, QueryParser.escape(searchKey))), 10.f), BooleanClause.Occur.SHOULD);

        //builder.add(makeBoostQuery(Constants.FIELD_FILE_NAME,  q, 10.0f), BooleanClause.Occur.SHOULD);
        //builder.add(makeBoostQuery(Constants.FIELD_SOURCE,     q, 1.0f), BooleanClause.Occur.SHOULD);
        builder.setMinimumNumberShouldMatch(1);
        return builder.build();
    }

    /**
     * 构建排序对象
     *
     * @return
     */
    @Override
    protected Sort buildSort() {
        if("update".equals(sort))
            return new Sort(new SortedNumericSortField(Constants.FIELD_LAST_INDEX, SortField.Type.LONG, true));
        return Sort.RELEVANCE;
    }

    /**
     * list facet names
     * @return
     */
    @Override
    protected List<String> listFacetFields() {
        return Arrays.asList(Constants.FIELD_LANGUAGE, Constants.FIELD_REPO_NAME, Constants.FIELD_CODE_OWNER);
    }

    /**
     * 自定义分词器
     * @param forIndex
     * @return
     */
    @Override
    protected Analyzer getAnalyzer(boolean forIndex) {
        return AnalyzerFactory.getCodeAnalyzer();
    }

}
