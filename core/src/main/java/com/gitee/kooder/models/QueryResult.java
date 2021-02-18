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
package com.gitee.kooder.models;

import com.gitee.kooder.core.Constants;
import com.gitee.kooder.utils.JsonUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.search.ScoreDoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * lucene search result object
 * @author Winter Lau<javayou@gmail.com>
 */
public class QueryResult {

    private String type;
    private int totalHits;
    private int totalPages;
    private int pageIndex;
    private int pageSize;
    private long timeUsed;
    private String query;
    private List<Searchable> objects;
    private Map<String,List<LabelAndValue>> facets;

    public QueryResult(String type) {
        this.type = type;
        this.objects = new ArrayList<>();
        this.facets = new TreeMap<>();
    }

    /**
     * get object from document and add it to list
     * @param doc
     * @param doc_score
     */
    public void addDocument(Document doc, ScoreDoc doc_score) {
        switch(type){
            case Constants.TYPE_REPOSITORY:
                Repository repo = new Repository();
                repo.setDocument(doc);
                repo.set_doc_id(doc_score.doc);
                repo.set_doc_score(doc_score.score);
                addObject(repo);
                break;
            case Constants.TYPE_ISSUE:
                Issue issue = new Issue(doc);
                issue.set_doc_id(doc_score.doc);
                issue.set_doc_score(doc_score.score);
                addObject(issue);
                break;
            case Constants.TYPE_CODE:
                SourceFile file = new SourceFile();
                file.setDocument(doc);
                file.set_doc_id(doc_score.doc);
                file.set_doc_score(doc_score.score);
                addObject(file);
        }
    }

    /**
     * generate json string
     * @return
     */
    public String json() {
        return JsonUtils.toJson(this);
    }

    @Override
    public String toString() {
        return this.json();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(int totalHits) {
        this.totalHits = totalHits;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTimeUsed() {
        return timeUsed;
    }

    public void setTimeUsed(long timeUsed) {
        this.timeUsed = timeUsed;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<Searchable> getObjects() {
        return objects;
    }

    public void setObjects(List<Searchable> objects) {
        this.objects = objects;
    }

    public void addObject(Searchable obj) {
        this.objects.add(obj);
    }

    public Map<String, List<LabelAndValue>> getFacets() {
        return facets;
    }

    public void setFacets(Map<String, List<LabelAndValue>> facets) {
        this.facets = facets;
    }

    public void addFacet(String facetName, LabelAndValue facet) {
        facets.computeIfAbsent(facetName, f -> new ArrayList<>()).add(facet);
    }
}
