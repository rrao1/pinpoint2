package com.codepath.videotabletest;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.codepath.videotabletest.models.PhoTag;
import com.codepath.videotabletest.models.Photo;
import com.codepath.videotabletest.models.VidTag;
import com.codepath.videotabletest.models.Video;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import java.util.Comparator;


public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Info
    private static final String DATABASE_NAME = "videosDatabase";
    private static final int DATABASE_VERSION = 1;

    private static final String TAG = "DEBUG";

    // Table Names
    private static final String TABLE_VIDEOS = "videos";
    private static final String TABLE_VIDTAGS = "vidtags";
    private static final String TABLE_PHOTOS = "photos";
    private static final String TABLE_PHOTAGS = "photags";


    // Video Table Columns
    private static final String KEY_VIDEO_ID = "id";
    //private static final String KEY_POST_USER_ID_FK = "userId";
    private static final String KEY_VIDEO_URI = "uri";
    private static final String KEY_VIDEO_NAME = "name";
    private static final String KEY_VIDEO_LOCATION = "location";
    private static final String KEY_VIDEO_DATE = "date";
    private static final String KEY_VIDEO_THUMBNAIL = "thumbnail";
    private static final String KEY_VIDEO_ORIENTATION = "orientation";


    // VidTag Table Columns
    private static final String KEY_VIDTAG_ID = "id";
    private static final String KEY_VIDTAG_VIDEO_ID_FK = "videoId";
    private static final String KEY_VIDTAG_LABEL = "label";
    private static final String KEY_VIDTAG_TIME = "time";

    //Photo Table Columns
    private static final String KEY_PHOTO_ID = "id";
    private static final String KEY_PHOTO_URI = "uri";
    private static final String KEY_PHOTO_NAME = "name";
    private static final String KEY_PHOTO_LOCATION = "location";
    private static final String KEY_PHOTO_DATE = "date";
    private static final String KEY_PHOTO_ORIENTATION = "orientation";

    // PhoTag Table Columns
    private static final String KEY_PHOTAG_ID = "id";
    private static final String KEY_PHOTAG_PHOTO_ID_FK = "videoId";
    private static final String KEY_PHOTAG_LABEL = "label";

    private static DatabaseHelper sInstance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_VIDTAGS_TABLE = "CREATE TABLE " + TABLE_VIDTAGS +
                "(" +
                KEY_VIDTAG_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_VIDTAG_VIDEO_ID_FK + " INTEGER REFERENCES " + TABLE_VIDEOS + "," + // Define a foreign key
                KEY_VIDTAG_LABEL + " TEXT," +
                //TODO CHECK IF IT'S AN INTENGER
                KEY_VIDTAG_TIME + " INTEGER" +
                ")";

        String CREATE_VIDEOS_TABLE = "CREATE TABLE " + TABLE_VIDEOS +
                "(" +
                KEY_VIDEO_ID + " INTEGER PRIMARY KEY," +
                KEY_VIDEO_NAME + " TEXT," +
                KEY_VIDEO_LOCATION + " TEXT," +
                KEY_VIDEO_DATE + " TEXT," +
                KEY_VIDEO_THUMBNAIL + " TEXT," +
                KEY_VIDEO_ORIENTATION + " TEXT," +
                KEY_VIDEO_URI + " TEXT" +
                ")";

        String CREATE_PHOTAGS_TABLE = "CREATE TABLE " + TABLE_PHOTAGS +
                "(" +
                KEY_PHOTAG_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_PHOTAG_PHOTO_ID_FK + " INTEGER REFERENCES " + TABLE_PHOTOS + "," + // Define a foreign key
                KEY_PHOTAG_LABEL + " TEXT" +
                ")";

        String CREATE_PHOTOS_TABLE = "CREATE TABLE " + TABLE_PHOTOS +
                "(" +
                KEY_PHOTAG_ID + " INTEGER PRIMARY KEY," +
                KEY_PHOTO_NAME + " TEXT," +
                KEY_PHOTO_LOCATION + " TEXT," +
                KEY_PHOTO_DATE + " TEXT," +
                KEY_PHOTO_ORIENTATION + " TEXT," +
                KEY_PHOTO_URI + " TEXT" +
                ")";



        db.execSQL(CREATE_VIDTAGS_TABLE);
        db.execSQL(CREATE_VIDEOS_TABLE);
        db.execSQL(CREATE_PHOTAGS_TABLE);
        db.execSQL(CREATE_PHOTOS_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIDTAGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIDEOS);
            onCreate(db);
        }
    }

    public void deleteDatabases() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIDTAGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIDEOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTAGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTOS);
        onCreate(db);

    }

    // Insert a vidtag into the database
    public void addVidTag(VidTag vidTag) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            // The user might already exist in the database (i.e. the same user created multiple posts).

            long videoId = addOrUpdateVideo(vidTag.video);

            ContentValues values = new ContentValues();
            values.put(KEY_VIDTAG_VIDEO_ID_FK, videoId);
            values.put(KEY_VIDTAG_LABEL, (vidTag.label).toLowerCase());
            values.put(KEY_VIDTAG_TIME, vidTag.time);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_VIDTAGS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add video tag to database");
        } finally {
            db.endTransaction();
        }
    }

    // Insert or update a user in the database
    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // user already exists) optionally followed by an INSERT (in case the user does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the user's primary key if we did an update.
    public long addOrUpdateVideo(Video video) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_VIDEO_NAME, video.name);
            values.put(KEY_VIDEO_LOCATION, video.location);
            values.put(KEY_VIDEO_DATE, video.date);
            values.put(KEY_VIDEO_URI, video.uri);
            values.put(KEY_VIDEO_THUMBNAIL, video.thumbnail);
            values.put(KEY_VIDEO_ORIENTATION, video.orientation);

            // First try to update the user in case the user already exists in the database
            // This assumes userNames are unique
            int rows = db.update(TABLE_VIDEOS, values, KEY_VIDEO_URI + "= ?", new String[]{video.uri});

            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the user we just updated
                String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_VIDEO_ID, TABLE_VIDEOS, KEY_VIDEO_URI);
                Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(video.uri)});
                try {
                    if (cursor.moveToFirst()) {
                        userId = cursor.getInt(0);
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                // user with this userName did not already exist, so insert new user
                userId = db.insertOrThrow(TABLE_VIDEOS, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add or update user");
        } finally {
            db.endTransaction();
        }
        return userId;
    }

    //Add a photo into the database
    public long addOrUpdatePhoto(Photo photo) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_PHOTO_NAME, photo.name);
            values.put(KEY_PHOTO_LOCATION, photo.location);
            values.put(KEY_PHOTO_DATE, photo.date);
            values.put(KEY_PHOTO_URI, photo.uri);
            values.put(KEY_PHOTO_ORIENTATION, photo.orientation);

            // First try to update the user in case the user already exists in the database
            // This assumes userNames are unique
            int rows = db.update(TABLE_PHOTOS, values, KEY_PHOTO_URI + "= ?", new String[]{photo.uri});

            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the user we just updated
                String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_PHOTO_ID, TABLE_PHOTOS, KEY_PHOTO_URI);
                Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(photo.uri)});
                try {
                    if (cursor.moveToFirst()) {
                        userId = cursor.getInt(0);
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                // user with this userName did not already exist, so insert new user
                userId = db.insertOrThrow(TABLE_PHOTOS, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add or update photo");
        } finally {
            db.endTransaction();
        }
        return userId;
    }

    // Insert a photo tag into the database
    public void addPhoTag(PhoTag phoTag) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            // The user might already exist in the database (i.e. the same user created multiple posts).

            long photoId = addOrUpdatePhoto(phoTag.photo);

            ContentValues values = new ContentValues();
            values.put(KEY_PHOTAG_PHOTO_ID_FK, photoId);
            values.put(KEY_PHOTAG_LABEL, (phoTag.label).toLowerCase());

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_PHOTAGS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add video tag to database");
        } finally {
            db.endTransaction();
        }
    }



    //update a vidtag's attributes
    public long updateVidTag(VidTag vidTag, String newLabel, int newTime) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;
        int vidTagId = getVidTagId(vidTag);

        int videoId = getVideoID((vidTag.video).uri);

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_VIDTAG_VIDEO_ID_FK, videoId);
            values.put(KEY_VIDTAG_LABEL, newLabel);
            values.put(KEY_VIDTAG_TIME, newTime);
            int rows = db.update(TABLE_VIDTAGS, values, KEY_VIDTAG_ID + "= ?", new String[]{Integer.toString(vidTagId)});
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add or update user");
        } finally {
            db.endTransaction();
        }
        return userId;
    }


    //Takes in the uri of a video, returns the video id
    public int getVideoID(String uri) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int id = 0;
        try {

            cursor = db.query(TABLE_VIDEOS, new String[]{KEY_VIDEO_ID, KEY_VIDEO_URI}, KEY_VIDEO_URI + "=?", new String[]{uri}, null, null, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getInt(cursor.getColumnIndex(KEY_VIDEO_ID));
            }
            return id;
        } finally {
            cursor.close();
        }
    }

    //Takes in video id, returns full video object
    public Video getVideo(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String uriStr;
        String location;
        String name;
        String thumbnail;
        String orientation;
        String date;
        Video video = new Video();
        try {

            cursor = db.query(TABLE_VIDEOS, new String[]{KEY_VIDEO_ID, KEY_VIDEO_URI, KEY_VIDEO_LOCATION, KEY_VIDEO_DATE, KEY_VIDEO_NAME, KEY_VIDEO_THUMBNAIL, KEY_VIDEO_ORIENTATION}, KEY_VIDEO_ID + "=?", new String[]{Integer.toString(id)}, null, null, null);


            if (cursor.getCount() > 0) {

                cursor.moveToFirst();
                uriStr = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_URI));
                thumbnail = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_THUMBNAIL));
                date = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_DATE));
                name = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_NAME));
                location = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_LOCATION));
                orientation = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_ORIENTATION));
                video.uri = uriStr;
                video.location = location;
                video.name = name;
                video.date = date;
                video.thumbnail = thumbnail;
                video.orientation = orientation;
            }
            return video;
        } finally {
            cursor.close();
        }
    }

    //Takes in video, returns a list of all vidtags associated with that video
    public List<VidTag> getAssociatedVidTags(Video video) {
        List<VidTag> vidTags = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int id = getVideoID(video.uri);
        if (id == 0) {
            return vidTags;
        }
        try {

            cursor = db.query(TABLE_VIDTAGS, new String[]{KEY_VIDTAG_ID, KEY_VIDTAG_VIDEO_ID_FK, KEY_VIDTAG_LABEL, KEY_VIDTAG_TIME}, KEY_VIDTAG_VIDEO_ID_FK + "=?", new String[]{Integer.toString(id)}, null, null, null);

            if (cursor.moveToFirst()) {

                do {
                    VidTag vidTag = new VidTag();
                    vidTag.video = video;
                    vidTag.label = cursor.getString(cursor.getColumnIndex(KEY_VIDTAG_LABEL));
                    vidTag.time = cursor.getInt(cursor.getColumnIndex(KEY_VIDTAG_TIME));
                    vidTags.add(vidTag);
                } while (cursor.moveToNext());
            }
            return vidTags;
        } finally {
            cursor.close();
        }
    }

    //Takes in video, deletes all tags associated with that video
    public boolean deleteAssociatedVidTags(Video video) {
        int id = getVideoID(video.uri);
        try {
            //Open the database
            SQLiteDatabase database = this.getWritableDatabase();
            //Execute sql query to remove from database
            //NOTE: When removing by String in SQL, value must be enclosed with ''
            database.execSQL("DELETE FROM " + TABLE_VIDTAGS + " WHERE " + KEY_VIDTAG_VIDEO_ID_FK + "= '" + id + "'");
            //Close the database
            database.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //Takes in string query, returns a set of videos which contain tags whose labels match the query
    public Set<Video> getSearchResults(String query) {
        Set<Video> videoResults = new TreeSet<>();
        Set<String> tags = new TreeSet<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {

            cursor = db.query(TABLE_VIDTAGS, new String[]{KEY_VIDTAG_ID, KEY_VIDTAG_VIDEO_ID_FK, KEY_VIDTAG_LABEL, KEY_VIDTAG_TIME}, KEY_VIDTAG_LABEL + "=?", new String[]{query.toLowerCase()}, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    int videoId = cursor.getInt(cursor.getColumnIndex(KEY_VIDTAG_VIDEO_ID_FK));

                    Video video = getVideo(videoId);
                    if (!tags.contains(video.uri)) {
                        videoResults.add(video);
                        tags.add(video.uri);
                    }
                } while (cursor.moveToNext());
            }
            return videoResults;
        } finally {
            cursor.close();
        }


    }

    public Set<Video> getSearchResultsName(String query) {
        Set<Video> videoResults = new TreeSet<>();
        Set<String> tags = new TreeSet<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {

            //cursor = db.query(TABLE_VIDTAGS, new String[]{KEY_VIDTAG_ID, KEY_VIDTAG_VIDEO_ID_FK, KEY_VIDTAG_LABEL, KEY_VIDTAG_TIME}, KEY_VIDTAG_LABEL + "=?", new String[]{query.toLowerCase()}, null, null, null);
            cursor = db.query(TABLE_VIDEOS, new String[]{KEY_VIDEO_ID,KEY_VIDEO_LOCATION,KEY_VIDEO_URI,KEY_VIDEO_THUMBNAIL,KEY_VIDEO_DATE,},KEY_VIDEO_NAME + "=?",new String[]{query.toLowerCase()}, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    int videoId = cursor.getInt(cursor.getColumnIndex(KEY_VIDEO_ID));

                    Video video = getVideo(videoId);
                    if (!tags.contains(video.uri)) {
                        videoResults.add(video);
                        tags.add(video.uri);
                    }
                } while (cursor.moveToNext());
            }
            return videoResults;
        } finally {
            cursor.close();
        }
    }
    public Set<Video> getSearchResultsLocation(String query) {
        Set<Video> videoResults = new TreeSet<>();
        Set<String> tags = new TreeSet<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {

            cursor = db.query(TABLE_VIDEOS, new String[]{KEY_VIDEO_ID, KEY_VIDEO_NAME, KEY_VIDEO_URI, KEY_VIDEO_THUMBNAIL, KEY_VIDEO_DATE}, KEY_VIDEO_LOCATION + "=?", new String[]{query.toLowerCase()}, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    int videoId = cursor.getInt(cursor.getColumnIndex(KEY_VIDEO_ID));

                    Video video = getVideo(videoId);
                    if (!tags.contains(video.uri)) {
                        videoResults.add(video);
                        tags.add(video.uri);
                    }
                } while (cursor.moveToNext());
            }
            return videoResults;
        } finally {
            cursor.close();
        }
    }
    class PhotoComp implements Comparator<Photo>{

        @Override
        public int compare(Photo e1, Photo e2) {
            return e1.uri.compareTo(e2.uri);
        }
    }
    public Set<Photo> getPhotoSearchResults(String query){
        Set<Photo> photoResults = new TreeSet<>(new PhotoComp());
        Set<String> tags = new TreeSet<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {

            cursor = db.query(TABLE_PHOTAGS, new String[]{KEY_PHOTAG_ID, KEY_PHOTAG_PHOTO_ID_FK,KEY_PHOTAG_LABEL}, KEY_PHOTAG_LABEL + "=?", new String[]{query.toLowerCase()}, null, null, null);


            if (cursor.moveToFirst()) {
                do {
                    int photoId = cursor.getInt(cursor.getColumnIndex(KEY_PHOTAG_PHOTO_ID_FK));

                    Photo photo = getPhoto(photoId);
                    if (!tags.contains(photo.uri)) {
                        photoResults.add(photo);
                        tags.add(photo.uri);
                    }
                } while (cursor.moveToNext());
            }
            return photoResults;
        } finally {
            cursor.close();
        }
    }
    public Set<Photo> getPhotoSearchResultsName(String query){
        Set<Photo> photoResults = new TreeSet<>(new PhotoComp());
        Set<String> tags = new TreeSet<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {

            cursor = db.query(TABLE_PHOTOS, new String[]{KEY_PHOTO_ID,KEY_PHOTO_DATE,KEY_PHOTO_URI,KEY_PHOTO_LOCATION,KEY_PHOTO_DATE}, KEY_PHOTO_NAME + "=?", new String[]{query.toLowerCase()}, null, null, null);


            if (cursor.moveToFirst()) {
                do {
                    int photoId = cursor.getInt(cursor.getColumnIndex(KEY_PHOTO_ID));

                    Photo photo = getPhoto(photoId);
                    //photoResults.add(photo);
                    if (!tags.contains(photo.uri)) {
                        photoResults.add(photo);
                        tags.add(photo.uri);
                    }
                } while (cursor.moveToNext());
            }
            return photoResults;
        } finally {
            cursor.close();
        }
    }
    public Set<Photo> getPhotoSearchResultsLocation(String query){
        Set<Photo> photoResults = new TreeSet<>(new PhotoComp());
        Set<String> tags = new TreeSet<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {

            //cursor = db.query(TABLE_VIDEOS, new String[]{KEY_VIDEO_ID, KEY_VIDEO_NAME, KEY_VIDEO_URI, KEY_VIDEO_THUMBNAIL, KEY_VIDEO_DATE}, KEY_VIDEO_LOCATION + "=?", new String[]{query.toLowerCase()}, null, null, null);
            cursor = db.query(TABLE_PHOTOS, new String[]{KEY_PHOTO_ID,KEY_PHOTO_DATE,KEY_PHOTO_URI,KEY_PHOTO_NAME,KEY_PHOTO_DATE}, KEY_PHOTO_LOCATION + "=?", new String[]{query.toLowerCase()}, null, null, null);


            if (cursor.moveToFirst()) {
                do {
                    int photoId = cursor.getInt(cursor.getColumnIndex(KEY_PHOTO_ID));

                    Photo photo = getPhoto(photoId);
                    //photoResults.add(photo);
                    if (!tags.contains(photo.uri)) {
                        photoResults.add(photo);
                        tags.add(photo.uri);
                    }
                } while (cursor.moveToNext());
            }
            return photoResults;
        } finally {
            cursor.close();
        }
    }

    // Gets all vidtags in the database
    public List<VidTag> getAllVidTags() {
        List<VidTag> vidTags = new ArrayList<>();

        // SELECT * FROM POSTS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
        String POSTS_SELECT_QUERY =
                String.format("SELECT * FROM %s LEFT OUTER JOIN %s ON %s.%s = %s.%s",
                        TABLE_VIDTAGS,
                        TABLE_VIDEOS,
                        TABLE_VIDTAGS, KEY_VIDTAG_VIDEO_ID_FK,
                        TABLE_VIDEOS, KEY_VIDEO_ID);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Video newVideo = new Video();
                    newVideo.uri = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_URI));
                    VidTag newVidTag = new VidTag();
                    newVidTag.label = cursor.getString(cursor.getColumnIndex(KEY_VIDTAG_LABEL));
                    newVidTag.time = cursor.getInt(cursor.getColumnIndex(KEY_VIDTAG_TIME));
                    newVidTag.video = newVideo;
                    vidTags.add(newVidTag);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get video tags from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return vidTags;
    }

    //Deletes video from the database, and deletes all tags associated with the video
    public boolean deleteVideo(Video video) {
        String deluri = video.uri;
        Log.d("VideoDelUri", deluri);
        deleteAssociatedVidTags(video);
        try {
            //Open the database
            SQLiteDatabase database = this.getWritableDatabase();
            //Execute sql query to remove from database
            //NOTE: When removing by String in SQL, value must be enclosed with ''
            database.execSQL("DELETE FROM " + TABLE_VIDEOS + " WHERE " + KEY_VIDEO_URI + "= '" + deluri + "'");
            //Close the database
            database.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //Takes in vidtag, returns id for the tag
    public int getVidTagId(VidTag vidTag) {
        List<VidTag> vidTags = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        int videoId = getVideoID((vidTag.video).uri);
        int id = 0;
        Cursor cursor = null;
        try {

            cursor = db.query(TABLE_VIDTAGS, new String[]{KEY_VIDTAG_ID, KEY_VIDTAG_VIDEO_ID_FK, KEY_VIDTAG_LABEL, KEY_VIDTAG_TIME}, KEY_VIDTAG_VIDEO_ID_FK + "=?", new String[]{Integer.toString(videoId)}, null, null, null);

            if (cursor.moveToFirst()) {

                do {
                    if (vidTag.label.equals(cursor.getString(cursor.getColumnIndex(KEY_VIDTAG_LABEL)))
                            && vidTag.time == cursor.getInt(cursor.getColumnIndex(KEY_VIDTAG_TIME))) {
                        id = cursor.getInt(cursor.getColumnIndex(KEY_VIDTAG_ID));
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return id;
    }

    //Deletes a vidtag
    public boolean deleteVidTag(VidTag vidTag) {
        int id = getVidTagId(vidTag);
        if (id == 0) { return false; }
        try {
            //Open the database
            SQLiteDatabase database = this.getWritableDatabase();
            //Execute sql query to remove from database
            //NOTE: When removing by String in SQL, value must be enclosed with ''
            database.execSQL("DELETE FROM " + TABLE_VIDTAGS + " WHERE " + KEY_VIDTAG_ID + "= '" + id + "'");
            //Close the database
            database.close();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Gets all videos in the database
    public List<Video> getAllVideos() {
        List<Video> videos = new ArrayList<>();

        // SELECT * FROM POSTS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_VIDEOS, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Video newVideo = new Video();
                    newVideo.uri = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_URI));
                    newVideo.name = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_NAME));
                    newVideo.location = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_LOCATION));
                    newVideo.date = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_DATE));
                    newVideo.thumbnail = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_THUMBNAIL));
                    newVideo.orientation = cursor.getString(cursor.getColumnIndex(KEY_VIDEO_ORIENTATION));
                    videos.add(newVideo);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get videos from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return videos;
    }


    // Delete all videos and vidtags in the database
    public void deleteAllVidTagsAndVideos() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_VIDTAGS, null, null);
            db.delete(TABLE_VIDEOS, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all vidtags and videos!");
        } finally {
            db.endTransaction();
        }
    }

    //Takes in the uri of a photo, returns the photo id
    public int getPhotoID(String uri) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int id = 0;
        try {

            cursor = db.query(TABLE_PHOTOS, new String[]{KEY_PHOTO_ID, KEY_PHOTO_URI}, KEY_PHOTO_URI + "=?", new String[]{uri}, null, null, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getInt(cursor.getColumnIndex(KEY_PHOTO_ID));
            }
            return id;
        } finally {
            cursor.close();
        }
    }

    //Takes in photo id, returns full photo object
    public Photo getPhoto(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String uriStr;
        String location;
        String name;
        String orientation;
        String date;
        Photo photo = new Photo();
        try {
            cursor = db.query(TABLE_PHOTOS, new String[]{KEY_PHOTO_ID, KEY_PHOTO_URI, KEY_PHOTO_LOCATION, KEY_PHOTO_DATE, KEY_PHOTO_NAME, KEY_PHOTO_ORIENTATION}, KEY_PHOTO_ID + "=?", new String[]{Integer.toString(id)}, null, null, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                uriStr = cursor.getString(cursor.getColumnIndex(KEY_PHOTO_URI));
                date = cursor.getString(cursor.getColumnIndex(KEY_PHOTO_DATE));
                name = cursor.getString(cursor.getColumnIndex(KEY_PHOTO_NAME));
                location = cursor.getString(cursor.getColumnIndex(KEY_PHOTO_LOCATION));
                orientation = cursor.getString(cursor.getColumnIndex(KEY_PHOTO_ORIENTATION));
                photo.uri = uriStr;
                photo.location = location;
                photo.name = name;
                photo.date = date;
                photo.orientation = orientation;
            }
            return photo;
        } finally {
            cursor.close();
        }
    }

    //Takes in vidtag, returns id for the tag
    public int getPhoTagId(PhoTag phoTag) {
        List<PhoTag> phoTags = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        int photoId = getPhotoID((phoTag.photo).uri);
        int id = 0;
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_PHOTAGS, new String[]{KEY_PHOTAG_ID, KEY_PHOTAG_PHOTO_ID_FK,
                    KEY_PHOTAG_LABEL}, KEY_PHOTAG_PHOTO_ID_FK + "=?",
                    new String[]{Integer.toString(photoId)}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    if (phoTag.label.equals(cursor.getString(cursor.getColumnIndex(KEY_VIDTAG_LABEL)))) {
                        id = cursor.getInt(cursor.getColumnIndex(KEY_VIDTAG_ID));
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return id;
    }

    //Deletes a photag
    public boolean deletePhoTag(PhoTag phoTag) {
        int id = getPhoTagId(phoTag);
        if (id == 0) { return false; }
        try {
            //Open the database
            SQLiteDatabase database = this.getWritableDatabase();
            //Execute sql query to remove from database
            //NOTE: When removing by String in SQL, value must be enclosed with ''
            database.execSQL("DELETE FROM " + TABLE_PHOTAGS + " WHERE " + KEY_PHOTAG_ID + "= '" + id + "'");
            //Close the database
            database.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //Deletes photo from the database, and deletes all tags associated with the photo
    public boolean deletePhoto(Photo photo) {
        String deluri = photo.uri;
        deleteAssociatedPhoTags(photo);
        try {
            //Open the database
            SQLiteDatabase database = this.getWritableDatabase();
            //Execute sql query to remove from database
            //NOTE: When removing by String in SQL, value must be enclosed with ''
            database.execSQL("DELETE FROM " + TABLE_PHOTOS + " WHERE " + KEY_PHOTO_URI + "= '" + deluri + "'");
            //Close the database
            database.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //Takes in video, deletes all tags associated with that video
    public boolean deleteAssociatedPhoTags(Photo photo) {
        int id = getPhotoID(photo.uri);
        try {
            //Open the database
            SQLiteDatabase database = this.getWritableDatabase();
            //Execute sql query to remove from database
            //NOTE: When removing by String in SQL, value must be enclosed with ''
            database.execSQL("DELETE FROM " + TABLE_PHOTAGS + " WHERE " + KEY_PHOTAG_PHOTO_ID_FK + "= '" + id + "'");
            //Close the database
            database.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Gets all videos in the database
    public List<Photo> getAllPhotos() {
        List<Photo> photos = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PHOTOS, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Photo newPhoto = new Photo();
                    newPhoto.uri = cursor.getString(cursor.getColumnIndex(KEY_PHOTO_URI));
                    newPhoto.name = cursor.getString(cursor.getColumnIndex(KEY_PHOTO_NAME));
                    newPhoto.location = cursor.getString(cursor.getColumnIndex(KEY_PHOTO_LOCATION));
                    newPhoto.date = cursor.getString(cursor.getColumnIndex(KEY_PHOTO_DATE));
                    newPhoto.orientation = cursor.getString(cursor.getColumnIndex(KEY_PHOTO_ORIENTATION));
                    photos.add(newPhoto);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get photos from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return photos;
    }

    // Gets all phoTags in the database
    public List<PhoTag> getAllPhoTags() {
        List<PhoTag> phoTags = new ArrayList<>();

        String POSTS_SELECT_QUERY =
                String.format("SELECT * FROM %s LEFT OUTER JOIN %s ON %s.%s = %s.%s",
                        TABLE_PHOTAGS,
                        TABLE_PHOTOS,
                        TABLE_PHOTAGS, KEY_PHOTAG_PHOTO_ID_FK,
                        TABLE_PHOTOS, KEY_PHOTO_ID);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Photo newPhoto = new Photo();
                    newPhoto.uri = cursor.getString(cursor.getColumnIndex(KEY_PHOTO_URI));
                    PhoTag phoTag = new PhoTag();
                    phoTag.label = cursor.getString(cursor.getColumnIndex(KEY_PHOTAG_LABEL));
                    phoTag.photo = newPhoto;
                    phoTags.add(phoTag);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get photo tags from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return phoTags;
    }

    public List<PhoTag> getAssociatedPhoTags(Photo photo) {
        List<PhoTag> phoTags = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int id = getPhotoID(photo.uri);
        if (id == 0) {
            return phoTags;
        }
        try {

            cursor = db.query(TABLE_PHOTAGS, new String[]{KEY_PHOTAG_ID, KEY_PHOTAG_PHOTO_ID_FK, KEY_PHOTAG_LABEL}, KEY_PHOTAG_PHOTO_ID_FK + "=?", new String[]{Integer.toString(id)}, null, null, null);

            if (cursor.moveToFirst()) {

                do {
                    PhoTag phoTag = new PhoTag();
                    phoTag.photo = photo;
                    phoTag.label = cursor.getString(cursor.getColumnIndex(KEY_PHOTAG_LABEL));
                    phoTags.add(phoTag);
                } while (cursor.moveToNext());
            }
            return phoTags;
        } finally {
            cursor.close();
        }
    }

    // Delete all videos and vidtags in the database
    public void deleteAllPhoTagsAndPhotos() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_PHOTAGS, null, null);
            db.delete(TABLE_PHOTOS, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all photags and photos!");
        } finally {
            db.endTransaction();
        }
    }

}