package com.xing.mita.movie.entity;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.entity.SectionEntity;

/**
 * @author Mita
 * @date 2018/10/12
 * @Description
 */
public class Video extends SectionEntity implements MultiItemEntity {

    private int type;
    private int titleImg;
    private String title;
    private String actor;
    private String picture;
    private String score;
    private String link;
    private String name;
    private String source;

    public Video(boolean isHeader, String header) {
        super(isHeader, header);
    }

    public String getTitle() {
        return title;
    }

    public Video setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getLink() {
        return link;
    }

    public Video setLink(String link) {
        this.link = link;
        return this;
    }

    public String getActor() {
        return actor;
    }

    public Video setActor(String actor) {
        this.actor = actor;
        return this;
    }

    public String getPicture() {
        return picture;
    }

    public Video setPicture(String picture) {
        this.picture = picture;
        return this;
    }

    public String getScore() {
        return score;
    }

    public Video setScore(String score) {
        this.score = score;
        return this;
    }

    public String getName() {
        return name;
    }

    public Video setName(String name) {
        this.name = name;
        return this;
    }

    public int getTitleImg() {
        return titleImg;
    }

    public Video setTitleImg(int titleImg) {
        this.titleImg = titleImg;
        return this;
    }

    public String getSource() {
        return source;
    }

    public Video setSource(String source) {
        this.source = source;
        return this;
    }

    @Override
    public int getItemType() {
        return type;
    }

    public int getType() {
        return type;
    }

    public Video setType(int type) {
        this.type = type;
        return this;
    }
}
