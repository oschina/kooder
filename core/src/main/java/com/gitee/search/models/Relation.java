package com.gitee.search.models;

import java.util.HashMap;

/**
 * Relation info
 * @author Winter Lau<javayou@gmail.com>
 */
public final class Relation {

    public final static Relation EMPTY = new Relation(0, "NONE", null);

    protected int id;
    protected String name;
    protected String url;

    public Relation() {}

    public Relation(int id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object obj) {
        return id == ((Relation)obj).id;
    }
}
