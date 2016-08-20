package com.codepath.videotabletest.adapters;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.codepath.videotabletest.R;
import com.codepath.videotabletest.models.Media;
import com.codepath.videotabletest.models.Photo;
import com.codepath.videotabletest.models.Video;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;


public class MediaAdapter extends ArrayAdapter<Media> {


    boolean isVideo;
    private LruCache<String, Bitmap> mMemoryCache;

    private static class ViewHolder {
        ImageView pic;
    }

    public MediaAdapter(Context context, List<Media> medias) {
        super(context, android.R.layout.simple_list_item_1, medias);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Media media = getItem(position);
        isVideo = media.isVideo();
        ViewHolder viewHolder;
        if (media.isVideo()) {
            Video video = media.getVideo();

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                viewHolder = new ViewHolder();
                int type = getItemViewType(position);
                convertView = getInflatedLayoutForType(type, media);
                viewHolder.pic = (ImageView) convertView.findViewById(R.id.ivThumbnail);
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.pic.setImageResource(0);
            ImageView imgview = (ImageView) convertView.findViewById(R.id.VideoPreviewPlayButton);
            if(imgview!=null) {
                imgview.setVisibility(View.VISIBLE);
            }


            if (video.uri.contains("content")) {
                File imgFile = new File(video.thumbnail);
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    viewHolder.pic.setImageBitmap(myBitmap);
                }
            }
            else {
                String picture = video.thumbnail;
                String pic = picture.replace("https", "http");
                Picasso.with(getContext())
                        .load(pic)
                        .into(viewHolder.pic);
            }
            return convertView;
        } else {
            Photo photo = media.getPhoto();
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                viewHolder = new ViewHolder();
                int type = getItemViewType(position);
                convertView = getInflatedLayoutForType(type, media);
                viewHolder.pic = (ImageView) convertView.findViewById(R.id.ivPhotoImage);
                convertView.setTag(viewHolder);

            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.pic.setImageResource(0);
            ImageView imgview = (ImageView) convertView.findViewById(R.id.VideoPreviewPlayButton);
            if(imgview!=null) {
                imgview.setVisibility(View.GONE);
            }
            try {
//                Picasso.with(getContext())
//                        .load(photo.uri)
//                        .into(viewHolder.pic);
                //viewHolder.pic.setImageURI(Uri.parse(photo.uri));

                if (photo.uri.contains("photo")) {
//                    Bitmap rotated = ImagePlayerActivity.rotateBitmapOrientation(photo.uri);
//                    viewHolder.pic.setImageBitmap(rotated);
                    rotateAndLoadPicasso(photo.uri, viewHolder.pic);
                    //Bitmap rotated = qualityRotated(photo.uri);
                    //Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photo.uri), 150, 150);
                    //viewHolder.pic.setImageBitmap(rotated);




                } else {
                    Bitmap rotated = rotateBitmapOrientation(photo.uri);
                    //Bitmap rotated = qualityRotated(photo.uri);
                    viewHolder.pic.setImageBitmap(rotated);
                    //rotateAndLoadPicasso(photo.uri, viewHolder.pic);
                }


                //viewHolder.pic.setImageBitmap(getThumbnail(getContext().getContentResolver(), photo.uri));
                //String path = VideoAdapter.getPhotoThumbnailPathForLocalFile((Activity) getContext(), Uri.parse(photo.uri));
                //viewHolder.pic.setImageBitmap(BitmapFactory.decodeFile(path));
            } catch (Exception e) {
                e.printStackTrace();
            }
            //loadBitmap(photo.uri, viewHolder.pic);
            return convertView;
        }
    }

    public void bitmapInfo() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //BitmapFactory.decodeFile(path);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;
    }


    private View getInflatedLayoutForType(int type, Media media) {
        if (media.isVideo()) {
            return LayoutInflater.from(getContext()).inflate(R.layout.item_video, null);
        } else {
            return LayoutInflater.from(getContext()).inflate(R.layout.item_photo, null);
        }
    }

//    public static int calculateInSampleSize(
//            BitmapFactory.Options options, int reqWidth, int reqHeight) {
//        // Raw height and width of image
//        final int height = options.outHeight;
//        final int width = options.outWidth;
//        int inSampleSize = 1;
//
//        if (height > reqHeight || width > reqWidth) {
//
//            // Calculate ratios of height and width to requested height and width
//            final int heightRatio = Math.round((float) height / (float) reqHeight);
//            final int widthRatio = Math.round((float) width / (float) reqWidth);
//
//            // Choose the smallest ratio as inSampleSize value, this will guarantee
//            // a final image with both dimensions larger than or equal to the
//            // requested height and width.
//            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
//        }
//
//        return inSampleSize;
//    }
//    public static Bitmap decodeSampledBitmapFromResource(String path,
//                                                         int reqWidth, int reqHeight) {
//
//        // First decode with inJustDecodeBounds=true to check dimensions
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(path, options);
//
//        // Calculate inSampleSize
//        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//
//        // Decode bitmap with inSampleSize set
//        options.inJustDecodeBounds = false;
//        return BitmapFactory.decodeFile(path, options);
//    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String data;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            data = params[0];

            try {
                String path = VideoAdapter.getPhotoThumbnailPathForLocalFile((Activity) getContext(), Uri.parse(data));
                return rotateBitmapOrientation(path);
                //return BitmapFactory.decodeFile(path);
                //return getThumbnail(getContext().getContentResolver(), data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
            //return decodeSampledBitmapFromResource(data, 100, 100);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                imageView.setImageResource(0);
                imageView.setImageBitmap(bitmap);
                }
            }

    }

    public void loadBitmap(String uri, ImageView imageView) {
        BitmapWorkerTask task = new BitmapWorkerTask(imageView);
        task.execute(uri);
    }

public Bitmap rotateBitmapOrientation(String photoFilePath) throws Exception {
    File newfile = new File(photoFilePath);
    Bitmap bm1 = getThumbnail(getContext().getContentResolver(), photoFilePath);
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
    public Bitmap qualityRotated(String photoFilePath) throws Exception {
        File newfile = new File(photoFilePath);
        Bitmap bm1 = BitmapFactory.decodeFile(photoFilePath);
        if (newfile.exists()) {
            // Create and configure BitmapFactory
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


    public static Bitmap getThumbnail(ContentResolver cr, String path) throws Exception {

        Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]
                {MediaStore.MediaColumns._ID}, MediaStore.MediaColumns.DATA + "=?", new String[]
                {path}, null);
        if (ca != null && ca.moveToFirst()) {
            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
            ca.close();
            return MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
        }

        ca.close();
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
                Picasso.with(getContext()).load(f).into(ivImage);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                Picasso.with(getContext()).load(f).rotate(180f).into(ivImage);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                Picasso.with(getContext()).load(f).rotate(270f).into(ivImage);
            } else {
                Picasso.with(getContext()).load(f).into(ivImage);
            }
        }
    }

}
