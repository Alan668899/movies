package com.xing.mita.movie.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Mita
 * @date 2019/1/23
 * @Description 电视节目
 */
@Entity
public class Program {

    @Id(autoincrement = true)
    private Long id;
    @Property
    private String name;
    @Property
    private String paradeUrl;
    @Property
    private String source;

    public Program() {
    }

    @Generated(hash = 88472944)
    public Program(Long id, String name, String paradeUrl, String source) {
        this.id = id;
        this.name = name;
        this.paradeUrl = paradeUrl;
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public Program setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Program setName(String name) {
        this.name = name;
        return this;
    }

    public String getParadeUrl() {
        return paradeUrl;
    }

    public Program setParadeUrl(String paradeUrl) {
        this.paradeUrl = paradeUrl;
        return this;
    }

    public String getSource() {
        return source;
    }

    public Program setSource(String source) {
        this.source = source;
        return this;
    }
}
