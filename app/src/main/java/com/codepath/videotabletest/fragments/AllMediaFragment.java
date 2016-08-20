package com.codepath.videotabletest.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.codepath.videotabletest.DatabaseHelper;
import com.codepath.videotabletest.R;
import com.codepath.videotabletest.activities.ImagePlayerActivity;
import com.codepath.videotabletest.activities.MainActivity;
import com.codepath.videotabletest.activities.VideoPlayerActivity;
import com.codepath.videotabletest.adapters.MediaAdapter;
import com.codepath.videotabletest.models.Media;
import com.codepath.videotabletest.models.Photo;
import com.codepath.videotabletest.models.Video;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AllMediaFragment extends Fragment{
    public static final String ARG_PAGE = "ARG_PAGE";
    DatabaseHelper databaseHelper;
    MediaAdapter mediaAdapter;
    List<Media> medias = new ArrayList<>();
    GridView mediaGrid;
    List<Media> mediasAll = new ArrayList<>();
    SearchView searchView;
    MenuItem searchItem;
    String searchTag = null;
    ImageView ivNomedia;
    View ivthumbail;

    private int mPage;

    public static AllMediaFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        AllMediaFragment fragment = new AllMediaFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
        databaseHelper = DatabaseHelper.getInstance(getActivity());
        setHasOptionsMenu(true);
    }

    // Inflate the fragment layout we defined above for this fragment
    // Set the associated text for the title
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_allmedia, container, false);
        //mediaAdapter = new MediaAdapter(getActivity(), medias);
        mediaGrid = (GridView) view.findViewById(R.id.mediaGrid);
        populateFeed();
        return view;
    }

    private void setupListViewListener2() {

        mediaGrid.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter,
                                            View item, int pos, long id) {
                        Media media = medias.get(pos);
                        if (media.isVideo()) {
                            Video vid = media.getVideo();
                            ivthumbail = (View) getView().findViewById(R.id.ivThumbnail);
                            Intent i = new Intent(getActivity(), VideoPlayerActivity.class);
                            i.putExtra("uri", vid.uri);
                            i.putExtra("searchTag", searchTag);
                            startActivity(i);
                        }
                        else {
                            Photo pho1 = media.getPhoto();
                            Intent i = new Intent(getActivity(), ImagePlayerActivity.class);
                            i.putExtra("imageUri", pho1.uri);
                            startActivity(i);
                        }
                    }
                }
        );
        mediaGrid.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View item, int pos,
                                                   long id) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                        adb.setTitle("Delete");
                        Media media = medias.get(pos);
                        if (media.isVideo()) {
                            Video vidTitle = media.getVideo();
                            adb.setMessage("Are you sure you want to delete this video?");
                            final int positionToRemove = pos;
                            adb.setNegativeButton("Cancel", null);
                            adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Video vid = medias.get(positionToRemove).getVideo();
                                    databaseHelper.deleteVideo(vid);
                                    populateFeed();
                                    Toast.makeText(getActivity(), "Deleted video", Toast.LENGTH_SHORT).show();
                                }
                            });
                            adb.show();
                            return true;
                        } else {
                            Photo photo = media.getPhoto();
                            adb.setMessage("Are you sure you want to delete this photo?");
                            final int positionToRemove = pos;
                            adb.setNegativeButton("Cancel", null);
                            adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Photo pho = medias.get(positionToRemove).getPhoto();
                                    databaseHelper.deletePhoto(pho);
                                    populateFeed();
                                    Toast.makeText(getActivity(), "Deleted photo", Toast.LENGTH_SHORT).show();
                                }
                            });
                            adb.show();
                            return true;
                        }
                    }

                });
    }

    public void search(String searchTag){
        medias.clear();
        medias.addAll(databaseHelper.getSearchResults(searchTag));
        medias.addAll(databaseHelper.getSearchResultsName(searchTag));
        medias.addAll(databaseHelper.getSearchResultsLocation(searchTag));
        medias.addAll(databaseHelper.getPhotoSearchResults(searchTag));
        medias.addAll(databaseHelper.getPhotoSearchResultsName(searchTag));
        medias.addAll(databaseHelper.getPhotoSearchResultsLocation(searchTag));
        if(medias.isEmpty() || mediasAll.isEmpty()){
            ivNomedia.setImageResource(R.drawable.nosearch);
            ivNomedia.setVisibility(View.VISIBLE);
        }else{
            ivNomedia.setImageResource(R.drawable.nosearch);
            ivNomedia.setVisibility(View.INVISIBLE);
        }
        MediaAdapter adapter = new MediaAdapter(getActivity(), medias);
        mediaGrid.setAdapter(adapter);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_search){
            searchItem = item;
            searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchTag = searchView.getQuery().toString();
                    search(searchTag);
                    searchView.clearFocus();
                    return true;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });

            MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    populateFeed();
                    return true;
                }
            });
        }
        return true;
    }

    public void notifyAdapterChanged(){
        //if(mediaAdapter!=null) {
            populateFeed();
            ivNomedia = (ImageView) getView().findViewById(R.id.ivNoMedia);
//            medias.clear();
//            if (mPage == 1) {
//                medias.addAll(databaseHelper.getAllVideos());
//                medias.addAll(databaseHelper.getAllPhotos());
//                mediasAll.addAll(databaseHelper.getAllVideos());
//                mediasAll.addAll(databaseHelper.getAllPhotos());
//            }
//            else if(mPage ==2){
//                medias.addAll(databaseHelper.getAllVideos());
//                mediasAll.addAll(databaseHelper.getAllVideos());
//            }
//            else if(mPage ==3){
//                medias.addAll(databaseHelper.getAllPhotos());
//                mediasAll.addAll(databaseHelper.getAllPhotos());
//            }
            if(medias.isEmpty() || mediasAll.isEmpty()){
                ivNomedia.setImageResource(R.drawable.nomediafound);
                ivNomedia.setVisibility(View.VISIBLE);
            }else{
                ivNomedia.setVisibility(View.INVISIBLE);
            }
            mediaAdapter.notifyDataSetChanged();
       // }
    }

    public void populateFeed() {
        MainActivity mainActivity = (MainActivity) getActivity();
        String sortType = mainActivity.getSortType();
        medias.clear();
        List<Video> allVideos = databaseHelper.getAllVideos();
        List<Photo> allPhotos = databaseHelper.getAllPhotos();
        if(sortType.equals("date")) {
            if (mPage == 1 || mPage == 3) {
                for (Photo photo: allPhotos) {
                    medias.add(0, photo);
                    mediasAll.add(0, photo);
                }
            }
            if (mPage == 1 || mPage == 2) {
                for (Video video: allVideos) {
                    medias.add(0, video);
                    mediasAll.add(0, video);
                }
            }
        } else {
            Map<String, Media> mediaMap = new TreeMap<>();
            String key;
            int counter = 0;
            if(mPage == 1 || mPage == 2) {
                for (Video video: allVideos) {
                    key = video.name;
                    if (key == null) {
                        key = "";
                    }
                    if (mediaMap.containsKey(key)) {
                        counter++;
                        key += String.valueOf(counter);
                    }
                    mediaMap.put(key, video);
                }
            }
            if (mPage == 1 || mPage == 3) {
                for (Photo photo: allPhotos) {
                    key = photo.name;
                    if (key == null) {
                        key = "";
                    }
                    if (mediaMap.containsKey(key)){
                        counter++;
                        key+= String.valueOf(counter);
                    }
                    mediaMap.put(key,photo);
                }
            }
            for (String mapKey: mediaMap.keySet()) {
                medias.add(mediaMap.get(mapKey));
                mediasAll.add(mediaMap.get(mapKey));
            }
        }
        mediaAdapter = new MediaAdapter(getActivity(), medias);
        mediaGrid.setAdapter(mediaAdapter);
//        ivNomedia = (ImageView) getView().findViewById(R.id.ivNoMedia);
//        if(medias.isEmpty() || mediasAll.isEmpty()){
//            ivNomedia.setImageResource(R.drawable.nomediafound);
//            ivNomedia.setVisibility(View.VISIBLE);
//        }else{
//            ivNomedia.setVisibility(View.INVISIBLE);
//        }
        setupListViewListener2();
    }


    //SORT BY DATE ATTRIBUTE

    //            double key;
//            int counter = 0;
//            SimpleDateFormat oldFormat = new SimpleDateFormat("MMM dd, yyyy");
//            SimpleDateFormat newFormat = new SimpleDateFormat("yyyyMMdd");
//            if(mPage == 1 || mPage == 2) {
//                for (Video video : allVideos) {
//                    try {
//                        Date date = oldFormat.parse(video.date);
//                        key = Integer.parseInt(newFormat.format(date));
//                        if (mediaMap.containsKey(key)) {
//                            counter++;
//                            key += (counter * .01);
//                        }
//                        mediaMap.put(key, video);
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            if (mPage == 1 || mPage == 3) {
//                for (Photo photo : allPhotos) {
//                    try {
//                        if (photo.date != null) {
//                            Date date = oldFormat.parse(photo.date);
//                            key = Integer.parseInt(newFormat.format(date));
//                        } else {
//                            key = 0 + counter;
//                        }
//                        mediaMap.put(key, photo);
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            for (double mapKey : mediaMap.keySet()) {
//                medias.add(0, mediaMap.get(mapKey));
//                mediasAll.add(0, mediaMap.get(mapKey));
//            }
//            mediaAdapter.notifyDataSetChanged();

}




