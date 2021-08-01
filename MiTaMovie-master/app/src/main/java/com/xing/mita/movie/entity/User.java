package com.xing.mita.movie.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Mita
 * @date 2019/1/9
 * @Description 建议反馈用户
 */
@Entity
public class User {

    @Id(autoincrement = true)
    private Long id;
    @Property
    private String nick;
    @Property
    private String headIcon;
    @Property
    private String openId;

    public User() {
    }

    @Generated(hash = 1450462555)
    public User(Long id, String nick, String headIcon, String openId) {
        this.id = id;
        this.nick = nick;
        this.headIcon = headIcon;
        this.openId = openId;
    }

    public Long getId() {
        return id;
    }

    public User setId(Long id) {
        this.id = id;
        return this;
    }

    public String getNick() {
        return nick;
    }

    public User setNick(String nick) {
        this.nick = nick;
        return this;
    }

    public String getHeadIcon() {
        return headIcon;
    }

    public User setHeadIcon(String headIcon) {
        this.headIcon = headIcon;
        return this;
    }

    public String getOpenId() {
        return openId;
    }

    public User setOpenId(String openId) {
        this.openId = openId;
        return this;
    }
}
