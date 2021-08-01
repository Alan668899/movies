package com.xing.mita.movie.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Mita
 * @date 2018/10/30
 * @Description
 */
@Entity
public class Connection {

    @Id(autoincrement = true)
    private Long id;
    @Property
    private String link;
    @Property
    private String name;
    @Property
    private String image;
    @Property
    private String intro;
    @Property
    private String source;

    public Connection() {
    }

    @Generated(hash = 820796291)
    public Connection(Long id, String link, String name, String image, String intro,
            String source) {
        this.id = id;
        this.link = link;
        this.name = name;
        this.image = image;
        this.intro = intro;
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public Connection setId(Long id) {
        this.id = id;
        return this;
    }

    public String getLink() {
        return link;
    }

    public Connection setLink(String link) {
        this.link = link;
        return this;
    }

    public String getName() {
        return name;
    }

    public Connection setName(String name) {
        this.name = name;
        return this;
    }

    public String getImage() {
        return image;
    }

    public Connection setImage(String image) {
        this.image = image;
        return this;
    }

    public String getIntro() {
        return intro;
    }

    public Connection setIntro(String intro) {
        this.intro = intro;
        return this;
    }

    public String getSource() {
        return source;
    }

    public Connection setSource(String source) {
        this.source = source;
        return this;
    }
}
