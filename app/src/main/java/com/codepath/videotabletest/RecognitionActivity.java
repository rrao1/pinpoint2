package com.codepath.videotabletest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
import com.clarifai.api.exception.ClarifaiException;
import com.codepath.videotabletest.activities.ImagePlayerActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecognitionActivity extends Activity {
    private static final String TAG = RecognitionActivity.class.getSimpleName();
    private final ClarifaiClient client = new ClarifaiClient(Credentials.CLIENT_ID,
            Credentials.CLIENT_SECRET);
    private TextView textView;
    ArrayList<String> items;
    MediaMetadataRetriever retriever;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);
        textView = (TextView) findViewById(R.id.text_view);

        String videoUri = getIntent().getStringExtra("videoUri");
        int time = getIntent().getIntExtra("time", -1);
        String photoUri = getIntent().getStringExtra("photoUri");
        if (videoUri != null) {
            final Uri uri = Uri.parse(videoUri);
            retriever = new MediaMetadataRetriever();
            if(uri.toString().contains("http")){
                recognizeFacebookVideo(uri);
            }else {
                retriever.setDataSource(this, uri);
                if (time != -1) {
                    recognizeFrame(time);
                } else {
                    //byte[] videoBytes = convertVideoToBytes(vidUri);
                    recognizeVideo();
                }
            }
        } else {
            recognizePhoto(photoUri);
        }
    }

    private void recognizeFacebookVideo(Uri uri) {
        final String fburi = uri.toString();
        textView.setText("Recognizing...");
        new AsyncTask<File[], Void, List<RecognitionResult>>() {
            @Override
            protected List<RecognitionResult> doInBackground(File[]... params) {
                return client.recognize(new RecognitionRequest(fburi));
            }

            @Override
            protected void onPostExecute(List<RecognitionResult> results) {
                updateUIForResult(results);
            }
        }.execute();
    }

//    private void recognizeThumbnail() {
//        final Uri thumbUri = Uri.parse(getIntent().getStringExtra("thumbnailUri"));
//        textView.setText("Recognizing...");
//            new AsyncTask<Uri, Void, RecognitionResult>() {
//                @Override protected RecognitionResult doInBackground(Uri... uri) {
//                    Log.d(TAG, "User " + thumbUri);
//                    return recognizeVideo(thumbUri);
//                }
//                @Override protected void onPostExecute(RecognitionResult result) {
//                    updateUIForResult(result);
//                }
//            }.execute();
//
//    }

    private File getVideoFrameFile(int time) {
        File f = new File(getApplicationContext().getCacheDir(), "bitmap");
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Convert bitmap to byte array
        Bitmap bitmap = retriever.getFrameAtTime(time * 1000); //unit in microsecond

        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 320,
                320 * bitmap.getHeight() / bitmap.getWidth(), true);

        // Compress the image as a JPEG.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
        byte[] jpeg = out.toByteArray();

        //write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(jpeg);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return f;
    }

    private void recognizeFrame(int time) {
        final Bitmap bitmap = retriever.getFrameAtTime(time * 1000); //unit in microsecond
        textView.setText("Recognizing...");
        new AsyncTask<Bitmap, Void, RecognitionResult>() {
            @Override protected RecognitionResult doInBackground(Bitmap... bitmaps) {
                return recognizeBitmap(bitmaps[0]);
            }
            @Override protected void onPostExecute(RecognitionResult result) {
                updateUIForResult(result);
            }
        }.execute(bitmap);
    }

    private void recognizeVideo() {
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int videoDuration = Integer.parseInt(time);// This will give time in millesecond
        final File[] imageFiles = {
                getVideoFrameFile(0),
                getVideoFrameFile(videoDuration / 2 * 1000),
                getVideoFrameFile(videoDuration * 1000)
        };
        textView.setText("Recognizing...");
        new AsyncTask<File[], Void, List<RecognitionResult>>() {
            @Override
            protected List<RecognitionResult> doInBackground(File[]... params) {
                return client.recognize(new RecognitionRequest(imageFiles));
            }

            @Override
            protected void onPostExecute(List<RecognitionResult> results) {
                updateUIForResult(results);
            }
        }.execute(imageFiles);
    }

    private void recognizePhoto(String photoUri) {
            final Bitmap bitmap = ImagePlayerActivity.rotateBitmapOrientation(photoUri);
            textView.setText("Recognizing...");
            new AsyncTask<Bitmap, Void, RecognitionResult>() {
                @Override protected RecognitionResult doInBackground(Bitmap... bitmaps) {
                    return recognizeBitmap(bitmaps[0]);
                }
                @Override protected void onPostExecute(RecognitionResult result) {
                    updateUIForResult(result);
                }
            }.execute(bitmap);
    }

