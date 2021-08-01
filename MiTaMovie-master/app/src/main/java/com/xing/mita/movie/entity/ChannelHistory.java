package com.xing.mita.movie.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Mita
 * @date 2019/2/14
 * @Description 频道历史记录
 */
@Entity
public class ChannelHistory {

    @Id
    private Long id;
    @Property
    private Long channelId;
    @Property
    private String name;
    @Property
    private Long updateTime;

    public ChannelHistory() {
    }


    @Generated(hash = 1083912980)
    public ChannelHistory(Long id, Long channelId, String name, Long updateTime) {
        this.id = id;
        this.channelId = channelId;
        this.name = name;
        this.updateTime = updateTime;
    }


    public Long getId() {
        return id;
    }

    public ChannelHistory setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getChannelId() {
        return channelId;
    }

    public ChannelHistory setChannelId(Long channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getName() {
        return name;
    }

    public ChannelHistory setName(String name) {
        this.name = name;
        return this;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public ChannelHistory setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
        return this;
    }
}
