package com.xing.mita.movie.entity;

import com.chad.library.adapter.base.entity.SectionEntity;

/**
 * @author Mita
 * @date 2018/10/12
 * @Description
 */
public class VideoMulti extends SectionEntity {

    private int titleImg;
    private String title;
    private String actor;
    private String picture;
    private String score;
    private String link;
    private String name;
    private String source;

    public VideoMulti(boolean isHeader, String header) {
        super(isHeader, header);
    }

    public String getTitle() {
        return title;
    }

    public VideoMulti setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getLink() {
        return link;
    }

    public VideoMulti setLink(String link) {
        this.link = link;
        return this;
    }

    public String getActor() {
        return actor;
    }

    public VideoMulti setActor(String actor) {
        this.actor = actor;
        return this;
    }

    public String getPicture() {
        return picture;
    }

    public VideoMulti setPicture(String picture) {
        this.picture = picture;
        return this;
    }

    public String getScore() {
        return score;
    }

    public VideoMulti setScore(String score) {
        this.score = score;
        return this;
    }

    public String getName() {
        return name;
    }

    public VideoMulti setName(String name) {
        this.name = name;
        return this;
    }

    public int getTitleImg() {
        return titleImg;
    }

    public VideoMulti setTitleImg(int titleImg) {
        this.titleImg = titleImg;
        return this;
    }

    public String getSource() {
        return source;
    }

    public VideoMulti setSource(String source) {
        this.source = source;
        return this;
    }
}
