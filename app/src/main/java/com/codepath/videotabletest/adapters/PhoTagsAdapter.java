package com.codepath.videotabletest.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codepath.videotabletest.R;
import com.codepath.videotabletest.activities.ImagePlayerActivity;
import com.codepath.videotabletest.models.PhoTag;

import java.util.List;

/**
 * Created by mfdavis on 7/27/16.
 */

public class PhoTagsAdapter extends RecyclerView.Adapter<PhoTagsAdapter.SimpleViewHolder>{

    private Context context;
    private List<PhoTag> elements;

    public PhoTagsAdapter(Context context, List<PhoTag> tags){
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
            if(currentActivity.contains("com.codepath.videotabletest.activities.EditActivityPhoto")){
                view = LayoutInflater.from(this.context).inflate(R.layout.item_vidtag_dark, parent, false);
            } else if(currentActivity.contains("com.codepath.videotabletest.activities.ImagePlayerActivity")){
                ImagePlayerActivity activity = (ImagePlayerActivity) this.context;
                if(activity.checkIfFragment()){
                    view = LayoutInflater.from(this.context).inflate(R.layout.item_vidtag_dark, parent, false);
                } else {
                    view = LayoutInflater.from(this.context).inflate(R.layout.item_vidtag, parent, false);
                }
            } else {
                view = LayoutInflater.from(this.context).inflate(R.layout.item_vidtag, parent, false);
            }
        } else {
            view = LayoutInflater.from(this.context).inflate(R.layout.item_vidtag, parent, false);
        }
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhoTagsAdapter.SimpleViewHolder holder, int position) {
        holder.tag.setText(elements.get(position).label);
    }

    public PhoTag getItem(int position) {
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
