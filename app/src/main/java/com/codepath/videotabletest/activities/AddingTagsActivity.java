package com.codepath.videotabletest.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.videotabletest.DatabaseHelper;
import com.codepath.videotabletest.ItemClickSupport;
import com.codepath.videotabletest.R;
import com.codepath.videotabletest.RecognitionActivity;
import com.codepath.videotabletest.VideoControllerView;
import com.codepath.videotabletest.adapters.VidTagsAdapter;
import com.codepath.videotabletest.models.VidTag;
import com.codepath.videotabletest.models.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddingTagsActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        MediaPlayer.OnPreparedListener, GestureDetector.OnGestureListener,
        com.codepath.videotabletest.VideoControllerView.MediaPlayerControl {

    SurfaceView videoSurface;
    FrameLayout layout;
    MediaPlayer player;
    RelativeLayout addLayout;
    boolean isPortrait;
    private HorizontalGridView gvSugTags;
    TextView tvNoTags;
    EditText etTagName;
    Button btnSugTags;
    List<Integer> tagTimes;
    List<VidTag> vidTags;
    com.codepath.videotabletest.VideoControllerView controller;
    Video video;
    DatabaseHelper databaseHelper;
    private GestureDetector gDetector;
    String main;
    String main2;
    private final int REQUEST_CODE = 20;
    View view;
    boolean on = false;
    int screenWidth;
    int screenHeight;
    int position;

    boolean gvVisible = false;
    android.support.v7.app.ActionBar actionBar;
    Uri uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_tags);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#69dea9'>Pinpoint</font>"));
        videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
        databaseHelper = DatabaseHelper.getInstance(this);
        //gvCurrTags = (HorizontalGridView) findViewById(R.id.gvCurrTags);
        gvSugTags = (HorizontalGridView) findViewById(R.id.gvSugTags);
        etTagName = (EditText) findViewById(R.id.etTagName);
        btnSugTags = (Button) findViewById(R.id.btnSugTags);
        layout = (FrameLayout) findViewById(R.id.videoSurfaceContainer);

        addLayout = (RelativeLayout) findViewById(R.id.addLayout);
        gvSugTags.setVisibility(View.GONE);
        isPortrait = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;


        tagTimes = new ArrayList<>();
        vidTags = new ArrayList<>();

        String uriStr = getIntent().getStringExtra("VideoUri");
        main = getIntent().getStringExtra("main");
        main2 = getIntent().getStringExtra("main2");
        uri = Uri.parse(uriStr);

        //Create new video object to add to database
        int id = databaseHelper.getVideoID(uriStr);
        video = databaseHelper.getVideo(id);
        gDetector = new GestureDetector(this);
//        if (video.orientation != null && video.orientation.equals("vertical")) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
        RunVideo(uri);
        controller.show();
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        actionBar = getSupportActionBar();
        actionBar.setTitle(Html.fromHtml("<font color='#69dea9'>Pinpoint</font>"));

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_addingtags, menu);
        return true;
    }

    public void RunVideo(Uri uri){
        SurfaceHolder videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);

        player = new MediaPlayer();
        controller = new com.codepath.videotabletest.VideoControllerView(this);
        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this, Uri.parse(String.valueOf(uri)));
            player.setOnPreparedListener(this);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        controller.show();
        return gDetector.onTouchEvent(event);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        player.setDisplay(holder);
        player.prepareAsync();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
