package com.codepath.videotabletest.models;

/**
 * Created by ramyarao on 7/20/16.
 */
public class Photo implements Media {
    public String uri;
    public String name;
    public String location;
    public String date;
    public String orientation;


    @Override
    public boolean isVideo() {
        return false;
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
        return null;
    }

    @Override
    public Photo getPhoto() {
        return this;
    }
}
