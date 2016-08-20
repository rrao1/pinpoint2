package com.codepath.videotabletest.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codepath.videotabletest.DatabaseHelper;
import com.codepath.videotabletest.R;
import com.codepath.videotabletest.activities.VideoPlayerActivity;
import com.codepath.videotabletest.adapters.VidTagsAdapter;
import com.codepath.videotabletest.models.VidTag;
import com.codepath.videotabletest.models.Video;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class InfoVideoDialogFragment extends DialogFragment {
    DatabaseHelper databaseHelper;
    Video video;
    String  uriStr;
    List<String> categories = new ArrayList<>();
    TextView tvNoCats;
    TextView tvNoTags;


    public InfoVideoDialogFragment() {
    }


    public static InfoVideoDialogFragment newInstance(String uri) {
        InfoVideoDialogFragment f = new InfoVideoDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("uriStr", uri);
        f.setArguments(args);

        return f;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_infovideo, container, false);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        VideoPlayerActivity activity = (VideoPlayerActivity) getActivity();
        activity.setIsFragment(false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        uriStr = getArguments().getString("uriStr");
        databaseHelper = DatabaseHelper.getInstance(getActivity());
        int id = databaseHelper.getVideoID(uriStr);
        video = databaseHelper.getVideo(id);
        TextView vidname = (TextView) view.findViewById(R.id.tvNameVid);
        TextView vidlocation = (TextView) view.findViewById(R.id.tvLocationVid);
        TextView viddate = (TextView) view.findViewById(R.id.tvEntDateVid);
        tvNoCats =(TextView) view.findViewById(R.id.tvNOCat);
        tvNoTags =(TextView) view.findViewById(R.id.tvNoTags);
        tvNoCats.setVisibility(View.INVISIBLE);
        tvNoTags.setVisibility(View.INVISIBLE);
        vidname.setText(video.name);
        vidlocation.setText(video.location);
        viddate.setText(video.date);

        HorizontalGridView vidtags = (HorizontalGridView) view.findViewById(R.id.gvCategories);

        ArrayList<VidTag> items = new ArrayList<VidTag>();
        List<VidTag> allVidTags = databaseHelper.getAssociatedVidTags(video);
        for (VidTag vidTag: allVidTags) {
            if (vidTag.time == -1 && !categories.contains(vidTag.label)) {
                tvNoCats.setVisibility(View.INVISIBLE);
                items.add(0, vidTag);
            }
        }
        if(items.isEmpty()){
            tvNoCats.setVisibility(View.VISIBLE);
        }

        final VidTagsAdapter adapter = new VidTagsAdapter(getActivity(), items);
        vidtags.setAdapter(adapter);


        List<VidTag> currentTagsList = new ArrayList<>();
        List<VidTag> associatedVidTags = databaseHelper.getAssociatedVidTags(video);
        Map<Integer,VidTag> vidTagMap = new TreeMap<>();
        for (VidTag vidTag: associatedVidTags) {
            if (vidTag.time != -1) {
                tvNoTags.setVisibility(View.INVISIBLE);
                vidTagMap.put(vidTag.time, vidTag);
            }
        }
        for (int key: vidTagMap.keySet()) {
            currentTagsList.add(vidTagMap.get(key));
        }
        HorizontalGridView vidcategories = (HorizontalGridView) view.findViewById(R.id.gvTagsVid);
        final VidTagsAdapter adapter2 = new VidTagsAdapter(getActivity(),currentTagsList);
        vidcategories.setAdapter(adapter2);
        
        if(vidTagMap.isEmpty()){
            tvNoTags.setVisibility(View.VISIBLE);
        }


    }
}