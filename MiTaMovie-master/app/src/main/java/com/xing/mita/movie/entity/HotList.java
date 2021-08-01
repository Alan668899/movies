package com.xing.mita.movie.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Mita
 * @date 2018/10/25
 * @Description 热门推荐
 */
@Entity
public class HotList {

    @Id(autoincrement = true)
    private Long id;
    @Property
    private String name;
    @Property
    private String type;
    @Property
    private String link;
    @Property
    private String source;

    public HotList() {
    }

    public HotList(String name, String type, String link, String source) {
        this.name = name;
        this.type = type;
        this.link = link;
        this.source = source;
    }

    @Generated(hash = 1140975135)
    public HotList(Long id, String name, String type, String link, String source) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.link = link;
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
