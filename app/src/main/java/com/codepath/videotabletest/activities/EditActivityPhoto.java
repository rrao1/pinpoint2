package com.codepath.videotabletest.activities;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
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
import com.codepath.videotabletest.adapters.MediaAdapter;
import com.codepath.videotabletest.adapters.PhoTagsAdapter;
import com.codepath.videotabletest.fragments.DatePickerFragment;
import com.codepath.videotabletest.models.PhoTag;
import com.codepath.videotabletest.models.Photo;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class EditActivityPhoto extends AppCompatActivity implements GestureDetector.OnGestureListener, DatePickerDialog.OnDateSetListener {

    DatabaseHelper databaseHelper;
    Photo photo;
    String uriStr;
    EditText etName;
    EditText etLocation;
    EditText etDate;
    EditText etCategories;
    String orientationStr;
    Button autotag;
    TextView tvSugCat;
    TextView tvUsedCat;
    TextView tvNoCat;
    private GestureDetector gDetector;
    String main2;
    private HorizontalGridView horizontalGridView;
    ClarifaiClient clarifai;
    private final int REQUEST_CODE = 20;
    List<String> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_photo_activity);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#69dea9'>Pinpoint</font>"));
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        uriStr = getIntent().getStringExtra("Image");
        main2 = getIntent().getStringExtra("mainEdit");
        databaseHelper = DatabaseHelper.getInstance(this);
        int id = databaseHelper.getPhotoID(uriStr);
        photo = databaseHelper.getPhoto(id);
        etName = (EditText) findViewById(R.id.etName);
        etDate = (EditText) findViewById(R.id.etDate);
        etLocation = (EditText) findViewById(R.id.etLocation);
        etCategories = (EditText) findViewById(R.id.etCategories);
        tvUsedCat = (TextView) findViewById(R.id.tvUsedCats);
        tvNoCat = (TextView) findViewById(R.id.tvNoCats);
        gDetector = new GestureDetector(this);
        ImageView ivThumbnail = (ImageView) findViewById(R.id.ivThumbnail);
        //Bitmap b = ImagePlayerActivity.rotateBitmapOrientation(uriStr);

        if (photo.uri.contains("photo")) {
            rotateAndLoadPicasso(photo.uri, ivThumbnail);
        } else {
            try {
                ivThumbnail.setImageBitmap(rotateBitmapOrientation(uriStr));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (photo.name != null) {
            etName.append(photo.name);
        }
        //set date if not null
        if (photo.date != null) {
            etDate.setText(photo.date);
        }

        if (photo.location != null) {
            etLocation.setText(photo.location);
        }

        if (photo.orientation != null) {
            orientationStr = photo.orientation;
        }

        List<PhoTag> phoTags = databaseHelper.getAssociatedPhoTags(photo);

        for (PhoTag phoTag: phoTags){
            categories.add(phoTag.label);
        }


        String categoriesStr = "";
        if (!categories.isEmpty()) {
            categoriesStr += categories.get(0);
        }
        for (int i = 1; i < categories.size(); i++) {
            categoriesStr += ", " + categories.get(i) ;
        }
        etCategories.append(categoriesStr);
        autotag = (Button) findViewById(R.id.btnAutoTag);
        tvSugCat =(TextView) findViewById(R.id.tvSugTitle);
        tvSugCat.setVisibility(View.GONE);

        populateUsedList();

    }


    public void populateUsedList() {
        horizontalGridView = (HorizontalGridView) findViewById(R.id.gridView);
        ArrayList<PhoTag> items = new ArrayList<>();
        List<PhoTag> allPhoTags = databaseHelper.getAllPhoTags();
        for (PhoTag phoTag : allPhoTags) {
            if (!categories.contains(phoTag.label)) {
                boolean repeat = false;
                for(int i =0; i<items.size();i++){
                    if(phoTag.label.contentEquals(items.get(i).label)){
                        repeat=true;
                    }
                }
                if(!repeat){
                    items.add(0, phoTag);
                }
            }
        }
        if (items.isEmpty()) {
            tvNoCat.setVisibility(View.VISIBLE);
            tvUsedCat.setVisibility(View.GONE);
        } else {
            tvNoCat.setVisibility(View.GONE);
            tvUsedCat.setVisibility(View.VISIBLE);

            final PhoTagsAdapter adapter = new PhoTagsAdapter(this, items);
            final ArrayList newItems = items;
            horizontalGridView.setAdapter(adapter);
            ItemClickSupport.addTo(horizontalGridView)
                    .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                        @Override
                        public void onItemClicked(HorizontalGridView horizontalGridView, int position, View v) {
                            PhoTag phoTag = adapter.getItem(position);
                            String categoriesStr = etCategories.getText().toString();
                            if(!categoriesStr.equals("")) {
                                String substr = categoriesStr.substring(categoriesStr.length() - 2);
                                if (!substr.contains(',' + "")) {
                                    etCategories.append(", " + phoTag.label);
                                } else {
                                    etCategories.append(phoTag.label);
                                }
                            } else {
                                etCategories.append(phoTag.label);
                            }
                            categories.add(phoTag.label);
                            newItems.remove(phoTag);
                            final PhoTagsAdapter newAdapter = new PhoTagsAdapter(EditActivityPhoto.this, newItems);
                            horizontalGridView.setAdapter(newAdapter);
                            if(newItems.isEmpty()) {
                                tvNoCat.setVisibility(View.VISIBLE);
                                tvUsedCat.setVisibility(View.GONE);
                            }
                        }
                    });
        }

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
        photo.name = etName.getText().toString();
        photo.date=etDate.getText().toString();
