package com.codepath.videotabletest.models;

/**
 * Created by ramyarao on 7/23/16.
 */
public class FacebookVideo {
    public String id;
    public String picture;
    public String url;
    public String date;
    public String name;
    public FacebookVideo(String id, String picture, String url, String date, String name) {
        this.id = id;
        this.picture = picture;
        this.url = url;
        this.date = date;
        this.name = name;
    }
    public FacebookVideo() {

    }
}
