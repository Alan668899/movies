package com.xing.mita.movie.entity;

/**
 * @author Mita
 * @date 2019/1/24
 * @Description
 */
public class Parade {

    private String time;
    private String content;

    public Parade(String time, String content) {
        this.time = time;
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
