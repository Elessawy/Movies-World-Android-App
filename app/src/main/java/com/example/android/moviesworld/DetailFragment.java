package com.example.android.moviesworld;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.moviesworld.data.MoviesContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private final String LOG_TAG = DetailFragment.class.getSimpleName();

    private VideoAdapter mVideoAdapter;
    private ReviewAdapter mReviewAdapter;

    private ImageView mCoverImage;
    private ImageView mPosterImage;
    private TextView mOriginalTitleText;
    private TextView mDateText;
    private TextView mVoteAverageAndCountText;
    private TextView mOverviewText;
    private CheckBox mFavouriteCheckBox;
    private ListView mVideosListView;
    private TextView mReviewTitle;
    private TextView mReviewAuthor;
    private TextView mReview;
    private TextView mSeeMore;
    private TextView mTrailersTitle;
    private ListView mReviewsListView;
    private TextView mDialogeSeeMoreText;

    private int movieId;
    private String originalTitle;
    private String title;
    private String releaseDate;
    private double voteAverage;
    private int voteCount;
    private String overview;
    private String posterImage;
    private String coverImage;

    private int mNextPageNumber = 1;
    private int mTotalPageNumber = 1000;

    private ShareActionProvider mShareActionProvider;

    public static final int INDEX_VIDEO_KEY = 0;
    public static final int INDEX_VIDEO_TYPE = 1;
    public static final int INDEX_VIDEO_NAME = 2;
    public static final int INDEX_REVIEW_ID = 0;
    public static final int INDEX_REVIEW_AUTHOR = 1;
    public static final int INDEX_REVIEW_CONTENT = 2;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.fragment_menu_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if(mVideoAdapter != null && mVideoAdapter.getCount() != 0){
            mShareActionProvider.setShareIntent(createShareVideoIntent());
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        mVideoAdapter = new VideoAdapter(getActivity());
        mReviewAdapter = new ReviewAdapter(getActivity());

        setMovieDetails();

        String movieID = Integer.toString(movieId);

        //fetch videos data
        new FetchExtraTask().execute(movieID,
                getResources().getString(R.string.videos_uri));

        Log.v("BODA" , "BODA : " +   getResources().getString(R.string.videos_uri));

        new FetchExtraTask().execute(movieID,
                getResources().getString(R.string.reviews_uri),
                Integer.toString(mNextPageNumber));

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_detail, container, false);

        mCoverImage = (ImageView) view.findViewById(R.id.cover_image);
        mPosterImage = (ImageView) view.findViewById(R.id.poster_image);
        mOriginalTitleText = (TextView) view.findViewById(R.id.original_title);
        mDateText = (TextView) view.findViewById(R.id.release_date);
        mVoteAverageAndCountText = (TextView) view.findViewById(R.id.vote_average_and_count);
        mOverviewText = (TextView) view.findViewById(R.id.overview);
        mFavouriteCheckBox = (CheckBox) view.findViewById(R.id.detail_check_box);
        mReviewTitle = (TextView)view.findViewById(R.id.review_title);
        mReviewAuthor = (TextView) view.findViewById(R.id.first_user_name);
        mReview = (TextView) view.findViewById(R.id.first_review);
        mSeeMore = (TextView) view.findViewById(R.id.see_more);
        mTrailersTitle = (TextView) view.findViewById(R.id.trailers_title);

        mCoverImage.getLayoutParams().height = getCoverHeight();

        setLayoutValues();

        mFavouriteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                if (isChecked) {
                    putMovieToFavourite();
                } else {
                    //delete movie
                    getContext().getContentResolver().delete(
                            MoviesContract.MovieEntry.CONTENT_URI,
                            MoviesContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                            new String[]{Integer.toString(movieId)}
                    );
                }

            }
        });

        mSeeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createReviewsDialog();
            }
        });

        mVideosListView = (ListView) view.findViewById(R.id.videos_list_view);
        mVideosListView.setAdapter(mVideoAdapter);

        mVideosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String videoKey =  mVideoAdapter.getItem(position)[INDEX_VIDEO_KEY];
                Uri videoUri = Uri.parse("http://www.youtube.com/watch?v=" + videoKey);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(videoUri);

                if(intent.resolveActivity(getActivity().getPackageManager()) != null){
                    startActivity(intent);
                }else{
                    Log.d(LOG_TAG, "Couldn't call " + videoUri.toString() + ", no receiving apps installed!");
                }
            }
        });

        return view;
    }

    private int getCoverHeight(){
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int width = 0;
        if(MainActivity.mTwoPane) {
            width = (int)((double)display.getWidth()/7*4.0);
        }else{
            width = display.getWidth();
        }
        int height = width/16 * 9;

        return height;
    }


    private void createReviewsDialog(){

        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.reviews_dialog);

        mReviewsListView = (ListView) dialog.findViewById(R.id.reviews_list_view);
        mReviewsListView.setAdapter(mReviewAdapter);

        mDialogeSeeMoreText = (TextView) dialog.findViewById(R.id.dialog_see_more);

        if (mNextPageNumber <= mTotalPageNumber) {

            mDialogeSeeMoreText.setVisibility(View.VISIBLE);
            mDialogeSeeMoreText.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    new FetchExtraTask().execute(Integer.toString(movieId),
                            getResources().getString(R.string.reviews_uri),
                            Integer.toString(mNextPageNumber));
                }

            });
        }

        dialog.setCancelable(true);
        dialog.setTitle(getResources().getString(R.string.reviews_title));
        dialog.show();
    }

    private void putMovieToFavourite(){
        ContentValues movieValues = new ContentValues();

        movieValues.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE, originalTitle);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, title);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT, voteCount);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, overview);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_IMAGE, posterImage);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_COVER_IMAGE, coverImage);

        Uri insertedUri = getContext().getContentResolver().insert(
                MoviesContract.MovieEntry.CONTENT_URI,
                movieValues
        );
    }

    private void setMovieDetails(){

        Bundle arguments = getArguments();

        if(arguments != null) {

            movieId = arguments.getInt(MainFragment.MOVIE_ID);
            originalTitle = arguments.getString(MainFragment.ORIGINAL_TITLE);
            title = arguments.getString(MainFragment.TITLE);
            releaseDate = arguments.getString(MainFragment.RELEASE_DATE);
            voteAverage = arguments.getDouble(MainFragment.VOTE_AVERAGE);
            voteCount = arguments.getInt(MainFragment.VOTE_COUNT);
            overview = arguments.getString(MainFragment.OVERVIEW);
            posterImage = arguments.getString(MainFragment.POSTER_IMAGE);
            coverImage = arguments.getString(MainFragment.COVER_IMAGE);
        }

    }

    private void setLayoutValues() {

        Picasso.with(getActivity())
                .load(getResources().getString(R.string.cover_image_uri) + coverImage)
                .into(mCoverImage);

        Picasso.with(getActivity())
                .load(getResources().getString(R.string.poster_image_uri) + posterImage)
                .into(mPosterImage);

        mOriginalTitleText.setText(originalTitle);

        if(releaseDate != null && releaseDate.length()>=4){
            mDateText.setText(releaseDate.substring(0,4));
        }else{
            mDateText.setText(getResources().getString(R.string.no_date));
            Log.v("BODA", "BODA: " + releaseDate);
        }

        mVoteAverageAndCountText.setText(voteAverage +  "/10\n(" + voteCount + ")");
        mOverviewText.setText(overview);

        //check if movie is favourite
        Cursor resCursor = getContext().getContentResolver().query(
                MoviesContract.MovieEntry.buildFavouriteMoviesWithMovieId(movieId),
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
    }

    private Intent createShareVideoIntent(){

        String path = getResources().getString(R.string.youtube_videos_uri)
                + mVideoAdapter.getItem(mVideoAdapter.getCount()-1)[INDEX_VIDEO_KEY];

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, path);

        return shareIntent;

    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth =  View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////Async Task//////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    private class FetchExtraTask extends AsyncTask<String, Void, ArrayList<String[]>>{

        private int isVideo = -1;

        private ArrayList getVideosDataFromJson(String jsonString) throws JSONException {

            ArrayList<String []> resultArrayList = new ArrayList<>();

            final String OWM_RESULTS = "results";
            final String OWM_KEY = "key";
            final String OWN_NAME = "name";
            final String OWN_TYPE = "type";
            final String OWM_SITE = "site";

            JSONObject mainJsonObject = new JSONObject(jsonString);
            JSONArray resultsJsonArray = mainJsonObject.getJSONArray(OWM_RESULTS);

            for(int i=0; i<resultsJsonArray.length(); i++){

                String[] newVideo = new String[3];
                JSONObject videoDetailsJsonObject = resultsJsonArray.getJSONObject(i);

                newVideo[INDEX_VIDEO_KEY] = videoDetailsJsonObject.getString(OWM_KEY);
                newVideo[INDEX_VIDEO_TYPE] = videoDetailsJsonObject.getString(OWN_TYPE);
                newVideo[INDEX_VIDEO_NAME] = videoDetailsJsonObject.getString(OWN_NAME);

                if(videoDetailsJsonObject.getString(OWM_SITE).equalsIgnoreCase("youtube"))
                    resultArrayList.add(newVideo);

            }

            return resultArrayList;
        }

        private ArrayList getReviewsDataFromJson(String jsonString) throws  JSONException{

            ArrayList<String []> resultArrayList = new ArrayList<>();

            final String OWM_RESULTS = "results";
            final String OWN_TOTAL_PAGES = "total_pages";
            final String OWN_ID = "id";
            final String OWM_AUTHOR = "author";
            final String OWN_CONTENT = "content";

            JSONObject mainJsonObject = new JSONObject(jsonString);
            JSONArray resultsJsonArray = mainJsonObject.getJSONArray(OWM_RESULTS);

            for(int i=0; i<resultsJsonArray.length(); i++){

                String[] newReview = new String[3];
                JSONObject reviewDetailsJsonObject = resultsJsonArray.getJSONObject(i);

                newReview[INDEX_REVIEW_ID] = reviewDetailsJsonObject.getString(OWN_ID);
                newReview[INDEX_REVIEW_AUTHOR] = reviewDetailsJsonObject.getString(OWM_AUTHOR);
                newReview[INDEX_REVIEW_CONTENT] = reviewDetailsJsonObject.getString(OWN_CONTENT);

                resultArrayList.add(newReview);

            }

            mTotalPageNumber = mainJsonObject.getInt(OWN_TOTAL_PAGES);

            return resultArrayList;
        }


        @Override
        protected ArrayList doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonString = null;

            final String API_KEY = getResources().getString(R.string.api_key);

            try{
                ///135397/videos
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/" + params[1] + "?";
                final String KEY_PARAM = "api_key";
                final String PAGE_PARAM = "page";

                Uri builtUri;

                if(params[1].equals("videos")) {
                    isVideo = 1;
                    builtUri = Uri.parse(BASE_URL).buildUpon()
                            .appendQueryParameter(KEY_PARAM, API_KEY)
                            .build();
                }else if(params[1].equals("reviews")) {
                    isVideo = 0;
                    builtUri = Uri.parse(BASE_URL).buildUpon()
                            .appendQueryParameter(KEY_PARAM, API_KEY)
                            .appendQueryParameter(PAGE_PARAM, params[2])
                            .build();
                }else{
                    Log.e(LOG_TAG, "Error: not videos or reviews");
                    return  null;
                }

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // create the request & open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                //read the input steam into a string
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null){
                    //do nothing
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine()) != null){
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0){
                    return null;
                }

                jsonString = buffer.toString();

                Log.v(LOG_TAG, "json Result : " + jsonString);

            }catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            }finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
                if(reader != null){
                    try{
                        reader.close();
                    }catch (final IOException e){
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try{

                if(params[1] == getResources().getString(R.string.videos_uri))
                    return getVideosDataFromJson(jsonString);
                else {
                    return getReviewsDataFromJson(jsonString);
                }
            }catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String[]> arrayList) {
            if(arrayList != null && !arrayList.isEmpty()){

                if(isVideo == 1) {
                    mVideoAdapter.add(arrayList);
                    setListViewHeightBasedOnChildren(mVideosListView);

                    mTrailersTitle.setVisibility(View.VISIBLE);

                    // Get the provider and hold onto it to set/change the share intent.
                    if(mShareActionProvider != null) {
                        mShareActionProvider.setShareIntent(createShareVideoIntent());
                    }
                }
                else if (isVideo == 0) {

                    mNextPageNumber++;

                    mReviewAdapter.add(arrayList);

                    if(mNextPageNumber <= 2) {
                        mReviewTitle.setVisibility(View.VISIBLE);
                        mReviewAuthor.setVisibility(View.VISIBLE);
                        mReview.setVisibility(View.VISIBLE);

                        mReviewAuthor.setText(arrayList.get(0)[INDEX_REVIEW_AUTHOR]);

                        String review = arrayList.get(0)[INDEX_REVIEW_CONTENT];
                        if(review.length() < 190)
                            mReview.setText(review);
                        else{
                            mReview.setText(review.substring(0, 190) + ".....");
                            mSeeMore.setVisibility(View.VISIBLE);
                        }

                        if (arrayList.size() > 1)
                            mSeeMore.setVisibility(View.VISIBLE);

                    }else if(mNextPageNumber > 2 && mNextPageNumber < mTotalPageNumber) {
                        mDialogeSeeMoreText.setVisibility(View.VISIBLE);
                    }

                }
            }


        }
    }

}
