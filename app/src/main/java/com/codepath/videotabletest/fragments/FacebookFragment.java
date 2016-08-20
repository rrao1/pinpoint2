package com.codepath.videotabletest.fragments;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.codepath.videotabletest.BuildConfig;
import com.codepath.videotabletest.DatabaseHelper;
import com.codepath.videotabletest.R;
import com.codepath.videotabletest.activities.AddingTagsActivity;
import com.codepath.videotabletest.adapters.FacebookAdapter;
import com.codepath.videotabletest.models.FacebookVideo;
import com.codepath.videotabletest.models.Video;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FacebookFragment extends Fragment {
    CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;
    AccessToken accessToken;
    List<FacebookVideo> fbVideos = new ArrayList<>();
    Map<String, String> map = new TreeMap<>();
    FacebookAdapter facebookAdapter;
    GridView gvFacebook;
    DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        FacebookSdk.sdkInitialize(getActivity());

        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }


        callbackManager = CallbackManager.Factory.create();
        return inflater.inflate(R.layout.fragment_facebook, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoginButton loginButton = (LoginButton) getView().findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
            }
        };

        facebookAdapter = new FacebookAdapter(getActivity(), fbVideos);
        gvFacebook = (GridView) getView().findViewById(R.id.gvFacebook);
        gvFacebook.setAdapter(facebookAdapter);




        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null && accessToken.getUserId() != null) {
        // API CALL TO GET ALL OF A USER'S VIDEOS
            Bundle b1 = new Bundle();
            b1.putString("fields", "id, picture, source, title, created_time");
            GraphRequest request1 = new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/" + accessToken.getUserId() + "/videos/uploaded",
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            JSONObject obj = response.getJSONObject();
                            fbVideos.clear();

                            try {
                                JSONArray arry = obj.getJSONArray("data");
                                for (int i = 0; i < arry.length(); i++) {
                                    FacebookVideo fbVid = new FacebookVideo();
                                    JSONObject video = arry.getJSONObject(i);
                                    String id = video.getString("id");
                                    String picture = video.getString("picture");
                                    String source = video.getString("source");
                                    if (video.has("title")) {
                                        String name = video.getString("title");
                                        fbVid.name = name;
                                    }
                                    if (video.has("created_time")) {
                                        String date = video.getString("created_time");
                                        fbVid.date = date;
                                    }
                                    fbVid.id = id;
                                    fbVid.picture = picture;
                                    fbVid.url = source;
                                    fbVideos.add(fbVid);
                                    gvFacebook.setAdapter(facebookAdapter);
                                    facebookAdapter.notifyDataSetChanged();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );
            request1.setParameters(b1);
            request1.executeAsync();
            databaseHelper = DatabaseHelper.getInstance(getActivity());
            setUpListener();

        }
        //debugging
        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo(
                    "com.codepath.videotabletest",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    public void method(View view) {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_videos"));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }





    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public void setUpListener() {
        gvFacebook.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter,
                                            View item, int pos, long id) {
                        final FacebookVideo video = fbVideos.get(pos);
                        Bundle b = new Bundle();
                        b.putString("fields", "source");
                        GraphRequest request = new GraphRequest(accessToken,
                                "/" + video.id,
                                null,
                                HttpMethod.GET,
                                new GraphRequest.Callback() {
                                    public void onCompleted(GraphResponse response) {
                                        JSONObject obj = response.getJSONObject();
                                        try {

                                            String source = obj.getString("source");
                                            Video video1 = new Video();
                                            video1.uri = source;
                                            video1.thumbnail = video.picture;
                                            video1.uri = video.url;
                                            video1.name = video.name;

                                            String dateString = video.date.toString();

                                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                                            Date date = dateFormat.parse(dateString);

                                            dateFormat = new SimpleDateFormat("MMM dd, yyyy");
                                            String formatedDate = dateFormat.format(date);


                                            video1.date = formatedDate;

                                            //video1.date = video.date;
                                            databaseHelper.addOrUpdateVideo(video1);
                                            Intent i = new Intent(getActivity(), AddingTagsActivity.class);
                                            i.putExtra("VideoUri", video1.uri);
                                            i.putExtra("main", "yes");
                                            startActivity(i);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (ParseException e) {
                                            Toast.makeText(getContext(),"parse error", Toast.LENGTH_SHORT);
                                        }
                                    }
                                }
                        );
                        request.setParameters(b);
                        request.executeAsync();
                    }
                }
        );
    }

}
