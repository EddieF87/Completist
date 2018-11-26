package xyz.sleekstats.completist.view;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import xyz.sleekstats.completist.model.FilmByPerson;

public class MovieClickableSpan extends ClickableSpan {
    private final View.OnClickListener clickListener;
    private String filmID;

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
        ds.setColor(Color.parseColor("#689899"));
//        ds.setUnderlineText(true);
    }
}