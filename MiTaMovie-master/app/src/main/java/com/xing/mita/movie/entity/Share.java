package com.xing.mita.movie.entity;

/**
 * @author Mita
 * @date 2018/11/3
 * @Description
 */
public class Share {

    private String name;
    private int icon;
    private String bgColor;

    public Share() {
    }

    public Share( String name, int icon, String bgColor) {
        this.name = name;
        this.icon = icon;
        this.bgColor = bgColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }
}
