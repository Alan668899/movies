package com.xing.mita.movie.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;

/**
 * @author Mita
 * @date 2019/2/18
 * @Description
 */
@Entity
public class Download {

    @Id
    private Long id;
    @Property
    private String name;
    @Property
    private String episode;
    @Property
    private String thumb;
    @Property
    private String url;
    @Property
    private String cacheName;
    @Property
    private int priority;
    /**
     * 0：正常
     * 1：等待下载
     * 2：手动暂停
     * 3：下载失败
     */
    @Property
    private int status;

    @Transient
    private String speed;
    @Transient
    private String cacheSize;
    @Transient
    private int progress;

    public Download() {
    }

    @Generated(hash = 288870652)
    public Download(Long id, String name, String episode, String thumb, String url,
            String cacheName, int priority, int status) {
        this.id = id;
        this.name = name;
        this.episode = episode;
        this.thumb = thumb;
        this.url = url;
        this.cacheName = cacheName;
        this.priority = priority;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Download setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Download setName(String name) {
        this.name = name;
        return this;
    }

    public String getEpisode() {
        return episode;
    }

    public Download setEpisode(String episode) {
        this.episode = episode;
        return this;
    }

    public String getThumb() {
        return thumb;
    }

    public Download setThumb(String thumb) {
        this.thumb = thumb;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Download setUrl(String url) {
        this.url = url;
        return this;
    }

    public int getProgress() {
        return progress;
    }

    public Download setProgress(int progress) {
        this.progress = progress;
        return this;
    }

    public String getCacheName() {
        return cacheName;
    }

    public Download setCacheName(String cacheName) {
        this.cacheName = cacheName;
        return this;
    }

    public String getSpeed() {
        return speed;
    }

    public Download setSpeed(String speed) {
        this.speed = speed;
        return this;
    }

    public String getCacheSize() {
        return cacheSize;
    }

    public Download setCacheSize(String cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }

    public int getPriority() {
        return priority;
    }

    public Download setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public Download setStatus(int status) {
        this.status = status;
        return this;
    }
}
