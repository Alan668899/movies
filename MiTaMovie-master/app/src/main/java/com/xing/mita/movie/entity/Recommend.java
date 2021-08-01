package com.xing.mita.movie.entity;

import android.text.TextUtils;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * @author Mita
 * @date 2018/10/9
 * @Description
 */
public class Recommend implements MultiItemEntity {

    private String type;
    private String name;
    private String id;
    private String request;

    public Recommend() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int getItemType() {
        return TextUtils.equals(type, "title") ? 1 : 0;
    }
}
