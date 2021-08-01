package com.xing.mita.movie.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Mita
 * @date 2018/10/24
 * @Description
 */
@Entity
public class SearchHistory {

    @Id(autoincrement = true)
    private Long id;
    @Property
    private String keyword;

    public SearchHistory() {
    }

    public SearchHistory(String keyword) {
        this.keyword = keyword;
    }

    @Generated(hash = 98012354)
    public SearchHistory(Long id, String keyword) {
        this.id = id;
        this.keyword = keyword;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
