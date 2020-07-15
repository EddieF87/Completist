package xyz.sleekstats.completist.view

import android.graphics.Color
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import xyz.sleekstats.completist.model.Genre

class GenreClickableSpan(private val content: Genre, private val clickListener: View.OnClickListener) : ClickableSpan() {

    override fun onClick(tv: View) {
        tv.tag = content
        clickListener.onClick(tv)
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.color = Color.parseColor("#a68bf1")
    }

}