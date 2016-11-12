package ru.ifmo.droid2016.tmdb.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.facebook.stetho.urlconnection.StethoURLConnectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;

import ru.ifmo.droid2016.tmdb.api.TmdbApi;
import ru.ifmo.droid2016.tmdb.model.Movie;
import ru.ifmo.droid2016.tmdb.utils.IOUtils;

/**
 * Created by Mr.Fiskerton on 012 12.11.16.
 */

public class PopularMoviesLoader extends AsyncTaskLoader<LoadResult<List<Movie>>> {

    private final String language;

    public PopularMoviesLoader(Context context, String language) {
        super(context);
        this.language = language;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public LoadResult<List<Movie>> loadInBackground() {
        final StethoURLConnectionManager stethoManager = new StethoURLConnectionManager("API");

        ResultType resultType = ResultType.ERROR;
        List<Movie> data = null;

        HttpURLConnection connection = null;
        InputStream in = null;

        try {
            connection = TmdbApi.getPopularMoviesRequest(language);//TODO:
            Log.d(TAG, "Performing request: " + connection.getURL());

            stethoManager.preConnect(connection, null);
            connection.connect();
            stethoManager.postConnect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                in = connection.getInputStream();
                in = stethoManager.interpretResponseStream(in);

                IOUtils.readFully(in);

                resultType = ResultType.OK;

            } else {
                // consider all other codes as errors
                throw new BadResponseException("HTTP: " + connection.getResponseCode()
                        + ", " + connection.getResponseMessage());
            }


        } catch (MalformedURLException e) {
            Log.e(TAG, "Failed to get movies", e);

        } catch (IOException e) {
            stethoManager.httpExchangeFailed(e);

        } catch (Exception e) {
            Log.e(TAG, "Failed to get movies: ", e);

        } finally {
            IOUtils.closeSilently(in);
            if (connection != null) {
                connection.disconnect();
            }
        }

        return new LoadResult<>(resultType, data);
    }

    private static final String TAG = "Movies";
}
