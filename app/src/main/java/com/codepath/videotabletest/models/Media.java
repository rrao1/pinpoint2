package com.codepath.videotabletest.models;

public interface Media  {
    public boolean isVideo();
    String getThumbnail();
    String getUri();
    String getName();
    String getLocation();
    Video getVideo();
    Photo getPhoto();
}
