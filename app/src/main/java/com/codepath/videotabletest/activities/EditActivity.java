package com.codepath.videotabletest.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.clarifai.api.ClarifaiClient;
import com.codepath.videotabletest.DatabaseHelper;
import com.codepath.videotabletest.ItemClickSupport;
import com.codepath.videotabletest.R;
import com.codepath.videotabletest.RecognitionActivity;
import com.codepath.videotabletest.adapters.PhoTagsAdapter;
import com.codepath.videotabletest.adapters.VidTagsAdapter;
import com.codepath.videotabletest.adapters.VideoAdapter;
import com.codepath.videotabletest.fragments.DatePickerFragment;
import com.codepath.videotabletest.models.VidTag;
import com.codepath.videotabletest.models.Video;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;





public class EditActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, DatePickerDialog.OnDateSetListener {

    DatabaseHelper databaseHelper;
    static Video video;
    String uriStr;
    EditText etName;
    EditText etLocation;
    TextView etDate;
    EditText etCategories;
    TextView tvNoCategories;
    String orientationStr;
    Button autotag;
    TextView SugCat;
    TextView tvUsedCats;
    ImageView ivVideoFrame;
    VideoAdapter videoAdapter;
    private GestureDetector gDetector;
    String main2;
    private HorizontalGridView horizontalGridView;
    ClarifaiClient clarifai;
    private final int REQUEST_CODE = 20;
    List<String> categories = new ArrayList<>();
    String datepicked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#69dea9'>Pinpoint</font>"));
        uriStr = getIntent().getStringExtra("uri");
        main2 = getIntent().getStringExtra("mainEdit");
        databaseHelper = DatabaseHelper.getInstance(this);
        int id = databaseHelper.getVideoID(uriStr);
        video = databaseHelper.getVideo(id);
        etName = (EditText) findViewById(R.id.etName);
        etDate = (TextView) findViewById(R.id.etDate);
        etLocation = (EditText) findViewById(R.id.etLocation);
        etCategories = (EditText) findViewById(R.id.etCategories);
        tvNoCategories = (TextView) findViewById(R.id.tvNoCats);
        tvUsedCats = (TextView) findViewById(R.id.tvUsedCats);
        gDetector = new GestureDetector(this);
        ImageView ivThumbnail = (ImageView) findViewById(R.id.ivThumbnail);
        if (video.uri.contains("content")) {
            File imgFile = new File(video.thumbnail);
            if(imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ivThumbnail.setImageBitmap(myBitmap);
            }
        }
        else {
            String picture = video.thumbnail;
            String pic = picture.replace("https", "http");
            Picasso.with(this)
                    .load(pic)
                    .into(ivThumbnail);
        }
        if (video.name != null) {
            etName.append(video.name);
        }
        //set date if not null
        if (video.date != null) {
            etDate.setText(video.date);
        }

        List<VidTag> vidTags = databaseHelper.getAssociatedVidTags(video);
        for (VidTag vidTag: vidTags){
            if (vidTag.time == -1) {
                categories.add(vidTag.label);
            }
        }

        populateUsedList();

        String categoriesStr = "";
        if (!categories.isEmpty()) {
            categoriesStr += categories.get(0);
        }
        for (int i = 1; i < categories.size(); i++) {
            categoriesStr += ", " + categories.get(i) ;
        }
        etCategories.append(categoriesStr);
        etLocation.setText(video.location);
        orientationStr = video.orientation;
        autotag=(Button) findViewById(R.id.btnAutoTag);
        SugCat =(TextView) findViewById(R.id.tvSugTitle);
        SugCat.setVisibility(View.GONE);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gDetector.onTouchEvent(event);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editactivity, menu);
        return true;
    }

    public void onMenuSubmit(MenuItem item) {
        video.name = etName.getText().toString();
//        String strCuurentDate = etDate.getText().toString();
//        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
//        String date = format.format(Date.parse(strCuurentDate));
        video.date = etDate.getText().toString();
        video.location = etLocation.getText().toString();
        video.orientation = orientationStr;

        List<VidTag> vidTags = databaseHelper.getAssociatedVidTags(video);
        for (VidTag vidTag: vidTags) {
            if (vidTag.time == -1) {
                databaseHelper.deleteVidTag(vidTag);
            }
        }

        String categoriesStr = etCategories.getText().toString();
        List<String> categoriesList = new ArrayList<>();
        if (categoriesStr.contains(",")) {
            List<String> items = Arrays.asList(categoriesStr.split("\\s*,\\s*"));
            for (String item1 : items) {
                if (!item1.equals("") && !categoriesList.contains(item1)) {
                    categoriesList.add(item1);
                    databaseHelper.addVidTag(new VidTag(video, item1, -1));
                }
            }
        }
        else if (!categoriesStr.equals("")) {
            databaseHelper.addVidTag(new VidTag(video, categoriesStr, -1));
        }
        databaseHelper.addOrUpdateVideo(video);
        startActivity(new Intent(this, MainActivity.class));
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        return false;
    }
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override
    public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
        if(start.getRawY() < finish.getRawY()) {
            hideKeyboard(EditActivity.this);
        }
        float diffY = start.getRawY() - finish.getRawY();
        float diffX = start.getRawX() - finish.getRawX();
        if ((Math.abs(diffX) - Math.abs(diffY)) > SWIPE_THRESHOLD) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(xVelocity) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    startActivity(new Intent(EditActivity.this, MainActivity.class));
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                } else {
                    if (main2 == null) {
                        this.finish();
                    }
                    else {
                        Intent i = new Intent(EditActivity.this, AddingTagsActivity.class);
                        i.putExtra("VideoUri", video.uri);
                        i.putExtra("main2", "yes");
                        startActivity(i);
                        overridePendingTransition(R.anim.left_in, R.anim.right_out);
                    }
                }
            }
        }
        return true;
    }