// Implement MediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(MediaPlayer mp) {
        controller.setMediaPlayer((VideoControllerView.MediaPlayerControl) this);
        controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));


        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width;
        int height;
        if (isPortrait) {
            width = size.x;
            height = size.y;
        }
        else {
            width = size.y;
            height = size.x;
        }

        //this assumes the video was taken on the phone and hasn't been edited in size
        if(video.orientation != null) {
            if (!isPortrait && video.orientation.equals("vertical")) {

                ViewGroup.LayoutParams params = layout.getLayoutParams();
                params.height = width;
                params.width = width * width / height;
                layout.requestLayout();

            }
            else if (isPortrait && video.orientation.equals("horizontal")) {
                ViewGroup.LayoutParams params = layout.getLayoutParams();
                params.width = width;
                params.height = width * width / height;
                layout.requestLayout();
                controller.setAnchorView((FrameLayout) findViewById(R.id.frameAddingTags));
            } else if (!isPortrait && video.orientation.equals("horizontal")) {
                ViewGroup.LayoutParams params = layout.getLayoutParams();
                params.width = screenWidth;
                params.height = screenHeight;
                layout.requestLayout();

            }
                //layout.setBackgroundColor(Color.parseColor("#000000"));
//            } else if (!isPortrait && video.orientation.equals("horizontal")) {
//
//                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) addLayout.getLayoutParams();
//                int dp = 285;
//                int px = (int) AddingTagsActivity.convertDpToPixel(dp, this);
//                params.topMargin = px;
//                //params.setMargins(0, dp, 0, 0);
//                addLayout.setLayoutParams(params);
//                getSupportActionBar().hide();
//
//            }
//            } else if (!isPortrait && video.orientation.equals("horizontal")) {
//
//                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) addLayout.getLayoutParams();
//                int dp = 285;
//                int px = (int) AddingTagsActivity.convertDpToPixel(dp, this);
//                params.topMargin = px;
//                //params.setMargins(0, dp, 0, 0);
//                addLayout.setLayoutParams(params);
////                getSupportActionBar().hide();
//            }
//            } else if (isPortrait && video.orientation.equals("vertical")) {
//                FrameLayout layout = (FrameLayout) findViewById(R.id.videoSurfaceContainer);
//                ViewGroup.LayoutParams params = layout.getLayoutParams();
//                params.width = width;
//                Resources r = getResources();
//                LinearLayout layout1 = (LinearLayout) findViewById(R.id.addLayout);
//                int layoutHeight = layout1.getHeight();
//                int layoutDpHeight = (int) convertPixelsToDp(layoutHeight, getApplicationContext());
//                layoutDpHeight += 50;
//                int pixelHeight = (int) convertDpToPixel(layoutDpHeight, getApplicationContext());
//
//                //float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, r.getDisplayMetrics());
//                params.height = height - pixelHeight;
//                //controller.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//            }

        }


        /// Converts 14 dip into its equivalent px


        player.start();
        controller.show();

        //BE SURE TO CAN THE TIME IF YOU WANT IT TO SHOW THE WHOLE TIME
        //controller.show(getDuration());
        displayTags();
    }
    // End MediaPlayer.OnPreparedListener
