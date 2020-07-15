package xyz.sleekstats.completist.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso
import xyz.sleekstats.completist.R

object PicassoBindingAdapter {
    private const val PROFILE_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w200/"
    private const val FILM_ITEM_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w200/"
    private const val FILM_DETAILS_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500/"
    private const val CAST_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w200/"

    @JvmStatic
    @BindingAdapter("profilePoster")
    fun setProfileImage(posterView: ImageView, posterUrl: String?) =
            loadImage(posterView, "$PROFILE_POSTER_BASE_URL$posterUrl", R.drawable.ic_person_gold_24dp)

    @JvmStatic
    @BindingAdapter("filmItemPoster")
    fun setFilmItemImage(posterView: ImageView, posterUrl: String?) =
            loadImage(posterView, "$FILM_ITEM_POSTER_BASE_URL$posterUrl", R.drawable.ic_sharp_movie_92px)

    @JvmStatic
    @BindingAdapter("filmDetailsPoster")
    fun setFilmDetailsImage(posterView: ImageView, posterUrl: String?) =
            loadImage(posterView, "$FILM_DETAILS_POSTER_BASE_URL$posterUrl", R.drawable.ic_sharp_movie_92px)

    @JvmStatic
    @BindingAdapter("castPoster")
    fun setCastImage(posterView: ImageView, posterUrl: String?) =
            loadImage(posterView, "$CAST_POSTER_BASE_URL$posterUrl", R.drawable.ic_person_gold_92dp)

    private fun loadImage(posterView: ImageView, posterUrl: String?, drawable: Int) =
            Picasso.get().load(posterUrl)
                    .placeholder(drawable)
                    .error(drawable)
                    .into(posterView)
}