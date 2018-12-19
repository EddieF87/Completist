package xyz.sleekstats.completist.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;
import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.databinding.FragmentMovieBinding;
import xyz.sleekstats.completist.model.CastCredits;
import xyz.sleekstats.completist.model.CastInfo;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.model.Genre;
import xyz.sleekstats.completist.viewmodel.MovieViewModel;

//Shows details for selected film, including director/cast, rating, and summary
public class MovieDetailsFragment extends Fragment
        implements CastAdapter.ItemClickListener
        , View.OnClickListener {

    private static final String TAG_RXERROR = "rxprobMovieDetails";

    private FilmPOJO mFilm;
    private RecyclerView mCastView;
    private CastAdapter mCastAdapter;

    private MovieViewModel movieViewModel;

    private PublishSubject<FilmPOJO> filmDetailsSubject;

    private FragmentMovieBinding movieBinding;
    private final CompositeDisposable listCompositeDisposable = new CompositeDisposable();

    public MovieDetailsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        movieBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_movie, container, false);
        movieBinding.setDetailsClick(this);

        if (movieViewModel == null) {
            movieViewModel = ViewModelProviders.of(requireActivity()).get(MovieViewModel.class);
        }
        filmDetailsSubject = movieViewModel.getFilmDetailsPublishSubject();
        listCompositeDisposable.add(
                filmDetailsSubject
                        .subscribe(this::setMovieInfoDisplay,
                                e -> Log.e(TAG_RXERROR, " getFilmDetailsPublishSubject e = " + e.getMessage())
                        )
        );
        View rootView = movieBinding.getRoot();

        mCastView = rootView.findViewById(R.id.cast_recyclerview);
        mCastView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        rootView.findViewById(R.id.details_watched_btn).setOnClickListener(view ->
                setMovieInfoDisplay(movieViewModel.onMovieWatchedFromDetails(mFilm)));
        rootView.findViewById(R.id.details_queue_btn).setOnClickListener(view ->
                setMovieInfoDisplay(movieViewModel.onMovieQueuedFromDetails(mFilm)));
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        movieViewModel.getShowOrFilm();
    }

    //Set display of movie details
    private void setMovieInfoDisplay(FilmPOJO filmPOJO) {

        if (filmPOJO == null) {
            return;
        }
        mFilm = filmPOJO;
        movieBinding.setFilm(mFilm);
        setCastRecyclerView(filmPOJO.getCastCredits());
    }

    //Populate recyclerview of cast names and images
    private void setCastRecyclerView(CastCredits castCredits) {

        if (castCredits == null) {
            return;
        }
        List<CastInfo> castInfos = new ArrayList<>(castCredits.getCast());
        List<CastInfo> crewInfos = new ArrayList<>(castCredits.getCrew());

        CastInfo directorInfo = getDirector(crewInfos);
        if (directorInfo != null) {
            castInfos.add(0, directorInfo);
        }

        if (mCastView == null) {
            View rootView = getView();
            if (rootView == null) {
                return;
            }
            mCastView = rootView.findViewById(R.id.cast_recyclerview);
            mCastView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        }
        if (mCastAdapter == null) {
            mCastAdapter = new CastAdapter(castInfos);
            mCastAdapter.setClickListener(castID -> movieViewModel.updateFilms(castID));
            mCastView.setAdapter(mCastAdapter);
        } else {
            mCastAdapter.setCastInfoList(castInfos);
            mCastAdapter.notifyDataSetChanged();
        }
    }

    private CastInfo getDirector(List<CastInfo> crewInfo) {
        for (CastInfo castInfo : crewInfo) {
            if (castInfo.getJob().equals("Director")) {
                return castInfo;
            }
        }
        return null;
    }

    @Override
    public void onCastClick(String castID) {
        movieViewModel.updateFilms(castID);
    }

    @Override
    public void onClick(View view) {
        String id;

        switch (view.getId()) {
            case R.id.movie_genre:
                if (view.getTag() == null) {
                    return;
                }
                Genre genre = (Genre) view.getTag();
                movieViewModel.getFilmsByGenre(genre, mFilm.isFilm());
                break;

            case R.id.tmdb_view:
                id = view.getTag(R.id.id).toString();
                String tvOrMovieString = (boolean) view.getTag(R.id.tvOrMovie) ? "movie" : "tv";
                goToSite("https://www.themoviedb.org/" + tvOrMovieString + "/" + id);
                break;

            case R.id.imdb_view:
                boolean tvOrMovie = (boolean) view.getTag(R.id.tvOrMovie);
                if (!tvOrMovie) {
                    return;
                }
                id = view.getTag(R.id.id).toString();
                goToSite("https://www.imdb.com/title/" + id);
                break;

            case R.id.similar_view:
                if (view.getTag() == null) {
                    return;
                }
                String filmID = (String) view.getTag();
                if(mFilm.isFilm()) {
                    movieViewModel.getMovieInfo(filmID);
                } else {
                    movieViewModel.getShowInfo(filmID);
                }
                break;
        }
    }

    private void goToSite(String site) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(site));
        startActivity(browserIntent);
    }
}
