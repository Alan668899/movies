package com.xing.mita.movie.entity;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * @author Mita
 * @date 2019/1/17
 * @Description 频道
 */
public class Channel implements MultiItemEntity {

    private Long id;
    private String title;
    private String name;
    private String link;
    private String pinYin;
    private String paradeUrl;
    /**
     * 0：标题
     * 1：推荐
     * 2：正常
     */
    private int type;

    public Channel() {
    }

    public Long getId() {
        return id;
    }

    public Channel setId(Long id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Channel setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getName() {
        return name;
    }

    public Channel setName(String name) {
        this.name = name;
        return this;
    }

    public String getLink() {
        return link;
    }

    public Channel setLink(String link) {
        this.link = link;
        return this;
    }

    public int getType() {
        return type;
    }

    public Channel setType(int type) {
        this.type = type;
        return this;
    }

    public String getPinYin() {
        return pinYin;
    }

    public Channel setPinYin(String pinYin) {
        this.pinYin = pinYin;
        return this;
    }

    public String getParadeUrl() {
        return paradeUrl;
    }

    public Channel setParadeUrl(String paradeUrl) {
        this.paradeUrl = paradeUrl;
        return this;
    }

    @Override
    public int getItemType() {
        return type;
    }
}
