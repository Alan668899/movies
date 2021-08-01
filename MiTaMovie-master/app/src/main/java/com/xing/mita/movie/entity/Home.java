package com.xing.mita.movie.entity;

import com.chad.library.adapter.base.entity.SectionEntity;

/**
 * @author Mita
 * @date 2018/10/11
 * @Description
 */
public class Home extends SectionEntity {

    private String title;
    private String titleLink;
    private int titleIcon;
    private String contentLink;
    private String contentName;
    private String contentPic;
    private String contentLabel;
    private int type;

    public Home(boolean isHeader, String header) {
        super(isHeader, header);
    }

    public String getTitle() {
        return title;
    }

    public Home setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitleLink() {
        return titleLink;
    }

    public Home setTitleLink(String titleLink) {
        this.titleLink = titleLink;
        return this;
    }

    public int getTitleIcon() {
        return titleIcon;
    }

    public Home setTitleIcon(int titleIcon) {
        this.titleIcon = titleIcon;
        return this;
    }

    public String getContentLink() {
        return contentLink;
    }

    public Home setContentLink(String contentLink) {
        this.contentLink = contentLink;
        return this;
    }

    public String getContentName() {
        return contentName;
    }

    public Home setContentName(String contentName) {
        this.contentName = contentName;
        return this;
    }

    public String getContentPic() {
        return contentPic;
    }

    public Home setContentPic(String contentPic) {
        this.contentPic = contentPic;
        return this;
    }

    public String getContentLabel() {
        return contentLabel;
    }

    public Home setContentLabel(String contentLabel) {
        this.contentLabel = contentLabel;
        return this;
    }

    public int getType() {
        return type;
    }

    public Home setType(int type) {
        this.type = type;
        return this;
    }

}
