package com.gitee.kooder.models;

/**
 * Relation info
 * @author Winter Lau<javayou@gmail.com>
 */
public final class Relation {

    public final static Relation EMPTY = new Relation(0, "NONE", null);

    protected long id;
    protected String name;
    protected String url;

    public Relation() {}

    public Relation(long id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
