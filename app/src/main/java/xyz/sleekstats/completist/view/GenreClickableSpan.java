package xyz.sleekstats.completist.view;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import xyz.sleekstats.completist.model.Genre;

public class GenreClickableSpan extends ClickableSpan {
    private final View.OnClickListener clickListener;
    private Genre content;

    public GenreClickableSpan(Genre content, View.OnClickListener clickListener) {
        super();
        this.content = content;
        this.clickListener= clickListener;
    }

    public void onClick(@NonNull View tv) {
        tv.setTag(content);
        clickListener.onClick(tv);
    }

    public void updateDrawState(@NonNull TextPaint ds) {
        ds.setColor(Color.parseColor("#689899"));
        ds.setUnderlineText(true); // set to false to remove underline
    }
}