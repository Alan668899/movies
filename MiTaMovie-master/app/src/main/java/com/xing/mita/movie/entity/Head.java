package com.xing.mita.movie.entity;

/**
 * @author Mita
 * @date 2019/1/10
 * @Description 头像
 */
public class Head {

    private String url;
    private boolean select;

    public Head(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }
}
