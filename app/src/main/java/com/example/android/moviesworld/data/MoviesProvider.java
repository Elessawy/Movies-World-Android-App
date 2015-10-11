package com.example.android.moviesworld.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by AbdElrahman on 2/10/2015.
 */
public class MoviesProvider extends ContentProvider {

    private static final UriMatcher mUriMatcher = buildUriMatcher();
    static final int MOVIES = 100;
    static final int MOVIES_WITH_MOVIE_ID = 101;
    static final int VIDEOS = 200;
    static final int VIDEOS_WITH_MOVIE_ID = 201;
    static final int REVIEWS = 300;
    static final int REVIEWS_WITH_MOVIE_ID = 301;

    private MoviesDbHelper mMoviesDbHelper;

    private static final String sMovieSelectionWithMovieId =
            MoviesContract.MovieEntry.TABLE_NAME +
                    "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";



    private static UriMatcher buildUriMatcher(){

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        final String authority = MoviesContract.CONTENT_AUTHORITY;
        final String moviesPath = MoviesContract.PATH_MOVIE;

        uriMatcher.addURI(authority, moviesPath , MOVIES);
        uriMatcher.addURI(authority, moviesPath + "/#", MOVIES_WITH_MOVIE_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mMoviesDbHelper = new MoviesDbHelper(getContext());
        return true;
    }


    @Override
    public String getType(Uri uri) {

        final int match = mUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case MOVIES_WITH_MOVIE_ID:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor retCursor;
        String movieId;

        switch (mUriMatcher.match(uri)){
            case MOVIES: {
                retCursor = mMoviesDbHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case MOVIES_WITH_MOVIE_ID: {
                movieId = MoviesContract.MovieEntry.getMovieIdFromUri(uri);
                retCursor = mMoviesDbHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        sMovieSelectionWithMovieId,
                        new String[]{movieId},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();
        Uri returnUri;

        switch (mUriMatcher.match(uri)){
            case MOVIES: {
                long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.MovieEntry.buildMoviesUri(_id);
                else
                    throw new android.database.SQLException("Faild to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri>?? : " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();
        int rowsDeleted;

        if(selection == null) selection = "1";

        switch (mUriMatcher.match(uri)){

            case MOVIES: {
                rowsDeleted = db.delete(MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri : " + uri);
        }

        if(rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return rowsDeleted;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();
        int rowsUpdated;

        switch (mUriMatcher.match(uri)){

            case MOVIES: {
                rowsUpdated = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri : " + uri);
        }

        if(rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }


        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        switch (mUriMatcher.match(uri)){

            case MOVIES:
                return bulkInsert(uri, values, MoviesContract.MovieEntry.TABLE_NAME);
            default:
                return super.bulkInsert(uri, values);
        }

    }

    private int bulkInsert(Uri uri, ContentValues[] values, String tableName){
        final SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();

        db.beginTransaction();
        int returnCount = 0;
        try{
            for (ContentValues value : values){
                long _id = db.insert(tableName, null, value);
                if(_id != -1)
                    returnCount++;
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return returnCount;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mMoviesDbHelper.close();
        super.shutdown();
    }

}
