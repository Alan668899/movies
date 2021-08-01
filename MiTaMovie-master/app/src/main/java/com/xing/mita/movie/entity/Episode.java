package com.xing.mita.movie.entity;

/**
 * @author Mita
 * @date 2018/10/16
 * @Description 剧集
 */
public class Episode {

    private String link;
    private String title;
    private boolean select;

    public Episode() {
    }

    public Episode(String link, String title) {
        this.link = link;
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public Episode setLink(String link) {
        this.link = link;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Episode setTitle(String title) {
        this.title = title;
        return this;
    }

    public boolean isSelect() {
        return select;
    }

    public Episode setSelect(boolean select) {
        this.select = select;
        return this;
    }
}
