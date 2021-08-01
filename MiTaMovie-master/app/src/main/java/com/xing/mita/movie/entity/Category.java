package com.xing.mita.movie.entity;

/**
 * @author Mita
 * @date 2018/10/13
 * @Description
 */
public class Category {

    private String name;
    private String id;
    private boolean select;

    public Category() {
    }

    public Category(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Category setName(String name) {
        this.name = name;
        return this;
    }

    public String getId() {
        return id;
    }

    public Category setId(String id) {
        this.id = id;
        return this;
    }

    public boolean isSelect() {
        return select;
    }

    public Category setSelect(boolean select) {
        this.select = select;
        return this;
    }
}
