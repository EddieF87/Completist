package xyz.sleekstats.completist.view

import android.graphics.Color
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View

class MovieClickableSpan(private val filmID: String, private val clickListener: View.OnClickListener) : ClickableSpan() {
    override fun onClick(tv: View) {
        tv.tag = filmID
        clickListener.onClick(tv)
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.color = Color.parseColor("#a68bf1")
        //        ds.setUnderlineText(true);
    }

}