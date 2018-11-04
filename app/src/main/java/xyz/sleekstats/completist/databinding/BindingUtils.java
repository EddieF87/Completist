package xyz.sleekstats.completist.databinding;

import android.support.v4.content.ContextCompat;

import java.util.List;

import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.Genre;
import xyz.sleekstats.completist.model.WatchCount;

public class BindingUtils {

    public static String setCollapsingToolBarText(String name, WatchCount watchCount) {
        if (watchCount == null) {
            return name;
        }
        return name + ": " + watchCount.getWatched() + "/" + watchCount.getTotal()
                + " (" + watchCount.getWatchedPct() + "%)";
    }

    public static String buildGenreString(List<Genre> genres) {
        if(genres == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(Genre genre: genres) {
            stringBuilder.append(genre.getName()).append("/ ");
        }
        stringBuilder.setLength(stringBuilder.length() - 2);
        return stringBuilder.toString();
    }

    public static String setRating(double rating) {
        return String.valueOf(rating);
    }
}
