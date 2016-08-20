
package com.codepath.videotabletest.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
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
import android.widget.FrameLayout;
import android.widget.TextView;

import com.codepath.videotabletest.DatabaseHelper;
import com.codepath.videotabletest.ItemClickSupport;
import com.codepath.videotabletest.R;
import com.codepath.videotabletest.VideoControllerView;
import com.codepath.videotabletest.adapters.VidTagsAdapter;
import com.codepath.videotabletest.fragments.InfoVideoDialogFragment;
import com.codepath.videotabletest.models.VidTag;
import com.codepath.videotabletest.models.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class VideoPlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        MediaPlayer.OnPreparedListener,
        com.codepath.videotabletest.VideoControllerView.MediaPlayerControl, GestureDetector.OnGestureListener {

    SurfaceView videoSurface;
    MediaPlayer player;
    com.codepath.videotabletest.VideoControllerView controller;
    Uri uri;
    Video video;
    int tagTime = -2;
    // Get singleton instance of database
    DatabaseHelper databaseHelper;
    private GestureDetector gDetector;
    boolean isPortrait;
    int screenWidth;
    int screenHeight;
    boolean resumed;
    android.support.v7.app.ActionBar actionBar;
    private HorizontalGridView gvCurrTags;
    List<Integer> tagTimes;
    List<VidTag> vidTags;
    TextView tvNoTags;
    Boolean isFragment = false;
    public static int count = 1;


    public void setIsFragment(Boolean bool) {
        isFragment = bool;
    }


    public boolean checkIfFragment() {
        return isFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        databaseHelper = DatabaseHelper.getInstance(this);
        count = 1;

        gvCurrTags = (HorizontalGridView) findViewById(R.id.gvCurrTags);
        tagTimes = new ArrayList<>();
        vidTags = new ArrayList<>();
        tvNoTags = (TextView) findViewById(R.id.tvNoTags);


        String uriStr = getIntent().getStringExtra("uri");
        String searchTag = getIntent().getStringExtra("searchTag");
        uri = Uri.parse(uriStr);
        videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
        int videoId = databaseHelper.getVideoID(uriStr);
        video = databaseHelper.getVideo(videoId);
        resumed = true;
        actionBar = getSupportActionBar();
        actionBar.setTitle(Html.fromHtml("<font color='#69dea9'>Pinpoint</font>"));
        actionBar.hide();
        List<VidTag> associatedTags = databaseHelper.getAssociatedVidTags(video);
        for (VidTag vidTag : associatedTags) {
            if (vidTag.label.equals(searchTag) && vidTag.time != -1) {
                tagTime = vidTag.time;
                break;
            }
        }
        isPortrait = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        gDetector = new GestureDetector(this);

        RunVideo(uri);
        TextView tvCategories = (TextView) findViewById(R.id.tvCategories);
        String categories = "";
        List<VidTag> vidTagList = databaseHelper.getAssociatedVidTags(video);
        int size = vidTagList.size();
        for (int i = 0; i < size; i++) {
            VidTag tag = vidTagList.get(i);
            if (tag.time == -1 ) {
                if (i < size - 1) {
                    categories += tag.label + ", ";
                }
                else {
                    categories += tag.label;
                }
            }
        }
        populateCurrentList();
        //tvCategories.setText(categories);

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

    }

    public void RunVideo(Uri uri){
        SurfaceHolder videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);
        videoHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        player = new MediaPlayer();
        controller = new com.codepath.videotabletest.VideoControllerView(this);

        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            if (!uri.toString().contains("content")) {
//                //String uri1 = uri.toString().replace("https", "http");
//                player.setDataSource(this, Uri.parse(String.valueOf(uri1)));
////            }
//            else {
                player.setDataSource(this, Uri.parse(String.valueOf(uri)));
            //}
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

    boolean taped = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //controller.show();
        return gDetector.onTouchEvent(event);
    }

    // Implement SurfaceHolder.Callback
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

    // End SurfaceHolder.Callback
// Implement MediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(MediaPlayer mp) {
        controller.setMediaPlayer((VideoControllerView.MediaPlayerControl) this);
        controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
//        FrameLayout hi = (FrameLayout) findViewById(R.id.videoSurfaceContainer);
//        hi.setLayerType(View.LAYER_TYPE_NONE, null);
        int time = 0;
        int duration = getDuration();
        if (tagTime != -2) {
            time = tagTime;
            player.seekTo(time);
        }
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
                FrameLayout layout = (FrameLayout) findViewById(R.id.videoSurfaceContainer);
                ViewGroup.LayoutParams params = layout.getLayoutParams();
                params.height = width;
                params.width = width * width / height;
                layout.requestLayout();

            } else if (isPortrait && video.orientation.equals("horizontal")) {
                FrameLayout layout = (FrameLayout) findViewById(R.id.videoSurfaceContainer);
                ViewGroup.LayoutParams params = layout.getLayoutParams();
                params.width = width;
                params.height = width * width / height;

                controller.setAnchorView((FrameLayout) findViewById(R.id.frameLandPort));
//
//                RelativeLayout rlCurrTags = (RelativeLayout) findViewById(R.id.rlCurrTags);
//                ViewGroup.MarginLayoutParams params1 = (ViewGroup.MarginLayoutParams) rlCurrTags.getLayoutParams();
//                int dp = 400;
//                int px = (int) AddingTagsActivity.convertDpToPixel(dp, this);
//                params1.topMargin = px;
//                //params.setMargins(0, dp, 0, 0);
//                rlCurrTags.setLayoutParams(params1);

            }
        }

        player.start();
        displayTags();
        controller.show(getDuration());
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
        if (player != null) {
            return player.getCurrentPosition();
        } else {
            RunVideo(uri);
            return player.getCurrentPosition();
        }

    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void seekTo(int i) {
        player.seekTo(i);
    }

    @Override
    public void start() {
        player.start();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }

    public void displayTags() {
        List<VidTag> vidTags = new ArrayList<>();
        List<Integer> tagTimes = new ArrayList<>();
        List<VidTag> associatedTags = databaseHelper.getAssociatedVidTags(video);
        int duration = getDuration();
        for (int i = 0; i < associatedTags.size(); i ++) {
            VidTag vidTag = associatedTags.get(i);
            int time = vidTag.time;
            if (time != -1) {
                tagTimes.add((time * 1000) / duration);
                vidTags.add(vidTag);
            };
        }
        if (vidTags.size() > 0) {
            controller.setDots(vidTags, tagTimes);
        }
    }

    public void toAdd(View view) {
        Intent i = new Intent(VideoPlayerActivity.this, AddingTagsActivity.class);
        i.putExtra("VideoUri", video.uri);
        i.putExtra("main", "no");
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_customize, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_filter) {
            Intent i = new Intent(VideoPlayerActivity.this, AddingTagsActivity.class);
            i.putExtra("VideoUri", video.uri);
            i.putExtra("main", "no");
            startActivity(i);
        }
        else if (id == R.id.action_share) {
            setShareIntent();
            return true;
        }
        else if(id == R.id.action_info){
            FragmentManager fm = getSupportFragmentManager();
            InfoVideoDialogFragment infovideoDialogFragment = InfoVideoDialogFragment.newInstance(uri.toString());
            Log.d("uriStr", uri.toString());
            isFragment = true;
            infovideoDialogFragment.show(fm, "fragment_infophoto");
        }

        return super.onOptionsItemSelected(item);
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
        //TODO: Don't show controller and action bar when swiping right to next activity?
