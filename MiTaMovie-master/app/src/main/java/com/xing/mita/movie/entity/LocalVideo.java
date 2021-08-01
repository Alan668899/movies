package com.xing.mita.movie.entity;

public class LocalVideo {

    private String name;
    private String path;
    private String during;
    private String size;
    /**
     * 0：常规
     * 1：编辑
     * 2：选中
     */
    private int status;

    public LocalVideo() {
    }

    public String getName() {
        return name;
    }

    public LocalVideo setName(String name) {
        this.name = name;
        return this;
    }

    public String getPath() {
        return path;
    }

    public LocalVideo setPath(String path) {
        this.path = path;
        return this;
    }

    public String getDuring() {
        return during;
    }

    public LocalVideo setDuring(String during) {
        this.during = during;
        return this;
    }

    public String getSize() {
        return size;
    }

    public LocalVideo setSize(String size) {
        this.size = size;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