//        String strCuurentDate = etDate.getText().toString();
//        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
//        String date = format.format(Date.parse(strCuurentDate));
//        photo.date = date;

        photo.location = etLocation.getText().toString();
        photo.orientation = orientationStr;

        List<PhoTag> phoTags = databaseHelper.getAssociatedPhoTags(photo);
        for (PhoTag phoTag: phoTags){
            categories.add(phoTag.label);
            databaseHelper.deletePhoTag(phoTag);
        }

        String categoriesStr = etCategories.getText().toString();
        List<String> categoriesList = new ArrayList<>();
        if (categoriesStr.contains(",")) {
            List<String> items = Arrays.asList(categoriesStr.split("\\s*,\\s*"));
            for (String item1 : items) {
                if (!item1.equals("") && !categoriesList.contains(item1)) {
                    categoriesList.add(item1);
            //        databaseHelper.addVidTag(new VidTag(video, item1, -1));
                    databaseHelper.addPhoTag(new PhoTag(photo, item1));
                }
            }
        }
        else if (!categoriesStr.equals("")) {
          //  databaseHelper.addVidTag(new VidTag(video, categoriesStr, -1));
            databaseHelper.addPhoTag(new PhoTag(photo, categoriesStr));

        }
        //databaseHelper.addOrUpdateVideo(video);
        //databaseHelper.addPhoTag(phoTags);
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override
    public boolean onFling(MotionEvent start, MotionEvent finish, float velocityX, float velocityY) {
        if(start.getRawY() < finish.getRawY()) {
            hideKeyboard(EditActivityPhoto.this);
        }
        float diffY = start.getRawY() - finish.getRawY();
        float diffX = start.getRawX() - finish.getRawX();
        if ((Math.abs(diffX) - Math.abs(diffY)) > SWIPE_THRESHOLD) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    Intent i = new Intent(EditActivityPhoto.this, MainActivity.class);
                    startActivity(i);
                } else {
                    if (main2 == null) {
                        this.finish();
                    }
                    else {
                        Intent i = new Intent(EditActivityPhoto.this, ImagePlayerActivity.class);
                        i.putExtra("imageUri", photo.uri);
                        i.putExtra("main2", "yes");
                        startActivity(i);
                    }
                }
            }
        }
        return true;
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

    public void autoTag(View view) {
        autotag.setVisibility(view.GONE);
        Intent i = new Intent(EditActivityPhoto.this, RecognitionActivity.class);
        i.putExtra("photoUri",uriStr);
        startActivityForResult(i,REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            tvSugCat.setVisibility(View.VISIBLE);
            String name = data.getExtras().getString("name");
            Bundle args = data.getBundleExtra("BUNDLE");
            ArrayList<String> tags = (ArrayList<String>) args.getStringArrayList("ARRAYLIST");
            ArrayList<PhoTag> sugTags = new ArrayList<>();
            for(int i=0; i< tags.size();i++){
                PhoTag tag = new PhoTag();
                tag.label = tags.get(i).toString();
                tag.photo = photo;
                sugTags.add(tag);
            }
            horizontalGridView = (HorizontalGridView) findViewById(R.id.gvSuggested);
            horizontalGridView.setVisibility(horizontalGridView.VISIBLE);
            final PhoTagsAdapter adapter = new PhoTagsAdapter(this, sugTags);
            final ArrayList newSugTags = sugTags;
            horizontalGridView.setAdapter(adapter);
            ItemClickSupport.addTo(horizontalGridView)
                    .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                        @Override
                        public void onItemClicked(HorizontalGridView horizontalGridView, int position, View v) {
                            PhoTag phoTag = adapter.getItem(position);
                            String categoriesStr = etCategories.getText().toString();
                            if(!categoriesStr.equals("")) {
                                String substr = categoriesStr.substring(categoriesStr.length() - 2);
                                if (!substr.contains(',' + "")) {
                                    etCategories.append(", " + phoTag.label);
                                } else {
                                    etCategories.append(phoTag.label);
                                }
                            } else {
                                etCategories.append(phoTag.label);
                            }
                            categories.add(phoTag.label);
                            newSugTags.remove(phoTag);
                            final PhoTagsAdapter newAdapter = new PhoTagsAdapter(EditActivityPhoto.this, newSugTags);
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
        photo.date = formatted;
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) throws Exception {
        File newfile = new File(photoFilePath);
        Bitmap bm1 = MediaAdapter.getThumbnail(this.getContentResolver(), photoFilePath);
        if (newfile.exists()) {


            // Create and configure BitmapFactory
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            //BitmapFactory.decodeFile(photoFilePath, bounds);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            Bitmap bm = BitmapFactory.decodeFile(newfile.getAbsolutePath(), opts);

            // Read EXIF Data
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(newfile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
            // Rotate Bitmap
            Matrix matrix = new Matrix();
            matrix.setRotate(rotationAngle, (float) bm1.getWidth() / 2, (float) bm1.getHeight() / 2);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bm1, 0, 0, bm1.getWidth(), bm1.getHeight(), matrix, true);
            // Return result
            return rotatedBitmap;
        }
        //TODO MAYBE NOT RETURN NULL
        return null;
    }

    public void rotateAndLoadPicasso(String photoFilePath, ImageView ivImage) {

        File newfile = new File(photoFilePath);
        if (newfile.exists()) {
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(newfile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
            File f = new File(photoFilePath);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                Picasso.with(this).load(f).into(ivImage);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                Picasso.with(this).load(f).rotate(180f).into(ivImage);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                Picasso.with(this).load(f).rotate(270f).into(ivImage);
            } else {
                Picasso.with(this).load(f).into(ivImage);
            }
        }
    }
}
