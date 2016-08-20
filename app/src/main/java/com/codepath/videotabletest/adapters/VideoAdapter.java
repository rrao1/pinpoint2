package com.codepath.videotabletest.adapters;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.codepath.videotabletest.R;
import com.codepath.videotabletest.models.Video;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class VideoAdapter extends BaseAdapter {
    private final Context context;
    List<Video> showvideos;
    Bitmap bitmap;
    String thumbnailPath;
    public static String[] thumbColumns = { MediaStore.Video.Thumbnails.DATA };
    public static String[] mediaColumns = { MediaStore.Video.Media._ID };

    public VideoAdapter(Context localContext, List<Video> showthesevideos) {
        context = localContext;
        showvideos = showthesevideos;


    }
    public int getCount()
    {
        return showvideos.size();
    }
    public Object getItem(int position)
    {
        return position;
    }
    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ImageView picturesView;
        LayoutInflater mInflater = (LayoutInflater)
                context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_video, null);
            picturesView = new ImageView(context);
            if(showvideos.get(position) != null)
            {
                Uri uri = Uri.parse(showvideos.get(position).uri);
                thumbnailPath = getThumbnailPathForLocalFile((Activity) context, uri);
                bitmap = BitmapFactory.decodeFile(thumbnailPath); //Creation of Thumbnail of image
                //bitmap = scaled(bitmap);
            }

            picturesView.setImageURI(Uri.parse(thumbnailPath));
            picturesView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            picturesView.setLayoutParams(new GridView.LayoutParams(500, 500));
            convertView.setTag(picturesView);
        }
        else {
            picturesView = (ImageView) convertView;
        }
        notifyDataSetChanged();
        //return ivThumbnail;
        return picturesView;
    }

    public Bitmap scaled(Bitmap srcBmp) {
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){


            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        }else{

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }
        return dstBmp;
    }
    //Converts uri to Thumbnail image
    public static String getThumbnailPathForLocalFile(Activity context, Uri fileUri) {

        long fileId = getFileId(context, fileUri);


        MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(),
                fileId, MediaStore.Video.Thumbnails.MICRO_KIND, null);

        Cursor thumbCursor = null;
        try {
            thumbCursor = context.managedQuery(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                    thumbColumns, MediaStore.Video.Thumbnails.VIDEO_ID + " = "
                            + fileId, null, null);

            if (thumbCursor.moveToFirst()) {
                String thumbPath = thumbCursor.getString(thumbCursor
                        .getColumnIndex(MediaStore.Video.Thumbnails.DATA));

                return thumbPath;
            }

        } finally {
        }

        return null;
    }

    //Gets the id of the file
    public static long getFileId(Activity context, Uri fileUri) {

        Cursor cursor = context.managedQuery(fileUri, mediaColumns, null, null,
                null);

        if (cursor.moveToFirst()) {
            int columnIndex = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int id = cursor.getInt(columnIndex);

            return id;
        }
        return 0;
    }


    //Gets the id of the file
    public static long getPhotoFileId(Activity context, Uri fileUri) {

        Cursor cursor = context.managedQuery(fileUri, mediaColumns, null, null,
                null);

        if (cursor.moveToFirst()) {
            int columnIndex = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int id = cursor.getInt(columnIndex);

            return id;
        }
        return 0;
    }

    //Converts uri to Thumbnail image
    public static String getPhotoThumbnailPathForLocalFile(Activity context, Uri fileUri) {

        long fileId = getPhotoFileId(context, fileUri);


        MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(),
                fileId, MediaStore.Images.Thumbnails.MICRO_KIND, null);

        Cursor thumbCursor = null;
        try {
            thumbCursor = context.managedQuery(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                    thumbColumns, MediaStore.Images.Thumbnails.IMAGE_ID + " = "
                            + fileId, null, null);

            if (thumbCursor.moveToFirst()) {
                String thumbPath = thumbCursor.getString(thumbCursor
                        .getColumnIndex(MediaStore.Images.Thumbnails.DATA));

                return thumbPath;
            }

        } finally {
        }

        return null;
    }

    public static Bitmap getThumbnail(ContentResolver cr, String path) throws Exception {

        Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.MediaColumns._ID }, MediaStore.MediaColumns.DATA + "=?", new String[] {path}, null);
        if (ca != null && ca.moveToFirst()) {
            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
            ca.close();
            return MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MICRO_KIND, null );
        }

        ca.close();
        return null;

    }


}