//        if (isPortrait && (video.orientation == null || (video.orientation != null && video.orientation.equals("vertical")))) {
//            if (isPortrait)
//            if (start.getRawY() < screenHeight / 2.0) {
//                if (start.getRawY() < finish.getRawY()) {
//                    getSupportActionBar().show();
////                    RelativeLayout rlCurrTags = (RelativeLayout) findViewById(R.id.rlCurrTags);
////                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rlCurrTags.getLayoutParams();
////                    int dp = 430;
////                    int px = (int) AddingTagsActivity.convertDpToPixel(dp, this);
////                    params.topMargin = px;
////                    //params.setMargins(0, dp, 0, 0);
////                    rlCurrTags.setLayoutParams(params);
//                } else {
//                    getSupportActionBar().hide();
////                    RelativeLayout rlCurrTags = (RelativeLayout) findViewById(R.id.rlCurrTags);
////                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rlCurrTags.getLayoutParams();
////                    int dp = 470;
////                    int px = (int) AddingTagsActivity.convertDpToPixel(dp, this);
////                    params.topMargin = px;
////                    //params.setMargins(0, dp, 0, 0);
////                    rlCurrTags.setLayoutParams(params);
//                }
//            } else {
//                if (start.getRawY() < finish.getRawY()) {
//                    controller.hide();
//                    gvCurrTags.setVisibility(View.GONE);
//                    if(tvNoTags.getVisibility() == View.VISIBLE) {
//                        tvNoTags.setVisibility(View.INVISIBLE);
//                    }
//                } else {
//                    controller.show();
//                    gvCurrTags.setVisibility(View.VISIBLE);
//                    if(tvNoTags.getVisibility() == View.INVISIBLE) {
//                        tvNoTags.setVisibility(View.VISIBLE);
//                    }
//                }
//            }
//        } else if (!isPortrait) {
            if (start.getRawY() < screenHeight / 3.0) {
                if (start.getRawY() < finish.getRawY()) {
                    getSupportActionBar().show();
                } else {
                    getSupportActionBar().hide();
                }
            } else {
                if (video.orientation != null && !(video.orientation.equals("vertical") && !isPortrait)) {
                    if (start.getRawY() < finish.getRawY()) {
                        controller.hide();
                        gvCurrTags.setVisibility(View.GONE);
                        if (tvNoTags.getVisibility() == View.VISIBLE) {
                            tvNoTags.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        controller.show();
                        gvCurrTags.setVisibility(View.VISIBLE);
                        if (tvNoTags.getVisibility() == View.INVISIBLE) {
                            tvNoTags.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
//        }

        float diffY = start.getRawY() - finish.getRawY();
        float diffX = start.getRawX() - finish.getRawX();
        if ((Math.abs(diffX) - Math.abs(diffY)) > SWIPE_THRESHOLD) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(xVelocity) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    Intent i = new Intent(VideoPlayerActivity.this, AddingTagsActivity.class);
                    i.putExtra("VideoUri", video.uri);
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
        return false;
    }
    @Override
    public void onShowPress(MotionEvent arg0) {
    }
    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        populateCurrentList();
    }

    @Override
    public void onBackPressed() {
        if (player != null) {
            player.pause();
        }
        //player.stop();
        finish();
    }
 private void setShareIntent() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(video.uri));
        startActivity(Intent.createChooser(shareIntent, "Share Image"));
    }
    protected void onPause() {
        super.onPause();
        player.stop();
        //player.release();
    }

    @Override
    protected void onStart() {
        super.onStart();
        RunVideo(uri);
    }

    public void populateCurrentList(){
        List<VidTag> currentTagsList = new ArrayList<>();
        List<VidTag> associatedVidTags = databaseHelper.getAssociatedVidTags(video);
        Map<Integer,VidTag> vidTagMap = new TreeMap<>();
        for (VidTag vidTag: associatedVidTags) {
            if (vidTag.time != -1) {
                vidTagMap.put(vidTag.time, vidTag);
            }
        }
        for (int key: vidTagMap.keySet()) {
            currentTagsList.add(vidTagMap.get(key));
        }
        final VidTagsAdapter adapter = new VidTagsAdapter(this,currentTagsList);
        gvCurrTags.setAdapter(adapter);
        if(currentTagsList.isEmpty()) {
            tvNoTags.setVisibility(View.VISIBLE);
            gvCurrTags.setVisibility(View.INVISIBLE);
        } else {
            tvNoTags.setVisibility(View.GONE);
            gvCurrTags.setVisibility(View.VISIBLE);
            ItemClickSupport.addTo(gvCurrTags)
                    .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                        @Override
                        public void onItemClicked(HorizontalGridView horizontalGridView, int position, View v) {
                            VidTag vidTag = adapter.getItem(position);
                            controller.selectTag(vidTag);
                        }
                    });
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

            player.release();
            player = null;


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player == null) {
            RunVideo(uri);
        }
        if (count == 2) {
            populateCurrentList();
            count = 1;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RunVideo(uri);
    }
}