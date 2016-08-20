package com.codepath.videotabletest.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.codepath.videotabletest.DatabaseHelper;
import com.codepath.videotabletest.R;
import com.codepath.videotabletest.fragments.InfoPhotoDialogFragment;
import com.codepath.videotabletest.models.Photo;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImagePlayerActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {


    Photo photo;
    Uri uri;
    DatabaseHelper databaseHelper;
    String uriStr;
    private GestureDetector gDetector;
    int screenWidth;
    int screenHeight;
    ImageView ivTest;
    android.support.v7.app.ActionBar actionBar;
    Boolean isFragment = false;

    public void setIsFragment(Boolean bool) {
        isFragment = bool;
    }

    public boolean checkIfFragment() {
        return isFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_player);
        databaseHelper = DatabaseHelper.getInstance(this);
        uriStr = getIntent().getStringExtra("imageUri");
         ivTest = (ImageView) findViewById(R.id.ivTest);
        if (uriStr.contains("photo")) {
            rotateAndLoadPicasso(uriStr, ivTest);
        } else {
            ivTest.setImageBitmap(rotateBitmapOrientation(uriStr));
        }
        uri = Uri.parse(uriStr);
        int photoId = databaseHelper.getVideoID(uriStr);
        photo = databaseHelper.getPhoto(photoId);
        actionBar = getSupportActionBar();
        actionBar.setTitle(Html.fromHtml("<font color='#69dea9'>Pinpoint</font>"));
        actionBar.hide();
        gDetector = new GestureDetector(this);
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_customizephoto, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter) {
            Intent i = new Intent(ImagePlayerActivity.this, EditActivityPhoto.class);
            i.putExtra("Image", uriStr);
            i.putExtra("main", "no");
            startActivity(i);
        }
        else if (id == R.id.action_share) {
            setShareIntent();
            return true;
        }
        else if(id==R.id.action_info){
            FragmentManager fm = getSupportFragmentManager();
            InfoPhotoDialogFragment editNameDialogFragment = InfoPhotoDialogFragment.newInstance(uriStr);
            isFragment = true;
            editNameDialogFragment.show(fm, "fragment_infophoto");

        }

        return super.onOptionsItemSelected(item);
    }
    private void setShareIntent() {;

//        Uri bmpUri = getLocalBitmapUri(ivTest);
//        Intent shareIntent = new Intent();
//        shareIntent.setAction(Intent.ACTION_SEND);
//        shareIntent.setType("*/*");
//        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
//        // Launch share menu
//        startActivity(Intent.createChooser(shareIntent, "Share Image"));
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpg");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        final File photoFile = new File(getFilesDir(), uriStr);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photoFile));
        startActivity(Intent.createChooser(shareIntent, "Share image using"));

    }

    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }




    public static Bitmap rotateBitmapOrientation(String photoFilePath) {

        File newfile = new File(photoFilePath);
        if (newfile.exists()) {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(newfile.getAbsolutePath(), bounds);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            Bitmap bm = BitmapFactory.decodeFile(newfile.getAbsolutePath(), opts);
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
            matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
            // Return result
            return rotatedBitmap;
        }
        return null;

    }

    @Override
    public boolean onDown(MotionEvent e) {
        actionBar.show();
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
    public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {

        if(start.getRawY() < screenHeight / 2.0) {
            if (start.getRawY() < finish.getRawY()) {
                getSupportActionBar().show();
            } else {
                getSupportActionBar().hide();
            }
        }
//        else {
//            if (start.getRawY() < finish.getRawY()) {
//                controller.hide();
//            } else {
//                controller.show();
//            }
//        }

        float diffY = start.getRawY() - finish.getRawY();
        float diffX = start.getRawX() - finish.getRawX();
        if ((Math.abs(diffX) - Math.abs(diffY)) > SWIPE_THRESHOLD) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(xVelocity) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    Intent i = new Intent(ImagePlayerActivity.this, EditActivityPhoto.class);
                    i.putExtra("Image", uriStr);
                    i.putExtra("main", "no");
                    startActivity(i);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                } else {
                    onBackPressed();
                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                }
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gDetector.onTouchEvent(event);
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
