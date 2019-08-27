package com.example.andreeagorcsa.popularmovies2.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;


import com.example.andreeagorcsa.popularmovies2.R;
import com.example.andreeagorcsa.popularmovies2.adapters.MovieAdapter;
import com.example.andreeagorcsa.popularmovies2.models.Movie;
import com.example.andreeagorcsa.popularmovies2.utils.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MovieAdapter.ItemClickHandler {

    public static final String LOG_TAG = MainActivity.class.getName();

    public static final String MOVIE_OBJECT = "movie_object";
    public static final String TOP_RATED = "top rated";
    public static final String MOST_POPULAR = "most popular";
    public static final String IS_FAVORITE = "is_favorite";
    public static final String SORT_TYPE = "sort_type";
    public static final String SPINNER_ITEM = "spinner_item";

    @BindView(R.id.movie_recycler_view)
    RecyclerView mMovieRecyclerView;
    // by default the value of sortType is "popular"
    private String sortType = "popular";
    private MovieAdapter mMovieAdapter;
    private GridLayoutManager mGridLayoutManager;
    private List<Movie> mMovieList;
    private Toolbar mToolbar;
    private Button mHeartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        // TO DO: check if it is correct and if you do not to create a real button

        //getSupportActionBar().setTitle("Popular Movies");

        // Toolbar settings
        mToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("Popular Movies");
        mToolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));

        buildMovieRecyclerView();

        internetConnectionCheck();


        if (savedInstanceState != null) {
            sortType = savedInstanceState.getString(SORT_TYPE);
            //yourSpinner.setSelection(savedInstanceState.getInt("yourSpinner", 0));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SORT_TYPE, sortType);
        // outState.putInt("yourSpinner", yourSpinner.getSelectedItemPosition());
    }

    private void buildMovieRecyclerView() {
        mMovieRecyclerView.setHasFixedSize(true);
        mMovieList = new ArrayList<>();
        mGridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.numberOfColumns));
        mMovieAdapter = new MovieAdapter(this, mMovieList);
        mMovieRecyclerView.setLayoutManager(mGridLayoutManager);
        mMovieRecyclerView.setAdapter(mMovieAdapter);
    }

    public void internetConnectionCheck() {
        // checking for Internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected == true) {
            // running a new AsyncTask with the key word POPULARITY
            new MovieAsyncTask().execute(JsonUtils.POPULARITY);
        } else {
            Toast.makeText(MainActivity.this, "No Internet connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(Movie movie) {
        Intent movieIntent= new Intent(this, DetailActivity.class);
        movieIntent.putExtra(MOVIE_OBJECT, movie);
        startActivity(movieIntent);
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);


        // spinner item part
        MenuItem spinnerItem = menu.findItem(R.id.action_change_movie);

        final Spinner spinner = (Spinner) spinnerItem.getActionView();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_item,
                getResources().getStringArray(R.array.change_movie));

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (parent.getItemAtPosition(position).equals(TOP_RATED)) {
                    // show the top rated movies
                    Toast.makeText(getApplicationContext(), spinner.getSelectedItem() + " movies selected", Toast.LENGTH_SHORT).show();
                    sortType = "top_rated";
                    new MovieAsyncTask().execute(sortType);
                } else if (parent.getItemAtPosition(position).equals(MOST_POPULAR)) {
                    // show the most popular movies
                    Toast.makeText(getApplicationContext(), spinner.getSelectedItem() + " movies selected", Toast.LENGTH_SHORT).show();
                    sortType = "popular";
                    new MovieAsyncTask().execute(sortType);
                } else  if ((parent.getItemAtPosition(position).equals(IS_FAVORITE))){
                    Toast.makeText(getApplicationContext(),  spinner.getSelectedItem() + " movies selected", Toast.LENGTH_SHORT).show();
                    sortType = "is_favorite";
                    // new MovieAsyncTask().execute(sortType);
                } else {
                    // no toast
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        return true;
    }

    /**
     * runs the fetchMovieData(moviesUrl) method at the background thread
     */
    public class MovieAsyncTask extends AsyncTask<String, Void, List<Movie>> {

        @Override
        protected void onPreExecute() {
            mMovieAdapter = (MovieAdapter) mMovieRecyclerView.getAdapter();
        }

        @Override
        protected List<Movie> doInBackground(String... url) {
            try {
                String moviesUrl = JsonUtils.buildUrl(sortType);
                mMovieList = JsonUtils.fetchMovieData(moviesUrl);
                Thread.sleep(1000);
            } catch (IOException e) {
                e.printStackTrace();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return mMovieList;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            mMovieList = movies;
            if (movies != null) {
                mMovieAdapter.setMovieList(movies);
            } else {
                Toast.makeText(MainActivity.this, "Your movie list is empty", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // should be accessed when clicking on "heart" favorite button from the toolbar
    public void showFavouriteMovies() {
        mHeartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isFavourite = readState();

                if (isFavourite) {
                    isFavourite = false;
                    saveState(isFavourite);
                    // show top rated/ most popular

                } else {
                    isFavourite = true;
                    saveState(isFavourite);
                    //show the favorite movies
                }
            }
        });
    }

    private void saveState(boolean isFavourite) {
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                "Favourite", Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEdit = sharedPreferences
                .edit();
        sharedPreferencesEdit.putBoolean("State", isFavourite);
        sharedPreferencesEdit.commit();
    }

    private boolean readState() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                "Favourite", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("State", true);
    }

}