//    @Override
//    public void onBackPressed() {
//        if (main2 == null) {
//            this.finish();
//        }
//        else {
//            Intent i = new Intent(EditActivity.this, AddingTagsActivity.class);
//            i.putExtra("VideoUri", video.uri);
//            i.putExtra("main2", "yes");
//            startActivity(i);
//        }
//    }

    @Override
    public void onLongPress(MotionEvent arg0) {
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        hideKeyboard(EditActivity.this);
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        return false;
    }

    public void populateUsedList(){
        horizontalGridView = (HorizontalGridView) findViewById(R.id.gridView);
        ArrayList<VidTag> items = new ArrayList<VidTag>();
        List<VidTag> allVidTags = databaseHelper.getAllVidTags();
        for (VidTag vidTag: allVidTags) {
            if (vidTag.time == -1 && !categories.contains(vidTag.label)) {
                boolean repeat = false;
                for(int i =0; i<items.size();i++){
                    if(vidTag.label.contentEquals(items.get(i).label)){
                        repeat=true;
                    }
                }
                if(!repeat){
                    items.add(0, vidTag);
                }
            }
        }
        if(items.isEmpty()) {
            tvNoCategories.setVisibility(View.VISIBLE);
            tvUsedCats.setVisibility(View.GONE);

        } else {
            tvNoCategories.setVisibility(View.GONE);
            tvUsedCats.setVisibility(View.VISIBLE);
            final VidTagsAdapter adapter = new VidTagsAdapter(this, items);
            final ArrayList newItems = items;
            horizontalGridView.setAdapter(adapter);
            ItemClickSupport.addTo(horizontalGridView)
                    .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                        @Override
                        public void onItemClicked(HorizontalGridView horizontalGridView, int position, View v) {
                            VidTag vidTag = adapter.getItem(position);
                            String categoriesStr = etCategories.getText().toString();
                            if(!categoriesStr.equals("")) {
                                String substr = categoriesStr.substring(categoriesStr.length() - 2);
                                if (!substr.contains(',' + "")) {
                                    etCategories.append(", " + vidTag.label);
                                } else {
                                    etCategories.append(vidTag.label);
                                }
                            } else {
                                etCategories.append(vidTag.label);
                            }
                            categories.add(vidTag.label);
                            newItems.remove(vidTag);
                            final PhoTagsAdapter newAdapter = new PhoTagsAdapter(EditActivity.this, newItems);
                            horizontalGridView.setAdapter(newAdapter);
                            if(newItems.isEmpty()) {
                                tvNoCategories.setVisibility(View.VISIBLE);
                                tvUsedCats.setVisibility(View.GONE);
                            }
                            populateUsedList();
                        }
                    });
        }
    }

    public void autoTag(View view) {
        autotag.setVisibility(view.GONE);
        Intent i = new Intent(EditActivity.this, RecognitionActivity.class);
//        i.putExtra("thumbnailUri",video.thumbnail);
        i.putExtra("videoUri", video.uri);
        startActivityForResult(i,REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            SugCat.setVisibility(View.VISIBLE);
            String name = data.getExtras().getString("name");
            Bundle args = data.getBundleExtra("BUNDLE");
            ArrayList<String> tags = (ArrayList<String>) args.getStringArrayList("ARRAYLIST");
            ArrayList<VidTag> sugTags = new ArrayList<>();
            for(int i=0; i<tags.size();i++){
                VidTag tag = new VidTag();
                tag.label = tags.get(i).toString();
                tag.time =0;
                tag.video=databaseHelper.getVideo(0);
                if (!categories.contains(tag.label)){
                    sugTags.add(tag);
                }
            }
            horizontalGridView = (HorizontalGridView) findViewById(R.id.gvSuggested);
            horizontalGridView.setVisibility(horizontalGridView.VISIBLE);
            final VidTagsAdapter adapter = new VidTagsAdapter(this, sugTags);
            final ArrayList newSugTags = sugTags;
            horizontalGridView.setAdapter(adapter);
            ItemClickSupport.addTo(horizontalGridView)
                    .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                        @Override
                        public void onItemClicked(HorizontalGridView horizontalGridView, int position, View v) {
                            VidTag vidTag = adapter.getItem(position);
                            String categoriesStr = etCategories.getText().toString();
                            if(!categoriesStr.equals("")) {
                                String substr = categoriesStr.substring(categoriesStr.length() - 2);
                                if (!substr.contains(',' + "")) {
                                    etCategories.append(", " + vidTag.label);
                                } else {
                                    etCategories.append(vidTag.label);
                                }
                            } else {
                                etCategories.append(vidTag.label);
                            }
                            categories.add(vidTag.label);
                            newSugTags.remove(vidTag);
                            final VidTagsAdapter newAdapter = new VidTagsAdapter(EditActivity.this, newSugTags);
                            horizontalGridView.setAdapter(newAdapter);

                        }
                    });
        }
    }

    public void Datepicker(View view) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        SimpleDateFormat format1 = new SimpleDateFormat("MMM dd, yyyy");
        String formatted = format1.format(c.getTime());
        etDate.setText(formatted);
        video.date = formatted;
    }
}
