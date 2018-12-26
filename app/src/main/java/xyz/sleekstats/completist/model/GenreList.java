package xyz.sleekstats.completist.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GenreList implements Parcelable {

    @SerializedName("genres")
    private final List<Genre> genres;

    public List<Genre> getGenres() {
        return genres;
    }

    private GenreList(Parcel in) {
        if (in.readByte() == 0x01) {
            genres = new ArrayList<>();
            in.readList(genres, Genre.class.getClassLoader());
        } else {
            genres = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (genres == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(genres);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<GenreList> CREATOR = new Parcelable.Creator<GenreList>() {
        @Override
        public GenreList createFromParcel(Parcel in) {
            return new GenreList(in);
        }

        @Override
        public GenreList[] newArray(int size) {
            return new GenreList[size];
        }
    };
}
