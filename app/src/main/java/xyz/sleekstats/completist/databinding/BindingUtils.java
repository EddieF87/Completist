package xyz.sleekstats.completist.databinding;

import android.databinding.BindingAdapter;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import xyz.sleekstats.completist.model.Genre;
import xyz.sleekstats.completist.model.WatchCount;
import xyz.sleekstats.completist.view.GenreClickableSpan;

public class BindingUtils {

    public static String setCollapsingToolBarText(String name, WatchCount watchCount) {
        if (watchCount == null) {
            return name;
        }
        return name + ": " + watchCount.getWatched() + "/" + watchCount.getTotal()
                + " (" + watchCount.getWatchedPct() + "%)";
    }

    @BindingAdapter(value = {"genresContent", "genresClickable"})
    public static void buildGenreString(TextView textView, List<Genre> genres, final View.OnClickListener clickListener) {
        if(genres == null) {
            return;
        }
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        for(Genre genre: genres) {
            SpannableString ss = new SpannableString(genre.getName());
            ss.setSpan(new GenreClickableSpan(genre, clickListener), 0, genre.getName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.append(ss).append("/ ");
        }
        if(stringBuilder.length() > 1) {
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        }

        textView.setText(stringBuilder, TextView.BufferType.SPANNABLE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
    }


    @BindingAdapter("detailsText")
    public static void setDetailsText(TextView textView, String text) {
        textView.setText(text);
        textView.setMovementMethod(new ScrollingMovementMethod());
    }
}
