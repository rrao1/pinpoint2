package com.codepath.videotabletest.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codepath.videotabletest.R;
import com.codepath.videotabletest.activities.VideoPlayerActivity;
import com.codepath.videotabletest.models.VidTag;

import java.util.List;

public class VidTagsAdapter extends RecyclerView.Adapter<VidTagsAdapter.SimpleViewHolder>{

    private Context context;
    private List<VidTag> elements;

    public VidTagsAdapter(Context context, List<VidTag> tags){
        this.context = context;
        this.elements = tags;

    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final TextView tag;
        public SimpleViewHolder(View view) {
            super(view);
            tag = (TextView) view.findViewById(R.id.tvTagName);
        }
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        String currentActivity = this.context.getClass().getName();
        final View view;
        if(!TextUtils.isEmpty(currentActivity)) {
            if(currentActivity.contains("com.codepath.videotabletest.activities.EditActivity")){
                view = LayoutInflater.from(this.context).inflate(R.layout.item_vidtag_dark, parent, false);
            } else if(currentActivity.contains("com.codepath.videotabletest.activities.VideoPlayerActivity")){
                VideoPlayerActivity activity = (VideoPlayerActivity) this.context;
                if(activity.checkIfFragment()){
                    view = LayoutInflater.from(this.context).inflate(R.layout.item_vidtag_dark, parent, false);
                } else {
                    view = LayoutInflater.from(this.context).inflate(R.layout.item_vidtag, parent, false);
                }
            } else if(currentActivity.contains("com.codepath.videotabletest.activities.AddingTagsActivity")){
                view = LayoutInflater.from(this.context).inflate(R.layout.item_vidtag, parent, false);
            } else {
                view = LayoutInflater.from(this.context).inflate(R.layout.item_vidtag, parent, false);
            }
        } else {
            view = LayoutInflater.from(this.context).inflate(R.layout.item_vidtag, parent, false);
        }
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        holder.tag.setText(elements.get(position).label);
    }

    public VidTag getItem(int position) {
        return elements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return this.elements.size();
    }
}
