package com.codepath.videotabletest.models;

/**
 * Created by ramyarao on 7/6/16.
 */
public class Video implements Comparable<Video>, Media {
    public String uri;
    public String name;
    public String location;
    public String date;
    public String thumbnail;
    public String orientation;
    public String url;


    @Override
    public int compareTo(Video v) {
        return this.uri.compareTo(v.uri);
    }

    @Override
    public boolean isVideo() {
        return true;
    }

    @Override
    public String getThumbnail() {
        return null;
    }

    @Override
    public String getUri() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    public Video getVideo() {
        return this;
    }

    @Override
    public Photo getPhoto() {
        return null;
    }
}

