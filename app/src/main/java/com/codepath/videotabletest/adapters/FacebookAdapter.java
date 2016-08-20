package com.codepath.videotabletest.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.codepath.videotabletest.R;
import com.codepath.videotabletest.models.FacebookVideo;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.List;


public class FacebookAdapter extends ArrayAdapter<FacebookVideo> {

    private static class ViewHolder {
        ImageView pic;
    }

    public FacebookAdapter(Context context, List<FacebookVideo> videos) {
        super(context, android.R.layout.simple_list_item_1, videos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FacebookVideo video = getItem(position);

        ViewHolder viewHolder;
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            viewHolder = new ViewHolder();
            int type = getItemViewType(position);
            convertView = getInflatedLayoutForType(type);
            viewHolder.pic = (ImageView) convertView.findViewById(R.id.ivFbThumbnail);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.pic.setImageResource(0);
        String pic = video.picture.replace("https", "http");
        Picasso.with(getContext())
                .load(pic)
                .into(viewHolder.pic);

        return convertView;
    }


    private View getInflatedLayoutForType(int type) {
        return LayoutInflater.from(getContext()).inflate(R.layout.item_facebook_video, null);
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
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


}