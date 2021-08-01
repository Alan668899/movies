package com.xing.mita.movie.entity;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Mita
 * @date 2018/11/1
 * @Description 观看记录
 */
@Entity
public class MovieHistory implements MultiItemEntity {

    @Id(autoincrement = true)
    private Long id;
    @Property
    private String name;
    @Property
    private String image;
    @Property
    private String link;
    @Property
    private String subLink;
    @Property
    private String episode;
    @Property
    private String source;
    @Property
    private String finalPlayUrl;
    @Property
    private String webSite;
    @Property
    private long progress;
    @Property
    private long during;
    @Property
    private long date;
    @Property
    private int position;
    private int type;

    public MovieHistory() {
    }

    @Generated(hash = 1586681326)
    public MovieHistory(Long id, String name, String image, String link,
            String subLink, String episode, String source, String finalPlayUrl,
            String webSite, long progress, long during, long date, int position,
            int type) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.link = link;
        this.subLink = subLink;
        this.episode = episode;
        this.source = source;
        this.finalPlayUrl = finalPlayUrl;
        this.webSite = webSite;
        this.progress = progress;
        this.during = during;
        this.date = date;
        this.position = position;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public MovieHistory setId(Long id) {
        this.id = id;
        return this;
    }

    public String getImage() {
        return image;
    }

    public MovieHistory setImage(String image) {
        this.image = image;
        return this;
    }

    public String getSubLink() {
        return subLink;
    }

    public MovieHistory setSubLink(String subLink) {
        this.subLink = subLink;
        return this;
    }

    public String getSource() {
        return source;
    }

    public MovieHistory setSource(String source) {
        this.source = source;
        return this;
    }

    public long getProgress() {
        return progress;
    }

    public MovieHistory setProgress(long progress) {
        this.progress = progress;
        return this;
    }

    public long getDuring() {
        return during;
    }

    public MovieHistory setDuring(long during) {
        this.during = during;
        return this;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public MovieHistory setName(String name) {
        this.name = name;
        return this;
    }

    public String getLink() {
        return link;
    }

    public MovieHistory setLink(String link) {
        this.link = link;
        return this;
    }

    public int getType() {
        return type;
    }

    public MovieHistory setType(int type) {
        this.type = type;
        return this;
    }

    public String getEpisode() {
        return episode;
    }

    public MovieHistory setEpisode(String episode) {
        this.episode = episode;
        return this;
    }

    public String getFinalPlayUrl() {
        return finalPlayUrl;
    }

    public MovieHistory setFinalPlayUrl(String finalPlayUrl) {
        this.finalPlayUrl = finalPlayUrl;
        return this;
    }

    public String getWebSite() {
        return webSite;
    }

    public MovieHistory setWebSite(String webSite) {
        this.webSite = webSite;
        return this;
    }

    public int getPosition() {
        return position;
    }

    public MovieHistory setPosition(int position) {
        this.position = position;
        return this;
    }

    @Override
    public int getItemType() {
        return type;
    }
}
