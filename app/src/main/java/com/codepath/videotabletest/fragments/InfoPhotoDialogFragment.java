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
import com.codepath.videotabletest.activities.ImagePlayerActivity;
import com.codepath.videotabletest.adapters.PhoTagsAdapter;
import com.codepath.videotabletest.models.PhoTag;
import com.codepath.videotabletest.models.Photo;

import java.util.ArrayList;
import java.util.List;

public class InfoPhotoDialogFragment extends DialogFragment {
    DatabaseHelper databaseHelper;
    Photo photo;
    String  uriStr;
    List<String> categories = new ArrayList<>();
    TextView tvNocat;


    public InfoPhotoDialogFragment() {
    }


    public static InfoPhotoDialogFragment newInstance(String uri) {
        InfoPhotoDialogFragment f = new InfoPhotoDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("uriStr", uri);
        f.setArguments(args);

        return f;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_infophoto, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        uriStr = getArguments().getString("uriStr");
        databaseHelper = DatabaseHelper.getInstance(getActivity());
        int id = databaseHelper.getPhotoID(uriStr);
        photo = databaseHelper.getPhoto(id);
        TextView vidname = (TextView) view.findViewById(R.id.tvName);
        TextView vidlocation = (TextView) view.findViewById(R.id.tvLocation);
        TextView viddate = (TextView) view.findViewById(R.id.tvEntDate);
        tvNocat =(TextView) view.findViewById(R.id.tvNoCat);
        tvNocat.setVisibility(View.INVISIBLE);
        vidname.setText(photo.name);
        vidlocation.setText(photo.location);
        viddate.setText(photo.date);
        HorizontalGridView phocat = (HorizontalGridView) view.findViewById(R.id.gvCategoriesPhoto);

        ArrayList<PhoTag> items = new ArrayList<PhoTag>();
        List<PhoTag> allPhoTags = databaseHelper.getAssociatedPhoTags(photo);
        for (PhoTag phoTag: allPhoTags) {
            tvNocat.setVisibility(View.INVISIBLE);
            items.add(0, phoTag);
        }
        final PhoTagsAdapter adapter = new PhoTagsAdapter(getActivity(), items);
        phocat.setAdapter(adapter);

        if(items.isEmpty()){
            tvNocat.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        ImagePlayerActivity activity = (ImagePlayerActivity) getActivity();
        activity.setIsFragment(false);
    }
}