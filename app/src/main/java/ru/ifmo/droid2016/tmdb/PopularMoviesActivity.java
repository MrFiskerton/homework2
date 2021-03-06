package ru.ifmo.droid2016.tmdb;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.ifmo.droid2016.tmdb.loader.LoadResult;
import ru.ifmo.droid2016.tmdb.loader.PopularMoviesLoader;
import ru.ifmo.droid2016.tmdb.loader.ResultType;
import ru.ifmo.droid2016.tmdb.model.Movie;
import ru.ifmo.droid2016.tmdb.utils.RecylcerDividersDecorator;

import static ru.ifmo.droid2016.tmdb.loader.ResultType.ERROR;
import static ru.ifmo.droid2016.tmdb.loader.ResultType.OK;

/**
 * Экран, отображающий список популярных фильмов из The Movie DB.
 */
public class PopularMoviesActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<LoadResult<List<Movie>>> {

    private static final int ITEMS_ON_THE_PAGE  = 20;
    private int              nNewPage           = 1;
    private static String    language           = (String) Locale.getDefault().getLanguage();
    private static boolean   isLanguageChanged  = false;
    private static boolean   thereIsTheInternet = false;

    private RecyclerView recyclerView;
    private TextView errorTextView;
    private ProgressBar progressView;
    private RecyclerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_movies);
        Log.d(TAG, "onCreate");
        progressView = (ProgressBar) findViewById(R.id.progress_bar);
        errorTextView = (TextView) findViewById(R.id.error_text);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new RecylcerDividersDecorator(
                ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null)));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager LLManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (LLManager.findLastVisibleItemPosition() >= (nNewPage * ITEMS_ON_THE_PAGE) - 5) {
                    nNewPage++;
                    Log.d(TAG, "TRYING");
                    getSupportLoaderManager().initLoader(nNewPage, null, getCallback()).forceLoad();
                }
            }
        });

        if (adapter == null) {
            adapter = new RecyclerAdapter(this);
            recyclerView.setAdapter(adapter);
        }

        // Pass all extra params directly to loader
        if (savedInstanceState == null || !savedInstanceState.containsKey("list movies")) {

            progressView.setIndeterminate(true);
            progressView.setVisibility(View.VISIBLE);
            errorTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);

            if (isOnline()) {
                thereIsTheInternet = true;
                final Bundle loaderArgs = getIntent().getExtras();
                getSupportLoaderManager().initLoader(1, loaderArgs, this).forceLoad();
            } else {
                thereIsTheInternet = false;
                displayError(ResultType.NO_INTERNET);
            }
        } else {
            thereIsTheInternet = true;
            ArrayList<Movie> movies = savedInstanceState.getParcelableArrayList("list movies");
            assert movies != null;
            adapter.setMovies(movies);
        }
    }

    @Override
    public Loader<LoadResult<List<Movie>>> onCreateLoader(int nNewPage, Bundle args) {
        Log.d(TAG, "onCreateLoader: language = " + language);
        return new PopularMoviesLoader(this, language, nNewPage);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (adapter.getItemCount() > 0 && thereIsTheInternet) {
            outState.putParcelableArrayList("list movies", adapter.getMovies());
            outState.putInt("nNewPage", nNewPage);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        nNewPage = savedInstanceState.getInt("nNewPage");
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        Bundle mBundleRecyclerViewState = new Bundle();
        Parcelable listState = recyclerView.getLayoutManager().onSaveInstanceState();
        String KEY_RECYCLER_STATE = "recycler_state";
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!language.equals(Locale.getDefault().getLanguage())) {
            Log.d(TAG, "language reloaded");
            for (int i = 2; i <= nNewPage; i++) {
                getSupportLoaderManager().destroyLoader(i);
            }
            language = Locale.getDefault().getLanguage();
            isLanguageChanged = true;
            nNewPage = 1;
            getSupportLoaderManager().restartLoader(nNewPage, null, this).forceLoad();

        }

    }

    @Override
    public void onLoadFinished(Loader<LoadResult<List<Movie>>> loader,
                               LoadResult<List<Movie>> result) {
        Log.d(TAG, "onLoadFinished: " + result);
        if (result.resultType == OK && result.data != null) {
            display(result.data);
            thereIsTheInternet = true;
        } else {
            thereIsTheInternet = false;
            displayError(result.resultType);
        }
    }

    private void display(List<Movie> data) {
        if (data.isEmpty()) {
            displayError(ERROR);
        } else {
            if (isLanguageChanged) {
                adapter.setMovies(data);
            } else {
                adapter.addMovies(data);
            }
            isLanguageChanged = false;
            progressView.setVisibility(View.GONE);
            errorTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void displayError(ResultType e) {
        progressView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(e.toString());
    }

    @Override
    public void onLoaderReset(Loader<LoadResult<List<Movie>>> loader) { /*adapter = null;*/}

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private PopularMoviesActivity getCallback() {
        return this;
    }

    private static final String TAG = "PopularMoviesActivity";
}