// Implement VideoMediaController.MediaPlayerControl

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
            if (player == null) {
                RunVideo(uri);
            }
            return player.getCurrentPosition();

    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public boolean isPlaying() {
        position = getCurrentPosition();
        return player.isPlaying();
    }

    @Override
    public void pause() {
        player.pause();
        controller.updatePausePlay();
        if (on) {
            resetSuggestedTags();
        }
        if (btnSugTags.getVisibility() == View.GONE) {
            resetSuggestedTags();
        }
    }

    @Override
    public void seekTo(int i) {
        player.seekTo(i);
    }

    @Override
    public void start() {
        player.start();
        resetSuggestedTags();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }

    public void displayTags() {
        vidTags = new ArrayList<>();
        tagTimes = new ArrayList<>();
        List<VidTag> associatedTags = databaseHelper.getAssociatedVidTags(video);
        int videoDuration = getDuration();
        for (int i = 0; i < associatedTags.size(); i ++) {
            VidTag vidTag = associatedTags.get(i);
            int time = vidTag.time;
            if (time != -1) {
                tagTimes.add((time * 1000) / videoDuration);
                vidTags.add(vidTag);
            };
        }
        controller.setDots(vidTags, tagTimes);
    }

    public void onAddTagClick(View view) {
        String tagName = etTagName.getText().toString();
        etTagName.setText("");
        hideKeyboard(this);
        VidTag vidTag = new VidTag();
        vidTag.label = tagName;
        vidTag.time = getCurrentPosition();
        vidTag.video = video;
        addTag(vidTag);

    }

    public void addTag(VidTag vidTag) {
        pause();
        //controller.updatePausePlay();
        vidTag.time = getCurrentPosition();
        int toastDuration = Toast.LENGTH_SHORT;
        int videoDuration = getDuration();
        //player.start();
        int tagTime = (vidTag.time * 1000)/videoDuration;
        //controller.selectTag(vidTag);
        if (videoDuration < 10000 && player.isPlaying()) {
            try {
                Thread.sleep(750);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        if (!tagTimeMatches(tagTime)){
            Toast.makeText(getApplicationContext(), vidTag.label, toastDuration).show();
            databaseHelper.addVidTag(vidTag);
            vidTags.add(vidTag);
            tagTimes.add(tagTime);
            controller.setSelectedVidTag(vidTag);
            controller.setDots(vidTags, tagTimes);
        } else {
            Toast.makeText(this, "Tag already exists at specified time", toastDuration).show();
        }
    }


    //TODO: Debug for longer videos
    private boolean tagTimeMatches(int tagTime) {
        for (int time: tagTimes) {
            int offset = Math.abs(time - tagTime);
            if (offset < 50) {
                return true;
            }
        }
        return false;
    }


    public void getSuggestedTags(View view) {

        pause();
        btnSugTags.setVisibility(View.GONE);
        gvVisible = true;
        Intent i = new Intent(AddingTagsActivity.this, RecognitionActivity.class);
        i.putExtra("videoUri",video.uri);
        i.putExtra("time", getCurrentPosition());
        startActivityForResult(i,REQUEST_CODE);
    }

    public void resetSuggestedTags() {
        gvVisible = false;
        btnSugTags.setVisibility(View.VISIBLE);
        gvSugTags.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            String name = data.getExtras().getString("name");
            Bundle args = data.getBundleExtra("BUNDLE");
            ArrayList<String> tags = (ArrayList<String>) args.getStringArrayList("ARRAYLIST");
            ArrayList<VidTag> sugTags = new ArrayList<>();
            for(int i=0; i<tags.size();i++){
                VidTag tag = new VidTag();
                tag.label = tags.get(i).toString();
                tag.time = getCurrentPosition();
                tag.video = video;
                List<String> vidTagLabels = new ArrayList<>();
                for (VidTag vidTag: vidTags) {
                    vidTagLabels.add(vidTag.label);
                }
                if(!vidTagLabels.contains(tag.label)){
                    sugTags.add(tag);
                }
            }
            final VidTagsAdapter adapter = new VidTagsAdapter(this, sugTags);
            gvSugTags.setAdapter(adapter);
            //tvSugTags.setVisibility(View.VISIBLE);
            gvSugTags.setVisibility(View.VISIBLE);
            final ArrayList<VidTag> newSugTags = sugTags;
            ItemClickSupport.addTo(gvSugTags)
                    .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                        @Override
                        public void onItemClicked(HorizontalGridView horizontalGridView, int position, View v) {
                            VidTag vidTag = adapter.getItem(position);
                            addTag(vidTag);
                            newSugTags.remove(vidTag);
                            VidTagsAdapter newAdapter = new VidTagsAdapter(AddingTagsActivity.this, newSugTags);
                            gvSugTags.setAdapter(newAdapter);
                        }
                    });
        }
    }

    public void toEdit(MenuItem item) {
        player.stop();
        Intent i = new Intent(AddingTagsActivity.this, EditActivity.class);
        // put "extras" into the bundle for access in the second activity
        i.putExtra("uri", video.uri);
        if (main != null && main.equals("yes")) {
            i.putExtra("mainEdit", "yes");
        }
        startActivity(i);
    }



    public void delete(View view) {
        final VidTag selectedVidTag = controller.getSelectedVidTag();
        AlertDialog.Builder adb = new AlertDialog.Builder(AddingTagsActivity.this);
        adb.setTitle("Delete");
        adb.setMessage("Are you sure you want to delete this tag?");
        adb.setNegativeButton("Cancel", null);
        adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                databaseHelper.deleteVidTag(selectedVidTag);
                controller.setSelectedDot(-1);
                Toast.makeText(AddingTagsActivity.this, "Deleted \"" + selectedVidTag.label + "\"", Toast.LENGTH_SHORT).show();
                displayTags();
            }
        });
        adb.show();
    }

    public void onEditClick(View view) {
        pause();
        //controller.updatePausePlay();
    }


    @Override
    public void onBackPressed() {
        if (player != null) {
            player.stop();
        }
//        if (player != null) {
//            player.reset();
//            player.release();
//            player = null;
//        }
        finish();
        VideoPlayerActivity.count = 2;
        //TODO do we want to create a new intent?
//        Intent i = new Intent(AddingTagsActivity.this, VideoPlayerActivity.class);
//        i.putExtra("uri", video.uri);
//        startActivity(i);
//        Log.d("uri", video.uri);

        //this.finish();
    }


    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override
    public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
        if(!(start.getRawY() < screenHeight / 2.0)) {

            if (start.getRawY() < finish.getRawY()) {
                controller.hide();
                gvSugTags.setVisibility(View.GONE);
                btnSugTags.setVisibility(View.GONE);
            } else {
                controller.show();
                if (gvVisible) {
                    gvSugTags.setVisibility(View.VISIBLE);
                } else {
                    btnSugTags.setVisibility(View.VISIBLE);
                }
            }
        } else {
//            if (start.getRawY() < finish.getRawY()) {
//                getSupportActionBar().show();
//                RelativeLayout rlSugg = (RelativeLayout) findViewById(R.id.rlSugg);
//                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rlSugg.getLayoutParams();
//                int dp = 430;
//                int px = (int) AddingTagsActivity.convertDpToPixel(dp, this);
//                params.topMargin = px;
//                //params.setMargins(0, dp, 0, 0);
//                rlSugg.setLayoutParams(params);
//            } else {
//                getSupportActionBar().hide();
//                RelativeLayout rlSugg = (RelativeLayout) findViewById(R.id.rlSugg);
//                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rlSugg.getLayoutParams();
//                int dp = 450;
//                int px = (int) AddingTagsActivity.convertDpToPixel(dp, this);
//                params.topMargin = px;
//                //params.setMargins(0, dp, 0, 0);
//                rlSugg.setLayoutParams(params);
//
//
//                ViewGroup.MarginLayoutParams params2 = (ViewGroup.MarginLayoutParams) addLayout.getLayoutParams();
//                int dp1 = 600;
//                int px1 = (int) AddingTagsActivity.convertDpToPixel(dp1, this);
//                params.topMargin = px1;
//                //params.setMargins(0, dp, 0, 0);
//                addLayout.setLayoutParams(params2);
//            }
        }

        if(start.getRawY() < finish.getRawY()) {
            hideKeyboard(AddingTagsActivity.this);
        }

        float diffY = start.getRawY() - finish.getRawY();
        float diffX = start.getRawX() - finish.getRawX();
        if ((Math.abs(diffX) - Math.abs(diffY)) > SWIPE_THRESHOLD) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(xVelocity) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    player.stop();
                    Intent i = new Intent(AddingTagsActivity.this, EditActivity.class);
                    // put "extras" into the bundle for access in the second activity
                    i.putExtra("uri", video.uri);
                    if (main != null && main.equals("yes")) {
                        i.putExtra("mainEdit", "yes");
                    }
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
    public void onLongPress(MotionEvent arg0) {
    }
    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        hideKeyboard(AddingTagsActivity.this);
        return false;
    }
    @Override
    public void onShowPress(MotionEvent arg0) {
    }
    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        return false;
    }



    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//        //int orientation = this.getResources().getConfiguration().orientation;
//
//        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            seekTo(position);
//
//        }
//        }



    @Override
    protected void onSaveInstanceState (Bundle outState){
        outState.putInt("pos", getCurrentPosition()); // save it here
    }

    private void addOrRemoveProperty(View view, int property, boolean flag){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if(flag){
            layoutParams.addRule(property);
        }else {
            layoutParams.removeRule(property);
        }
        view.setLayoutParams(layoutParams);
    }

    @Override
    protected void onStop() {
        super.onStop();
            player.release();
            player = null;

    }

    @Override
    protected void onStart() {
        super.onStart();
        RunVideo(uri);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        RunVideo(uri);
//    }
}