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
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.FacetField;

import java.io.Serializable;

/**
 * Searchable object
 * @author Winter Lau<javayou@gmail.com>
 */
public abstract class Searchable implements Serializable {

    protected long id;      // object id , ex: repo id, issue id
    protected int _doc_id;      // document id
    protected float _doc_score; // document score

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int get_doc_id() {
        return _doc_id;
    }

    public void set_doc_id(int _doc_id) {
        this._doc_id = _doc_id;
    }

    public float get_doc_score() {
        return _doc_score;
    }

    public void set_doc_score(float _doc_score) {
        this._doc_score = _doc_score;
    }

    protected Document newDocument() {
        Document doc = new Document();
        doc.add(new NumericDocValuesField(Constants.FIELD_ID, id));
        doc.add(new StoredField(Constants.FIELD_ID, String.valueOf(id)));
        return doc;
    }

    /**
     * generate lucene document
     * @return
     */
    public abstract Document getDocument() ;

    /**
     * Read fields from document
     * @param doc
     */
    public abstract Searchable setDocument(Document doc) ;

    /**
     * 读取数值字段
     * @param doc
     * @param fieldName
     * @param defValues
     * @return
     */
    public final static int getIntField(Document doc, String fieldName, int...defValues) {
        int def = (defValues.length > 0)?defValues[0]:-1;
        return NumberUtils.toInt(doc.get(fieldName), def);
    }

    protected void addLongToDoc(Document doc, String fn, long fv) {
        doc.add(new LongPoint(fn, fv));
        doc.add(new StoredField(fn, String.valueOf(fv)));
    }

    protected void addIntToDoc(Document doc, String fn, int fv) {
        doc.add(new IntPoint(fn, fv));
        doc.add(new StoredField(fn, String.valueOf(fv)));
    }

    protected void addFacetToDoc(Document doc, String fn, String fv) {
        doc.add(new FacetField(fn, fv));
        doc.add(new TextField(fn,  fv, Field.Store.YES));
    }

    protected void addNumToDoc(Document doc, String fn, long fv) {
        doc.add(new NumericDocValuesField(fn, fv));
        doc.add(new StoredField(fn, fv));
    }

}
