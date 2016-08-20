package com.codepath.videotabletest.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.videotabletest.DatabaseHelper;
import com.codepath.videotabletest.R;
import com.codepath.videotabletest.adapters.FragmentPageAdapter;
import com.codepath.videotabletest.adapters.VideoAdapter;
import com.codepath.videotabletest.fragments.AllMediaFragment;
import com.codepath.videotabletest.fragments.FacebookFragment;
import com.codepath.videotabletest.fragments.SingleChoiceDialogFragment;
import com.codepath.videotabletest.models.Media;
import com.codepath.videotabletest.models.Photo;
import com.codepath.videotabletest.models.Video;
import com.facebook.login.LoginManager;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    DatabaseHelper databaseHelper;
    List<Media> medias = new ArrayList<>();
    GridView mediaGrid;
    private LruCache<String, Bitmap> mMemoryCache;
    private static int RESULT_LOAD_VIDEO = 1;
    String imgDecodableString;
    Uri uri;
    Video video;
    String searchTag = null;
    public final String APP_TAG = "Pinpoint";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName;
    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionCaptureVideo, floatingActionCaptureImage, floatingActionUploadMedia, floatingActionFacebook;
    private static final int VIDEO_CAPTURE = 101;
    Uri videoUri;
    SearchView searchView;
    MenuItem searchItem;
    public static String searchTerm;
    List<Media> mediasAll = new ArrayList<>();
    String sortType;
    FragmentPageAdapter fragmentPageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseHelper = DatabaseHelper.getInstance(this);
        List<Photo> photos = databaseHelper.getAllPhotos();
        if (photos.isEmpty()) {
            photoFileName = "photo.jpg";
        } else {
            Photo lastPhoto = photos.get(photos.size() - 1);
            String lastUri = lastPhoto.uri;
            int count = databaseHelper.getPhotoID(lastUri);
            int newCount = count + 1;
            photoFileName = "photo" + newCount + ".jpg";
        }
        //databaseHelper.deleteAllPhoTagsAndPhotos();
        ActionBar actionBar = getSupportActionBar();
        setFloatingActionMenu();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_logo);
        actionBar.setTitle(Html.fromHtml("<font color='#69dea9'>Pinpoint</font>"));
        if (sortType == null) {
            sortType = "date";
        }
        setUpTabs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchTag = searchView.getQuery().toString();
                medias.clear();
                medias.addAll(databaseHelper.getSearchResults(searchTag));
                medias.addAll(databaseHelper.getSearchResultsName(searchTag));
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                medias.clear();
                medias.addAll(databaseHelper.getAllVideos());
                medias.addAll(databaseHelper.getAllPhotos());
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_sort:
                FragmentManager manager = getSupportFragmentManager();
                SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment();

                Bundle bundle = new Bundle();
                bundle.putStringArrayList(SingleChoiceDialogFragment.DATA, getItems());// Require ArrayList
                int selected;
                if (sortType != null && sortType.equals("name")) {
                    selected = 1;
                } else {
                    selected = 0;
                }
                bundle.putInt(SingleChoiceDialogFragment.SELECTED, selected);
                dialog.setArguments(bundle);
                dialog.show(manager, "Dialog");
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private ArrayList<String> getItems()
    {
        ArrayList<String> ret_val = new ArrayList<String>();
        ret_val.add("Date");
        ret_val.add("Name");
        return ret_val;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
        setUpTabs();
    }

    //Fixes problem with Back button not working
    @Override
    public void onBackPressed() {
        medias.clear();
        medias.addAll(databaseHelper.getAllVideos());
        medias.addAll(databaseHelper.getAllPhotos());
        if (getFragmentManager().getBackStackEntryCount() == 0) {
           getSupportFragmentManager().popBackStack();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    public String getSortType() {
        return sortType;
    }

    public void setUpTabs() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        fragmentPageAdapter= new FragmentPageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fragmentPageAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabsStrip.setViewPager(viewPager);
        tabsStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                AllMediaFragment fragment = (AllMediaFragment) fragmentPageAdapter.instantiateItem(viewPager, position);
                if (fragment != null) {
                    fragment.notifyAdapterChanged();
                }

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                AllMediaFragment fragment = (AllMediaFragment) fragmentPageAdapter.instantiateItem(viewPager, position);
                if (fragment != null) {
                    fragment.notifyAdapterChanged();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    public void getVideo(View view){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/* video/*");
        startActivityForResult(intent, RESULT_LOAD_VIDEO);
    }

    //take video
    public void takeVideo() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            File mediaFile = new File(
                    Environment.getExternalStorageDirectory().getAbsolutePath());
            videoUri = Uri.fromFile(mediaFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
            startActivityForResult(intent, VIDEO_CAPTURE);
        } else {
            Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG).show();
        }

    }
    int CAMERA_REQUEST =2;

    //take picture
    public void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_VIDEO && resultCode == RESULT_OK
                    && null != data) {
                uri = data.getData();
                String uriStr = uri.toString();
                if (uriStr.contains("video")) {
                    Video video1 = newVideo(uri);
                    String[] filePathColumn = {MediaStore.Video.Media.DATA};

                    Cursor cursor = getContentResolver().query(uri,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgDecodableString = cursor.getString(columnIndex);
                    File newfile = new File(imgDecodableString);
                    if (newfile.exists()) {
                        Date lastModDate = new Date(newfile.lastModified());
                        String strCuurentDate = lastModDate.toString();
                        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
                        String date = format.format(Date.parse(strCuurentDate));
                        video.date = date;

                    }
                    databaseHelper.addOrUpdateVideo(video1);
                    cursor.close();
                    Intent i = new Intent(MainActivity.this, AddingTagsActivity.class);
                    i.putExtra("VideoUri", video.uri);
                    i.putExtra("main", "yes");
                    startActivity(i);
                }
                else if (uriStr.contains("images")) {
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(uri,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgDecodableString = cursor.getString(columnIndex);
                    File newfile = new File(imgDecodableString);
                    Photo pho = newPhoto(Uri.parse(imgDecodableString));
                    if (newfile.exists()) {
                        Date lastModDate = new Date(newfile.lastModified());
                        String strCuurentDate = lastModDate.toString();
                        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
                        String date = format.format(Date.parse(strCuurentDate));
                        pho.date =date;
                    }
                    databaseHelper.addOrUpdatePhoto(pho);
                    cursor.close();
                    //AllMediaFragment allMediaFragment = (AllMediaFragment) fragmentPageAdapter.getItem(fragmentPageAdapter.getCount()).getFragmentManager().findFragmentById(R.id.viewpager);
                    //allMediaFragment.notifyAdapterChanged();
                    Intent i = new Intent(MainActivity.this,EditActivityPhoto.class);
                    i.putExtra("Image", pho.uri);
                    i.putExtra("main", "yes");
                    startActivity(i);

                }
            }
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    Uri takenPhotoUri = getPhotoFileUri(photoFileName);
                    Bitmap bm = ImagePlayerActivity.rotateBitmapOrientation(takenPhotoUri.getPath());
                    Photo pho = newPhoto(Uri.parse(takenPhotoUri.getPath()));
                    databaseHelper.addOrUpdatePhoto(pho);

                   // AllMediaFragment allMediaFragment = (AllMediaFragment) fragmentPageAdapter.getItem(fragmentPageAdapter.getCount()).getFragmentManager().findFragmentById(R.id.viewpager);
                    //allMediaFragment.notifyAdapterChanged();
                    Intent i = new Intent(MainActivity.this,EditActivityPhoto.class);
                    i.putExtra("Image", pho.uri);
                    i.putExtra("main", "yes");
                    startActivity(i);

                    if (requestCode == CAMERA_REQUEST) {
                        Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
                }
            }
            if (requestCode == VIDEO_CAPTURE) {
                if (resultCode == RESULT_OK) {
                    uri = data.getData();
                    Video video2 = newVideo(uri);
                    String[] filePathColumn = { MediaStore.Video.Media.DATA };

                    Cursor cursor = getContentResolver().query(uri,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgDecodableString = cursor.getString(columnIndex);
                    File newfile = new File(imgDecodableString);
                    if (newfile.exists()) {
                        Date lastModDate = new Date(newfile.lastModified());
                        String strCuurentDate = lastModDate.toString();
                        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
                        String date = format.format(Date.parse(strCuurentDate));
                        video.date = date;
                    }
                    databaseHelper.addOrUpdateVideo(video2);
                    cursor.close();
                    Intent i = new Intent(MainActivity.this, AddingTagsActivity.class);
                    i.putExtra("VideoUri", video2.uri);
                    i.putExtra("main", "yes");
                    startActivity(i);
                    Toast.makeText(this, "Video has been saved to:\n" + data.getData(), Toast.LENGTH_LONG).show();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Video recording cancelled.",  Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Failed to record video",  Toast.LENGTH_LONG).show();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    //getsDate and stores it in the database with video
    public String getDate(String inputUri) {
        File newfile = new File(inputUri);
        if (newfile.exists()) {
            Date lastModDate = new Date(newfile.lastModified());
            return lastModDate.toString();
        }
        else {
            return null;
        }
    }

    public Photo newPhoto(Uri uri) {
        String uriStr = uri.toString();
        Photo photo = new Photo();
        photo.uri = uriStr;
        return photo;
    }

    //creates a newvideo
    public Video newVideo(Uri uri) {
        String uriStr = uri.toString();
        video = new Video();
        video.uri = uriStr;
        video.thumbnail = VideoAdapter.getThumbnailPathForLocalFile(this, uri);
        String vidDate = getDate(uriStr);
       if (vidDate != null) {
            video.date = vidDate;
        }
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(this, Uri.parse(video.uri));
        String orientation = mediaMetadataRetriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        String location = mediaMetadataRetriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_LOCATION);

        if (location != null) {
            int index = location.lastIndexOf('-');
            if (index == -1) {
                index = location.lastIndexOf('+');
            }
            float latitude = Float.parseFloat(location.substring(0, index - 1));
            float longitude = Float.parseFloat(location.substring(index));

            LatLng latLng = new LatLng(latitude, longitude);
            List<Address> addresses = getAddress(latLng);

            if (addresses != null && addresses.get(0) != null) {
                String locAddress = addresses.get(0).getLocality();
                video.location = locAddress;
            }
        }
        if (orientation ==  null){
                video.orientation="vertical";
        }
        else {
          if (Integer.parseInt(orientation) == 0) {
                video.orientation = "horizontal";
            } else if (Integer.parseInt(orientation) == 90) {
                video.orientation = "vertical";
            }
            else {
              video.orientation = "vertical";
          }
        }
        return video;
    }

    //gets location for image
    public List<Address> getAddress(LatLng point) {
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this);
            if (point.latitude != 0 || point.longitude != 0) {
                addresses = geocoder.getFromLocation(point.latitude ,
                        point.longitude, 1);
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getAddressLine(1);
                String country = addresses.get(0).getAddressLine(2);
                System.out.println(address+" - "+city+" - "+country);

                return addresses;

            } else {
                Toast.makeText(this, "Invalid location",
                        Toast.LENGTH_LONG).show();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void toFacebook() {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.your_placeholder, new FacebookFragment()).addToBackStack(null);
        ft.commit();
    }

    public void setFloatingActionMenu(){
        //Setting the color for menu floating action
        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        materialDesignFAM.setMenuButtonColorNormal(getResources().getColor(R.color.colorAccent));
        materialDesignFAM.setMenuButtonColorPressed(getResources().getColor(R.color.colorPrimaryLight));
        //Setting for Video Button
        floatingActionCaptureVideo = (FloatingActionButton) findViewById(R.id.material_design_floating_action_capture_video);
        floatingActionCaptureVideo.setColorNormal(getResources().getColor(R.color.colorAccent));
        floatingActionCaptureVideo.setColorPressed(getResources().getColor(R.color.colorPrimaryLight));
        floatingActionCaptureVideo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takeVideo();
                materialDesignFAM.close(true);
            }});
        //Setting for Camera Button
        floatingActionCaptureImage = (FloatingActionButton) findViewById(R.id.material_design_floating_action_capture_image);
        floatingActionCaptureImage.setColorNormal(getResources().getColor(R.color.colorAccent));
        floatingActionCaptureImage.setColorPressed(getResources().getColor(R.color.colorPrimaryLight));
        floatingActionCaptureImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takePicture();
                materialDesignFAM.close(true);
            }});
        //Setting for Upload Media Button
        floatingActionUploadMedia = (FloatingActionButton) findViewById(R.id.material_design_floating_action_upload_media);
        floatingActionUploadMedia.setColorNormal(getResources().getColor(R.color.colorAccent));
        floatingActionUploadMedia.setColorPressed(getResources().getColor(R.color.colorPrimaryLight));
        floatingActionUploadMedia.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getVideo(v);
                materialDesignFAM.close(true);

            }});

        floatingActionFacebook = (FloatingActionButton) findViewById(R.id.material_design_floating_action_facebook);
        floatingActionFacebook.setColorNormal(getResources().getColor(R.color.colorAccent));
        floatingActionFacebook.setColorPressed(getResources().getColor(R.color.colorPrimaryLight));
        floatingActionFacebook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toFacebook();
                materialDesignFAM.close(true);

            }});
    }

    // Returns the Uri for a photo stored on disk given the fileName
    public Uri getPhotoFileUri(String fileName) {
        if (isExternalStorageAvailable()) {
            File mediaStorageDir = new File(
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                Log.d(APP_TAG, "failed to create directory");
            }

            return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
        }
        return null;
    }

    // Returns true if external storage for photos is available
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public void method(View view) {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_videos"));
    }


}
