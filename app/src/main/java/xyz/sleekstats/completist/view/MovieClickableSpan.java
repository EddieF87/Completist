package xyz.sleekstats.completist.view;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class MovieClickableSpan extends ClickableSpan {
    private final View.OnClickListener clickListener;
    private final String filmID;

    public MovieClickableSpan(String filmID, View.OnClickListener clickListener) {
        super();
        this.filmID = filmID;
        this.clickListener= clickListener;
    }

    public void onClick(@NonNull View tv) {
        tv.setTag(filmID);
        clickListener.onClick(tv);
    }

    public void updateDrawState(@NonNull TextPaint ds) {
        ds.setColor(Color.parseColor("#a68bf1"));
//        ds.setUnderlineText(true);
    }
}