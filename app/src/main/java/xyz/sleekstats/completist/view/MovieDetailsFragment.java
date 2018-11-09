package xyz.sleekstats.completist.view;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
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
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.databinding.FragmentMovieBinding;
import xyz.sleekstats.completist.model.CastCredits;
import xyz.sleekstats.completist.model.CastInfo;
import xyz.sleekstats.completist.model.FilmByPerson;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.viewmodel.MovieViewModel;

//Shows details for selected film, including director/cast, rating, and summary
public class MovieDetailsFragment extends Fragment implements CastAdapter.ItemClickListener {

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

        if (movieViewModel == null) {
            movieViewModel = ViewModelProviders.of(requireActivity()).get(MovieViewModel.class);
        }
        filmDetailsSubject = movieViewModel.getFilmDetailsPublishSubject();
        listCompositeDisposable.add(
                filmDetailsSubject
                        .doOnError(e -> Log.e(TAG_RXERROR, "e = " + e.getMessage()))
                        .subscribe(this::setMovieInfoDisplay)
        );
        View rootView = movieBinding.getRoot();
        mCastView = rootView.findViewById(R.id.cast_recyclerview);
        ImageView mWatchBtn = rootView.findViewById(R.id.details_watched_btn);
        ImageView mQueueBtn = rootView.findViewById(R.id.details_queue_btn);
        mWatchBtn.setOnClickListener(view ->
                listCompositeDisposable.add(
                        movieViewModel.onMovieWatched(new FilmByPerson(mFilm.getTitle(), mFilm.getId(), mFilm.getPoster_path()))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError(e -> Log.e(TAG_RXERROR, "e = " + e.getMessage()))
                                .subscribe(
                                        success -> setDisplay(false),
                                        error -> setDisplay(true)
                                )
                )
        );
        mQueueBtn.setOnClickListener(view -> movieViewModel.moveViewPager(1));

        mCastView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
//        setRetainInstance(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        movieViewModel.getShowOrFilm();
    }

    //Set display of movie details
    private void setMovieInfoDisplay(FilmPOJO filmPOJO) {
        mFilm = filmPOJO;
        checkFilm();
        setCastRecyclerView(filmPOJO.getCastCredits());
    }

    //Populate recyclerview of cast names and images
    private void setCastRecyclerView(CastCredits castCredits) {

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
        } else {
            mCastAdapter.setCastInfoList(castInfos);
        }
        mCastView.setAdapter(mCastAdapter);
    }

    private CastInfo getDirector(List<CastInfo> crewInfo) {
        for (CastInfo castInfo : crewInfo) {
            if (castInfo.getJob().equals("Director")) {
                return castInfo;
            }
        }
        return null;
    }

    private void checkFilm() {
        if (mFilm == null) {
            return;
        }
        listCompositeDisposable.add(
                movieViewModel.checkForMovie(new FilmByPerson(mFilm.getTitle(), mFilm.getId(), mFilm.getPoster_path()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(e -> Log.e(TAG_RXERROR, "e = " + e.getMessage()))
                        .subscribe(
                                success -> setDisplay(true),
                                error -> setDisplay(false)
                        )
        );
    }

    private void setDisplay(boolean watched) {
        if (mFilm == null) {
            return;
        }
        mFilm.setWatched(watched);
        movieBinding.setFilm(mFilm);
    }

    @Override
    public void onCastClick(String castID) {
        movieViewModel.updateFilms(castID);
    }
}
