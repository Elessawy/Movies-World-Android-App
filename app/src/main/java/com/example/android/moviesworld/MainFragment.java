package com.example.android.moviesworld;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.moviesworld.data.MoviesContract;

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
public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String LOG_TAG = MainFragment.class.getSimpleName();
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final int FAVOURITE_MOVIE_LOADER = 0;

    public static final String FAVOURITE_SORT = "favourite";
    public static final String POPULARITY_SORT = "popularity.desc";
    public static final String VOTE_AVERAGE_SORT = "vote_average.desc";
    public static final int POPULARITY_SECTION_NUMBER = 1;
    public static final int VOTE_AVERAGE_SECTION_NUMBER = 2;
    public static final int FAVOURITE_SECTION_NUMBER = 3;

    private String mSortType;
    private int mNextPageNumber = 1;
    private int mTotalPages = 1000;
    private boolean mLoading = false;

    private ImageAdapter mImageAdapter;
    private ProgressBar mProgressBar;
    private TextView mNoConnectionTextView;
    private TextView mNoFavouriteTextView;
    private Button mRetryButton;
    private GridView mGridView;
    private int mPosition = 0;

    public static final String MOVIE_ID = "id";
    public static final String ORIGINAL_TITLE = "original_title";
    public static final String TITLE = "title";
    public static final String RELEASE_DATE = "release_date";
    public static final String VOTE_AVERAGE = "vote_average";
    public static final String VOTE_COUNT = "vote_count";
    public static final String OVERVIEW = "overview";
    public static final String POSTER_IMAGE = "poster_image";
    public static final String COVER_IMAGE = "cover_image";

    private static String[] ADAPTER_COLUMNS_PROJECTION = {
            MoviesContract.MovieEntry.TABLE_NAME + "." + MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_MOVIE_ID,
            MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_VOTE_COUNT,
            MoviesContract.MovieEntry.COLUMN_OVERVIEW,
            MoviesContract.MovieEntry.COLUMN_POSTER_IMAGE,
            MoviesContract.MovieEntry.COLUMN_COVER_IMAGE
    };

    static final int COL_ID = 0;
    static final int COL_MOVIE_ID = 1;
    static final int COL_ORIGINAL_TITLE = 2;
    static final int COL_TITLE = 3;
    static final int COL_RELEASE_DATE = 4;
    static final int COL_VOTE_AVERAGE = 5;
    static final int COL_VOTE_COUNT = 6;
    static final int COL_OVERVIEW = 7;
    static final int COL_POSTER_IMAGE = 8;
    static final int COL_COVER_IMAGE = 9;


    public interface Callback{
        public void onItemSelected(Movie movie);
    }


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MainFragment newInstance(int sectionNumber) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MainFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);

        if(sectionNumber == POPULARITY_SECTION_NUMBER) {
            mSortType = POPULARITY_SORT;
        }else if(sectionNumber == VOTE_AVERAGE_SECTION_NUMBER) {
            mSortType = VOTE_AVERAGE_SORT;
        }else{//data will be get from database by loader in onActivityCreated
            mSortType = FAVOURITE_SORT;
        }

        mImageAdapter = new ImageAdapter(getActivity(),sectionNumber);

        Log.v(LOG_TAG, "onCreate of Section Number " + sectionNumber);

        super.onCreate(savedInstanceState);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(mSortType == "favourite")
            getLoaderManager().initLoader(FAVOURITE_MOVIE_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_par);
        if(mLoading)
            mProgressBar.setVisibility(View.VISIBLE);
        else
            mProgressBar.setVisibility(View.GONE);

        mNoConnectionTextView = (TextView) rootView.findViewById(R.id.no_connection_text);
        mNoFavouriteTextView = (TextView) rootView.findViewById(R.id.no_favourites_text);
        mRetryButton = (Button) rootView.findViewById(R.id.retry_button);

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FetchMoviesTask().execute(mSortType, Integer.toString(mNextPageNumber));
            }
        });


        mGridView = (GridView) rootView.findViewById(R.id.grid_view);
        mGridView.setAdapter(mImageAdapter);

        setImageAdapterSize();

        mGridView.setOnItemClickListener(new GridView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                startActivity(createDetailActivityIntent(position));
                ((Callback) getActivity()).onItemSelected(
                        mImageAdapter.getItem(position).clone()
                );
            }
        });

        if(mSortType != "favourite") {
            mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                    mPosition = firstVisibleItem;

                    if (!mLoading && firstVisibleItem + visibleItemCount >= totalItemCount) {
                        // End has been reached
                        if (!isNetworkConnected()) {
                            mNoConnectionTextView.setVisibility(View.VISIBLE);
                            mRetryButton.setVisibility(View.VISIBLE);
                        }else if (mNextPageNumber > mTotalPages) {
                            //no more pages
                        } else{
                            //load more & make loading progress bar visible
                            new FetchMoviesTask().execute(mSortType, Integer.toString(mNextPageNumber));
                        }
                    }

                }

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

            });

        }

        int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        Log.v(LOG_TAG, "onCreateView of Section Number " + sectionNumber);

        return rootView;
    }


    @Override
    public void onResume() {
        if(mSortType == "favourite"){
            getLoaderManager().restartLoader(FAVOURITE_MOVIE_LOADER,null,this);
        }
        mImageAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        setImageAdapterSize();
        mGridView.setNumColumns(getResources().getInteger(R.integer.columns_number));

    }

    private void setImageAdapterSize(){
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = 0;
        int numCol = getResources().getInteger(R.integer.columns_number) ;
        if(MainActivity.mTwoPane) {
            width = (int)(((double)display.getWidth()/7 * 3.0)/(double)numCol);
        }else{
            width = (int)(((double)display.getWidth()) / (double)numCol);
        }
        int height = (int)((double)width/185 * 277.0);
        mImageAdapter.setWidthAndHeight(width, height);

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {// There are no active networks.
            return false;
        } else
            return true;
    }


    ////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////AsyncTask/////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////

    private class FetchMoviesTask extends AsyncTask<String , Void, ArrayList> {

        final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private ArrayList<Movie> getDataFromJson(String jsonString) throws JSONException {

            ArrayList<Movie> resultArrayList = new ArrayList<>();

            final String OWN_TOTAL_PAGES = "total_pages";
            final String OWM_RESULTS = "results";
            final String OWM_MOVIE_ID = "id";
            final String OWM_ORIGINAL_TITLE = "original_title";
            final String OWM_TITLE = "title";
            final String OWM_RELEASE_DATE = "release_date";
            final String OWM_OVERVIEW = "overview";
            final String OWM_VOTE_AVERAGE = "vote_average";
            final String OWM_VOTE_COUNT = "vote_count";
            final String OWM_POSTER_PATH = "poster_path";
            final String OWM_BACKDROP_PATH = "backdrop_path";

            JSONObject mainJsonObject = new JSONObject(jsonString);
            JSONArray resultsJsonArray = mainJsonObject.getJSONArray(OWM_RESULTS);

            for (int i = 0; i < resultsJsonArray.length(); i++) {

                Movie newMovie = new Movie();
                JSONObject movieDetailsJsonObject = resultsJsonArray.getJSONObject(i);

                newMovie.setMovieId(movieDetailsJsonObject.getInt(OWM_MOVIE_ID));
                newMovie.setOriginalTitle(movieDetailsJsonObject.getString(OWM_ORIGINAL_TITLE));
                newMovie.setTitle(movieDetailsJsonObject.getString(OWM_TITLE));
                newMovie.setReleaseDate(movieDetailsJsonObject.getString(OWM_RELEASE_DATE));
                newMovie.setOverview(movieDetailsJsonObject.getString(OWM_OVERVIEW));
                newMovie.setVoteAverage(movieDetailsJsonObject.getDouble(OWM_VOTE_AVERAGE));
                newMovie.setVoteCount(movieDetailsJsonObject.getInt(OWM_VOTE_COUNT));
                newMovie.setPosterPath(movieDetailsJsonObject.getString(OWM_POSTER_PATH));
                newMovie.setCoverPath(movieDetailsJsonObject.getString(OWM_BACKDROP_PATH));

                resultArrayList.add(newMovie);

                Log.v(LOG_TAG, "Movie : " + newMovie.getTitle());
            }

            mTotalPages = mainJsonObject.getInt(OWN_TOTAL_PAGES);

            return resultArrayList;
        }


        @Override
        protected void onPreExecute() {
            mLoading = true;
            mProgressBar.setVisibility(View.VISIBLE);
            mNoConnectionTextView.setVisibility(View.GONE);
            mRetryButton.setVisibility(View.GONE);
        }

        @Override
        protected ArrayList doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonString = null;

            final String API_KEY = getResources().getString(R.string.api_key);

            try {
                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String KEY_PARAM = "api_key";
                final String SORT_PARAM = "sort_by";
                final String PAGE_PARAM = "page";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(KEY_PARAM, API_KEY)
                        .appendQueryParameter(SORT_PARAM, params[0])
                        .appendQueryParameter(PAGE_PARAM, params[1])
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // create the request & open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                //read the input steam into a string
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    //do nothing
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                jsonString = buffer.toString();

                Log.v(LOG_TAG, "json Result : " + jsonString);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getDataFromJson(jsonString);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;

        }


        @Override
        protected void onPostExecute(ArrayList arrayList) {
            if (arrayList != null && !arrayList.isEmpty()) {
                mImageAdapter.add(arrayList);
                mNextPageNumber++;
                mNoConnectionTextView.setVisibility(View.GONE);
                mRetryButton.setVisibility(View.GONE);
            } else if (!isNetworkConnected()) {
                mNoConnectionTextView.setVisibility(View.VISIBLE);
                mRetryButton.setVisibility(View.VISIBLE);
            }
            mLoading = false;
            mProgressBar.setVisibility(View.GONE);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////LOADER/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getActivity(),
                MoviesContract.MovieEntry.buildFavouriteMoviesUri(),
                ADAPTER_COLUMNS_PROJECTION,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mImageAdapter.swapCursor(data);

        if(mImageAdapter == null || mImageAdapter.getCount() == 0){
            mNoFavouriteTextView.setVisibility(View.VISIBLE);
        }else {
            mNoFavouriteTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mImageAdapter.swapCursor(null);
    }
}
