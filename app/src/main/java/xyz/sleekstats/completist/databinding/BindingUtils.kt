package xyz.sleekstats.completist.databinding

import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import xyz.sleekstats.completist.R
import xyz.sleekstats.completist.model.Genre
import xyz.sleekstats.completist.model.ResultsPOJO
import xyz.sleekstats.completist.model.WatchCount
import xyz.sleekstats.completist.view.GenreClickableSpan
import xyz.sleekstats.completist.view.MovieClickableSpan

object BindingUtils {

    @JvmStatic
    fun setCollapsingToolBarText(name: String?, watchCount: WatchCount?): String =
            if (watchCount == null) "$name" else "$name: ${watchCount.watched}/${watchCount.total} (${watchCount.watchedPct}%)"

    @BindingAdapter(value = ["detailsContent", "detailsClickable"])
    @JvmStatic
    fun buildGenreString(textView: TextView, genres: List<Genre>?, clickListener: View.OnClickListener) {
        if (genres == null) {
            return
        }
        val stringBuilder = SpannableStringBuilder()
        genres.forEach { genre ->
            val ss = SpannableString(genre.name)
            ss.setSpan(GenreClickableSpan(genre, clickListener), 0, genre.name.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            stringBuilder.append(ss).append(" / ")
        }
        if (stringBuilder.length > 2) {
            stringBuilder.delete(stringBuilder.length - 3, stringBuilder.length)
        }
        textView.apply {
            setText(stringBuilder, TextView.BufferType.SPANNABLE)
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.TRANSPARENT
        }
    }

    @BindingAdapter(value = ["mediaID", "tvOrMovie", "siteClickable"])
    @JvmStatic
    fun buildIdClickable(textView: TextView, id: String?, tvOrMovie: Boolean, clickListener: View.OnClickListener?) {
        textView.apply {
            setTag(R.id.id, id)
            setTag(R.id.tvOrMovie, tvOrMovie)
            setOnClickListener(clickListener)
        }
    }

    @BindingAdapter(value = ["personID", "personSiteClickable"])
    @JvmStatic
    fun buildIdClickable(textView: TextView, id: String?, clickListener: View.OnClickListener?) {
        when (id) {
            null, MovieKeys.LIST_WATCHED, MovieKeys.LIST_QUEUED, MovieKeys.LIST_NOWPLAYING, MovieKeys.LIST_POPULAR, MovieKeys.LIST_TOPRATED, MovieKeys.LIST_GENRE -> textView.visibility = View.INVISIBLE
            else -> textView.apply {
                setTag(R.id.id, id)
                setOnClickListener(clickListener)
                textView.visibility = View.VISIBLE
            }
        }
    }

    @BindingAdapter("detailsText")
    @JvmStatic
    fun setDetailsText(textView: TextView, text: String?) {
        textView.text = text
        textView.movementMethod = ScrollingMovementMethod()
    }

    @BindingAdapter("releaseDate", "lastDate")
    @JvmStatic
    fun setReleaseText(textView: TextView, releaseDate: String?, lastDate: String?) {
        var releaseDateDisplay = releaseDate ?: ""
        var lastDateDisplay = lastDate ?: ""
        try {
            val text: String
            if (lastDateDisplay.isEmpty()) {
                text = releaseDateDisplay.substring(0, 4)
            } else {
                releaseDateDisplay = releaseDateDisplay.substring(0, 4)
                lastDateDisplay = lastDateDisplay.substring(0, 4)
                text = if (releaseDateDisplay == lastDateDisplay) {
                    releaseDateDisplay
                } else {
                    "$releaseDateDisplay-$lastDateDisplay"
                }
            }
            textView.text = text
        } catch (e: Exception) {
            textView.text = ""
        }
    }

    @BindingAdapter("tvOrMovie", "runtime", "number_of_seasons", "number_of_episodes")
    @JvmStatic
    fun setRunTimeText(textView: TextView, tvOrMovie: Boolean, runtime: Int, number_of_seasons: Int, number_of_episodes: Int) {
        textView.text = if (tvOrMovie) {
            "$runtime minutes"
        } else {
            "$number_of_seasons seasons, $number_of_episodes episodes"
        }
    }

    @BindingAdapter("similarText", "detailsClickable")
    @JvmStatic
    fun setSimilarText(textView: TextView, similar: ResultsPOJO?, clickListener: View.OnClickListener) {
        if (similar == null) {
            return
        }
        val films = similar.results
        val stringBuilder = SpannableStringBuilder("Similar: ")
        val maxFilms = 10.coerceAtMost(films.size)
        if(maxFilms == 0) {
            return
        }
        for (i in 0 until maxFilms) {
            val film = films[i]
            val ss = SpannableString(film.title)
            ss.setSpan(MovieClickableSpan(film.id, clickListener), 0, film.title?.length ?: 0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            stringBuilder.append(ss).append(" / ")
        }
        if (stringBuilder.length > 11) {
            stringBuilder.delete(stringBuilder.length - 3, stringBuilder.length)
        }
        textView.apply {
            setText(stringBuilder, TextView.BufferType.SPANNABLE)
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.RED
        }
    }
}