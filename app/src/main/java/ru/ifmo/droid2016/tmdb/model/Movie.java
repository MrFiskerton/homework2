package ru.ifmo.droid2016.tmdb.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Информация о фильме, полученная из The Movie DB API
 */

public class Movie implements Parcelable {

    /**
     * Path изображения постера фильма.
     */
    public final @NonNull String posterPath;

    /**
     * Название фильма на языке оригинала.
     */
    public final @NonNull String originalTitle;

    /**
     * Описание фильма на языке пользователя.
     */
    public final @Nullable String overviewText;

    /**
     * Название фильма на языке пользователя.
     */
    public final @Nullable String localizedTitle;

    public Movie(String posterPath,
                 String originalTitle,
                 String overviewText,
                 String localizedTitle) {
        this.posterPath = posterPath;
        this.originalTitle = originalTitle;
        this.overviewText = overviewText;
        this.localizedTitle = localizedTitle;
    }

    protected Movie(Parcel in) {
        posterPath = in.readString();
        originalTitle = in.readString();
        overviewText = in.readString();
        localizedTitle = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(posterPath);
        dest.writeString(originalTitle);
        dest.writeString(overviewText);
        dest.writeString(localizedTitle);
    }
}
