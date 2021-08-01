package com.xing.mita.movie.entity;

/**
 * @author Mita
 * @date 2019/1/24
 * @Description 直播源
 */
public class Hdp {

    private String name;
    private String url;

    public Hdp(String name, String url) {
        this.name = name;
        this.url = url;
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
}
