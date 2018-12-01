package xyz.sleekstats.completist.databinding;

import android.databinding.BindingAdapter;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.FilmByPerson;
import xyz.sleekstats.completist.model.Genre;
import xyz.sleekstats.completist.model.ResultsPOJO;
import xyz.sleekstats.completist.model.WatchCount;
import xyz.sleekstats.completist.view.GenreClickableSpan;
import xyz.sleekstats.completist.view.MovieClickableSpan;

import static xyz.sleekstats.completist.databinding.MovieKeys.LIST_GENRE;
import static xyz.sleekstats.completist.databinding.MovieKeys.LIST_NOWPLAYING;
import static xyz.sleekstats.completist.databinding.MovieKeys.LIST_POPULAR;
import static xyz.sleekstats.completist.databinding.MovieKeys.LIST_QUEUED;
import static xyz.sleekstats.completist.databinding.MovieKeys.LIST_TOPRATED;
import static xyz.sleekstats.completist.databinding.MovieKeys.LIST_WATCHED;

public class BindingUtils {

    public static String setCollapsingToolBarText(String name, WatchCount watchCount) {
        if (watchCount == null) {
            return name;
        }
        return name + ": " + watchCount.getWatched() + "/" + watchCount.getTotal()
                + " (" + watchCount.getWatchedPct() + "%)";
    }

    @BindingAdapter(value = {"detailsContent", "detailsClickable"})
    public static void buildGenreString(TextView textView, List<Genre> genres, final View.OnClickListener clickListener) {
        if (genres == null) {
            return;
        }
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        for (Genre genre : genres) {
            SpannableString ss = new SpannableString(genre.getName());
            ss.setSpan(new GenreClickableSpan(genre, clickListener), 0, genre.getName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.append(ss).append(" / ");
        }
        if (stringBuilder.length() > 2) {
            stringBuilder.delete(stringBuilder.length() - 3, stringBuilder.length());
        }

        textView.setText(stringBuilder, TextView.BufferType.SPANNABLE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
    }

    @BindingAdapter(value = {"mediaID", "tvOrMovie", "siteClickable"})
    public static void buildIdClickable(TextView textView, String id, boolean tvOrMovie, final View.OnClickListener clickListener) {
        textView.setTag(R.id.id, id);
        textView.setTag(R.id.tvOrMovie, tvOrMovie);
        textView.setOnClickListener(clickListener);
    }

    @BindingAdapter(value = {"personID", "personSiteClickable"})
    public static void buildIdClickable(TextView textView, String id, final View.OnClickListener clickListener) {
        if (id == null) {
            textView.setVisibility(View.INVISIBLE);
            return;
        }
        switch (id) {
            case LIST_WATCHED:
            case LIST_QUEUED:
            case LIST_NOWPLAYING:
            case LIST_POPULAR:
            case LIST_TOPRATED:
            case LIST_GENRE:
                textView.setVisibility(View.INVISIBLE);
                return;
            default:
                textView.setTag(R.id.id, id);
                textView.setOnClickListener(clickListener);
                textView.setVisibility(View.VISIBLE);
        }
    }

    @BindingAdapter("detailsText")
    public static void setDetailsText(TextView textView, String text) {
        textView.setText(text);
        textView.setMovementMethod(new ScrollingMovementMethod());
    }

    @BindingAdapter({"releaseDate", "lastDate"})
    public static void setReleaseText(TextView textView, String releaseDate, String lastDate) {
        try {
            String text;
            if (lastDate == null) {
                text = releaseDate.substring(0, 4);
            } else {
                releaseDate = releaseDate.substring(0, 4);
                lastDate = lastDate.substring(0, 4);
                if (releaseDate.equals(lastDate)) {
                    text = releaseDate;
                } else {
                    text = releaseDate + "-" + lastDate;
                }
            }
            textView.setText(text);
        } catch (Exception e) {
            textView.setText("");
        }
    }

    @BindingAdapter({"tvOrMovie", "runtime", "number_of_seasons", "number_of_episodes"})
    public static void setRunTimeText(TextView textView, boolean tvOrMovie, int runtime, int number_of_seasons, int number_of_episodes) {
        String text;
        if (tvOrMovie) {
            text = runtime + " minutes";
        } else {
            text = number_of_seasons + " seasons, " + number_of_episodes + " episodes";
        }
        textView.setText(text);
    }


    @BindingAdapter({"similarText", "detailsClickable"})
    public static void setSimilarText(TextView textView, ResultsPOJO similar, final View.OnClickListener clickListener) {
        if(similar == null) {
            Log.d("similarr", "similar = null");
            return;
        }
        List<FilmByPerson> films = similar.getResults();
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder("Similar: ");
        int maxFilms = Math.min(10, films.size());
        for (int i = 0; i < maxFilms; i++) {
            FilmByPerson film = films.get(i);
            SpannableString ss = new SpannableString(film.getTitle());
            ss.setSpan(new MovieClickableSpan(film.getId(), clickListener), 0, film.getTitle().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.append(ss).append(" / ");
        }
        if (stringBuilder.length() > 2) {
            stringBuilder.delete(stringBuilder.length() - 3, stringBuilder.length());
        }

        textView.setText(stringBuilder, TextView.BufferType.SPANNABLE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.RED);
    }
}
