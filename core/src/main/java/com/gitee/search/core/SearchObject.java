package com.gitee.search.core;

import org.apache.lucene.document.*;

import java.util.List;
import java.util.Map;

/**
 * Searchable Object
 * @author Winter Lau (javayou@gmail.com)
 */
public interface SearchObject extends Comparable<SearchObject> {

    public String FIELD_NAME_ID = "id";
    public String FIELD_NAME_CLASS = "__class__";

    /**
     * get searchable object id
     * @return
     */
    long id();

    /**
     * set searchable object id
     * @param id
     */
    void id(long id);

    /**
     * fields name to be stored
     * @return
     */
    List<String> storeFields();

    /**
     * other store data attached to this object
     * @return
     */
    default Map<String, String> extendStoreData() {
        return null;
    }

    /**
     * fields name to be indexed
     * @return
     */
    List<String> indexFields();

    /**
     * other indexing data attached to this object
     * @return
     */
    default Map<String, String> extendIndexData() {
        return null;
    }

    /**
     * list N objects where id bigger than @id
     *
     * @param id
     * @param count
     * @return
     */
    List<? extends SearchObject> list(long id, int count);

}
