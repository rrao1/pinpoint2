package com.codepath.videotabletest.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.codepath.videotabletest.R;
import com.codepath.videotabletest.models.Video;

import java.io.File;
import java.util.List;


public class VideoAdapter2 extends ArrayAdapter<Video> {

    private static class ViewHolder {
        ImageView pic;
    }

    public VideoAdapter2(Context context, List<Video> videos) {
        super(context, android.R.layout.simple_list_item_1, videos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Video video = getItem(position);

        ViewHolder viewHolder;
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            viewHolder = new ViewHolder();
            int type = getItemViewType(position);
            convertView = getInflatedLayoutForType(type);
            viewHolder.pic = (ImageView) convertView.findViewById(R.id.ivThumbnail);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.pic.setImageResource(0);
        File imgFile = new File(video.thumbnail);
        if(imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            viewHolder.pic.setImageBitmap(myBitmap);
        }
        return convertView;
    }

    private View getInflatedLayoutForType(int type) {
        return LayoutInflater.from(getContext()).inflate(R.layout.item_video, null);
    }
}