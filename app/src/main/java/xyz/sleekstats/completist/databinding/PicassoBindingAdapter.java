package xyz.sleekstats.completist.databinding;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import xyz.sleekstats.completist.R;

public class PicassoBindingAdapter {

    private static final String PROFILE_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w200/";
    private static final String FILM_ITEM_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w200/";
    private static final String FILM_DETAILS_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500/";
    private static final String CAST_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w200/";

    @BindingAdapter("profilePoster")
    public static void setProfileImage(ImageView posterView, String posterUrl) {
        loadImage(posterView, PROFILE_POSTER_BASE_URL + posterUrl, R.drawable.ic_sharp_account_box_92px);
    }

    @BindingAdapter("filmItemPoster")
    public static void setFilmItemImage(ImageView posterView, String posterUrl) {
        loadImage(posterView, FILM_ITEM_POSTER_BASE_URL + posterUrl, R.drawable.ic_sharp_movie_92px);
    }

    @BindingAdapter("filmDetailsPoster")
    public static void setFilmDetailsImage(ImageView posterView, String posterUrl) {
        loadImage(posterView, FILM_DETAILS_POSTER_BASE_URL + posterUrl, R.drawable.ic_sharp_movie_92px);
    }

    @BindingAdapter("castPoster")
    public static void setCastImage(ImageView posterView, String posterUrl) {
        loadImage(posterView, CAST_POSTER_BASE_URL + posterUrl, R.drawable.ic_sharp_account_box_92px);
    }

    private static void loadImage(ImageView posterView, String posterUrl, int drawable) {
        Picasso.get().load(posterUrl)
                .placeholder(drawable)
                .error(drawable)
                .into(posterView);
    }
}
