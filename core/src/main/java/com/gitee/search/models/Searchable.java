package com.gitee.search.models;

import org.apache.lucene.document.Document;

/**
 * Searchable object
 * @author Winter Lau<javayou@gmail.com>
 */
public abstract class Searchable {

    public final static int VISIBILITY_PRIVATE  = 0;
    public final static int VISIBILITY_INTERNAL = 1;
    public final static int VISIBILITY_PUBLIC   = 2;

    protected long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

}