//    private static void persistImage(Bitmap bitmap, String name) {
//        File filesDir = AddingTagsActivity.getAppContext().getFilesDir();
//        File imageFile = new File(filesDir, name + ".jpg");
//
//        OutputStream os;
//        try {
//            os = new FileOutputStream(imageFile);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
//            os.flush();
//            os.close();
//        } catch (Exception e) {
//            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
//        }
//    }

    private RecognitionResult recognizeBitmap(Bitmap bitmap) {
        try {
            // Scale down the image. This step is optional. However, sending large images over the
            // network is slow and  does not significantly improve recognition performance.
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 320,
                    320 * bitmap.getHeight() / bitmap.getWidth(), true);

            // Compress the image as a JPEG.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
            byte[] jpeg = out.toByteArray();

            // Send the JPEG to Clarifai and return the result.
            return client.recognize(new RecognitionRequest(jpeg)).get(0);
        } catch (ClarifaiException e) {
            Log.e(TAG, "Clarifai error", e);
            return null;
        }
    }

//    /** Sends the given bitmap to Clarifai for recognition and returns the result. */
//    private RecognitionResult recognizeVideo(Uri uri ) {
//        try {
//            return client.recognize(new RecognitionRequest(new File(String.valueOf(uri)))).get(0);
//        } catch (ClarifaiException e) {
//            Log.e(TAG, "Clarifai error", e);
//            return null;
//        }
//    }
//
//    /** Sends the given bitmap to Clarifai for recognition and returns the result. */
//    private void recognizeVideo(final byte[] videoBytes) {
//        textView.setText("Recognizing...");
//        new AsyncTask<byte[], Void, List<RecognitionResult>>() {
//            @Override
//            protected List<RecognitionResult> doInBackground(byte[]... params) {
//                return client.recognize(new RecognitionRequest(videoBytes));
//            }
//
//            @Override
//            protected void onPostExecute(List<RecognitionResult> results) {
//                updateUIForResult(results);
//            }
//        }.execute(videoBytes);
//        new AsyncTask<Uri, Void, RecognitionResult>() {
//            @Override protected RecognitionResult doInBackground(Uri... uri) {
//                return (RecognitionResult) client.recognize(new RecognitionRequest(videoBytes));
//            }
//            @Override protected void onPostExecute(RecognitionResult result) {
//                updateUIForResult(result);
//                Toast.makeText(getApplicationContext(), "Executed!", Toast.LENGTH_SHORT);
//            }
//        }.execute();
//    }
//
//    private byte[] convertVideoToBytes(Uri uri){
//        byte[] videoBytes = null;
//        try {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            FileInputStream fis = new FileInputStream(new File(getRealPathFromURI(this, uri)));
//
//            byte[] buf = new byte[1024];
//            int n;
//            while (-1 != (n = fis.read(buf)))
//                baos.write(buf, 0, n);
//
//            videoBytes = baos.toByteArray();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return videoBytes;
//    }
//
//    private String getRealPathFromURI(Context context, Uri contentUri) {
//        Cursor cursor = null;
//        try {
//            String[] proj = { MediaStore.Video.Media.DATA };
//            cursor = context.getContentResolver().query(contentUri, proj, null,
//                    null, null);
//            int column_index = cursor
//                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
//            cursor.moveToFirst();
//            return cursor.getString(column_index);
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//    }

    /** Updates the UI by displaying tags for the given result. */
    private void updateUIForResult(RecognitionResult result) {
        if (result != null) {
            if (result.getStatusCode() == RecognitionResult.StatusCode.OK) {
                items = new ArrayList<String>();
                for (Tag tag : result.getTags()) {
                    Log.d("Items", tag.getName() + " = " + tag.getProbability());
                    items.add(tag.getName());
                }

            } else {
                Log.e(TAG, "Clarifai: " + result.getStatusMessage());
            }
        } else {
            textView.setText("Sorry,the result = null");
            Log.d(TAG, "User " + "please failed");

        }
        getSugTags();
    }

    /** Updates the UI by displaying tags for the given result. */
    private void updateUIForResult(List<RecognitionResult> results) {
        if (results != null) {
            items = new ArrayList<String>();
            for (RecognitionResult result: results) {
                if (result.getStatusCode() == RecognitionResult.StatusCode.OK) {
                    for (Tag tag : result.getTags()) {
                        //Log.d("Items", tag.getName() + " = " + tag.getProbability());
                        if(!items.contains(tag.getName())) {
                            items.add(tag.getName());
                        }
                    }
                } else {
                    Log.e(TAG, "Clarifai: " + result.getStatusMessage());
                }
            }
        } else {
            textView.setText("Sorry,the result = null");
            Log.d(TAG, "User " + "please failed");
        }
        getSugTags();
    }

    public void getSugTags() {
        Intent data = new Intent();
        Bundle args = new Bundle();
        args.putStringArrayList("ARRAYLIST",items);
        data.putExtra("BUNDLE", args);
        data.putExtra("code", 20);
        setResult(RESULT_OK, data);
        finish(); //
    }
}