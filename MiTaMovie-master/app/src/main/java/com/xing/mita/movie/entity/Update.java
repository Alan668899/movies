package com.xing.mita.movie.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Mita
 * @date 2018/11/21
 * @Description apk升级
 */
@Entity
public class Update {

    @Id
    private Long id;
    @Property
    private int versionCode;
    @Property
    private String versionName;
    @Property
    private String content;
    @Property
    private String url;
    @Property
    private boolean hasDownload;

    public Update() {
    }

    @Generated(hash = 562625296)
    public Update(Long id, int versionCode, String versionName, String content,
            String url, boolean hasDownload) {
        this.id = id;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.content = content;
        this.url = url;
        this.hasDownload = hasDownload;
    }

    public Long getId() {
        return id;
    }

    public Update setId(Long id) {
        this.id = id;
        return this;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public Update setVersionCode(int versionCode) {
        this.versionCode = versionCode;
        return this;
    }

    public String getVersionName() {
        return versionName;
    }

    public Update setVersionName(String versionName) {
        this.versionName = versionName;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Update setContent(String content) {
        this.content = content;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Update setUrl(String url) {
        this.url = url;
        return this;
    }

    public boolean isHasDownload() {
        return hasDownload;
    }

    public Update setHasDownload(boolean hasDownload) {
        this.hasDownload = hasDownload;
        return this;
    }

    public boolean getHasDownload() {
        return this.hasDownload;
    }

}
