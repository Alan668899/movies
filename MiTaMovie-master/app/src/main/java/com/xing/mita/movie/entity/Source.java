package com.xing.mita.movie.entity;

import java.util.List;

/**
 * @author Mita
 * @date 2018/10/16
 * @Description 来源
 */
public class Source {

    private String source;
    private String id;
    private List<Episode> episodeList;

    public Source() {
    }

    public Source(String source, String id, List<Episode> episodeList) {
        this.source = source;
        this.id = id;
        this.episodeList = episodeList;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Episode> getEpisodeList() {
        return episodeList;
    }

    public void setEpisodeList(List<Episode> episodeList) {
        this.episodeList = episodeList;
    }
}
