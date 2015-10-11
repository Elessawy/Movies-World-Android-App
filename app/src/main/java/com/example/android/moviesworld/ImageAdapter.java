package com.example.android.moviesworld;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.example.android.moviesworld.data.MoviesContract;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

    private ArrayList<Movie> data;
    private int sectionNumber;
    private Context mContext;
    private int width;
    private int height;

    public ImageAdapter(Context c, int sectionNumber) {
        this.data = new ArrayList<>();
        this.sectionNumber = sectionNumber;
        mContext = c;
    }

    public void setWidthAndHeight(int width, int height){
        this.width = width;
        this.height = height;
    }

    public int getCount() {
        return data.size();
    }

    public Movie getItem(int position) {
        return data.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.movie_item, parent, false);
        }

        view.getLayoutParams().height = height;
        view.getLayoutParams().width = width;
        view.requestLayout();

        Movie movie = getItem(position);

        ImageView imageView = (ImageView) view.findViewById(R.id.image_view);
        String imageSrc = movie.getPosterPath();
        Picasso.with(mContext).load(mContext.getResources().getString(R.string.poster_image_uri)
                + imageSrc).into(imageView);

        final CheckBox mFavouriteCheckBox = (CheckBox) view.findViewById(R.id.adapter_check_box);

        if(sectionNumber != MainFragment.FAVOURITE_SECTION_NUMBER) {
            //check if movie is favourite
            Cursor resCursor = mContext.getContentResolver().query(
                    MoviesContract.MovieEntry.buildFavouriteMoviesWithMovieId(movie.getMovieId()),
                    new String[]{MoviesContract.MovieEntry.COLUMN_MOVIE_ID},
                    null,
                    null,
                    null
            );

            if (resCursor != null && resCursor.moveToFirst()) {//is Favourite
                mFavouriteCheckBox.setChecked(true);
            } else {//not favourite
                mFavouriteCheckBox.setChecked(false);
            }

            mFavouriteCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Movie checkedMovie = getItem(position);

                    if (mFavouriteCheckBox.isChecked() && sectionNumber!=3) {
                        putMovieToFavourite(checkedMovie);

                    } else if(!mFavouriteCheckBox.isChecked()) {
                        mContext.getContentResolver().delete(
                                MoviesContract.MovieEntry.CONTENT_URI,
                                MoviesContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                                new String[]{Integer.toString(checkedMovie.getMovieId())}
                        );
                    }
                }
            });

        }else{
            mFavouriteCheckBox.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    private void putMovieToFavourite(Movie movie) {

        ContentValues movieValues = new ContentValues();

        movieValues.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, movie.getMovieId());
        movieValues.put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
        movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
        movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT, movie.getVoteCount());
        movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
        movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_IMAGE, movie.getPosterPath());
        movieValues.put(MoviesContract.MovieEntry.COLUMN_COVER_IMAGE, movie.getCoverPath());

        Uri insertedUri = mContext.getContentResolver().insert(
                MoviesContract.MovieEntry.CONTENT_URI,
                movieValues
        );
    }

    public void add(ArrayList<Movie> data) {
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    public void swap(ArrayList<Movie> data){
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    public void addCursor(Cursor data){

        if(data != null ){

            ArrayList<Movie> res = new ArrayList<>();

            while(data.moveToNext()){

                Movie movie = new Movie();

                movie.setMovieId(data.getInt(MainFragment.COL_MOVIE_ID));
                movie.setOriginalTitle(data.getString(MainFragment.COL_ORIGINAL_TITLE));
                movie.setTitle(data.getString(MainFragment.COL_TITLE));
                movie.setReleaseDate(data.getString(MainFragment.COL_RELEASE_DATE));
                movie.setOverview(data.getString(MainFragment.COL_OVERVIEW));
                movie.setVoteAverage(data.getDouble(MainFragment.COL_VOTE_AVERAGE));
                movie.setVoteCount(data.getInt(MainFragment.COL_VOTE_COUNT));
                movie.setCoverPath(data.getString(MainFragment.COL_COVER_IMAGE));
                movie.setPosterPath(data.getString(MainFragment.COL_POSTER_IMAGE));

                res.add(movie);
            }
            this.data.addAll(res);
        }
        notifyDataSetChanged();

    }

    public void swapCursor(Cursor data){
        this.data.clear();
        addCursor(data);
    }


}