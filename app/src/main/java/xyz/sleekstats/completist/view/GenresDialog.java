package xyz.sleekstats.completist.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import java.util.List;

import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.Genre;
import xyz.sleekstats.completist.model.GenreList;

public class GenresDialog extends DialogFragment implements GenresAdapter.ItemClickListener {

    private static final String KEY_GENRES = "key_genres";
    private GenreSelector mGenreSelector;

    public GenresDialog() {
    }

    public static GenresDialog newInstance(GenreList genreList) {

        Bundle args = new Bundle();
        GenresDialog fragment = new GenresDialog();
        args.putParcelable(KEY_GENRES, genreList);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if(bundle == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        GenreList genresList = bundle.getParcelable(KEY_GENRES);
        List<Genre> genres = genresList != null ? genresList.getGenres() : null;

        @SuppressLint("InflateParams") View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_genres, null);
        RecyclerView recyclerView = rootView.findViewById(R.id.genres_rv);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        GenresAdapter genresAdapter = new GenresAdapter(genres);
        genresAdapter.setClickListener(this);
        recyclerView.setAdapter(genresAdapter);

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK)
                .setView(rootView)
                .setTitle("Genres")
                .setNegativeButton(R.string.back, (dialog, id) -> {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        return alertDialog;
    }

    @Override
    public void onGenreClick(Genre genre) {
        if (mGenreSelector != null) mGenreSelector.onGenreSelected(genre);
        dismiss();
    }

    public void setGenreSelector(GenreSelector genreSelector) {
        this.mGenreSelector = genreSelector;
    }

    public interface GenreSelector{
        void onGenreSelected(Genre genre);
    }
}