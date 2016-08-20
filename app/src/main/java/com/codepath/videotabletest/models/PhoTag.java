package com.codepath.videotabletest.models;

/**
 * Created by ramyarao on 7/20/16.
 */
public class PhoTag {
    public Photo photo;
    public String label;

    public PhoTag (){

    }

    public PhoTag (Photo photo, String label) {
        this.photo = photo;
        this.label = label;
    }
}
