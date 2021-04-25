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
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

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
        return codeQuery(searchKey);
    }

    public static Query codeQuery(String q) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        String[] tokens = AnalyzerFactory.codeAnalyzer.tokens(q).stream().toArray(String[]::new);

        Query fileNameQuery = createPhraseQuery(Constants.FIELD_FILE_NAME, tokens, 1);//new PhraseQuery(1, Constants.FIELD_FILE_NAME, tokens);
        Query sourceQuery = createPhraseQuery(Constants.FIELD_SOURCE, tokens, 5);//new PhraseQuery(5, Constants.FIELD_SOURCE, tokens);

        //make up query
        builder.add(new BoostQuery(fileNameQuery, 10.0f), BooleanClause.Occur.SHOULD);
        builder.add(sourceQuery, BooleanClause.Occur.SHOULD);

        return builder.setMinimumNumberShouldMatch(1).build();
    }

    /**
     * Combine PhraseQuery & WildcardQuery
     * @param field
     * @param phraseWords
     * @param slop
     * @return
     */
    private static Query createPhraseQuery(String field, String[] phraseWords, int slop) {
        if(phraseWords.length == 1)
            return new WildcardQuery(new Term(field, phraseWords[0]+"*"));

        SpanQuery[] queryParts = new SpanQuery[phraseWords.length];
        for (int i = 0; i < phraseWords.length; i++) {
            if(phraseWords[i].length() == 1) {
                queryParts[i] = new SpanTermQuery(new Term(field, phraseWords[i]));
            }
            else {
                WildcardQuery wildQuery = new WildcardQuery(new Term(field, phraseWords[i] + "*"));
                queryParts[i] = new SpanMultiTermQueryWrapper<>(wildQuery);
            }
        }
        return new SpanNearQuery(queryParts, slop,true);
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
