package com.computer.mazhihuapp.model;

import java.util.List;

/**
 * Created by wwjun.wang on 2015/8/14.
 */
public class Before {

    private List<Latest.stories> stories;
    private String date;

    public List<Latest.stories> getStories() {
        return stories;
    }

    public void setStories(List<Latest.stories> stories) {
        this.stories = stories;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getDate() {
        return date;
    }


}
