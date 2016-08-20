package com.codepath.videotabletest.models;

import java.io.Serializable;

/**
 * Created by ramyarao on 7/6/16.
 */
public class VidTag implements Serializable{
    public Video video;
    public String label;
    public int time;

    public VidTag() { }
    public VidTag(Video video, String label, int time) {
        this.video = video;
        this.label = label;
        this.time = time;
    }

}
