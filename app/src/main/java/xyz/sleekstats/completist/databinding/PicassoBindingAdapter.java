package xyz.sleekstats.completist.databinding;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import xyz.sleekstats.completist.R;

public class PicassoBindingAdapter {

    private static final String PROFILE_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w200/";
    private static final String FILM_ITEM_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w200/";

    @BindingAdapter("profilePoster")
    public static void setProfileImage(ImageView posterView, String posterUrl) {
        Picasso.get().load(PROFILE_POSTER_BASE_URL + posterUrl)
                .placeholder(R.drawable.ic_sharp_account_box_92px)
                .error(R.drawable.ic_sharp_account_box_92px)
                .into(posterView);
    }

    @BindingAdapter("filmItemPoster")
    public static void setFilmItemImage(ImageView posterView, String posterUrl) {
        Picasso.get().load(FILM_ITEM_POSTER_BASE_URL + posterUrl)
                .placeholder(R.drawable.ic_sharp_account_box_92px)
                .error(R.drawable.ic_sharp_account_box_92px)
                .into(posterView);
    }
}
