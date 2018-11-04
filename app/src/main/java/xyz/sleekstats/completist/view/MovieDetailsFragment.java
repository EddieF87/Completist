package xyz.sleekstats.completist.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.databinding.FragmentMovieBinding;
import xyz.sleekstats.completist.model.CastCredits;
import xyz.sleekstats.completist.model.CastInfo;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.viewmodel.MovieViewModel;

//Shows details for selected film, including director/cast, rating, and summary
public class MovieDetailsFragment extends Fragment implements CastAdapter.ItemClickListener {

    private static final String ARG_ID = "id";
    private static final String KEY_ISTV = "istv";

    private boolean isTV;
    private String mMovieId;
    private RecyclerView mCastView;
    private CastAdapter mCastAdapter;

    private MovieViewModel movieViewModel;
    private Disposable mFilmDisposable;

    private OnFragmentInteractionListener mListener;
    private FragmentMovieBinding movieBinding;

    public MovieDetailsFragment() {
    }

    public static MovieDetailsFragment newInstance(String id) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovieId = getArguments().getString(ARG_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        movieBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_movie, container, false);
        View rootView = movieBinding.getRoot();
        mCastView = rootView.findViewById(R.id.cast_recyclerview);
        mCastView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
//        setRetainInstance(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mMovieId = savedInstanceState.getString("id", mMovieId);
            isTV = savedInstanceState.getBoolean(KEY_ISTV);
        }
        if(isTV) {
            getShow(mMovieId);
        } else {
            getFilm(mMovieId);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    //Retrieve film data from ViewModel
    public void getFilm(String movie_id) {
        isTV = false;
        mMovieId = movie_id;
        if (movieViewModel == null) {
            movieViewModel = ViewModelProviders.of(requireActivity()).get(MovieViewModel.class);
        }
        Observable<FilmPOJO> filmPOJOObservable = movieViewModel.getMovieInfo(movie_id);
        mFilmDisposable = filmPOJOObservable.subscribe(s -> {
                    setMovieInfoDisplay(s);
                    setCastRecyclerView(s.getCastCredits());
                },
                e -> Log.e("rxprob", "filmPOJOObservable getMovieInfo" + e.getMessage()));
    }

    //Retrieve tv show data from ViewModel
    public void getShow(String showID) {
        isTV = true;
        mMovieId = showID;
        if (movieViewModel == null) {
            movieViewModel = ViewModelProviders.of(requireActivity()).get(MovieViewModel.class);
        }
        Observable<FilmPOJO> filmPOJOObservable = movieViewModel.getShowInfo(showID);
        mFilmDisposable = filmPOJOObservable.subscribe(s -> {
                    String name = s.getName();
                    s.setTitle(name);
                    setMovieInfoDisplay(s);
                    setCastRecyclerView(s.getCastCredits());
                },
                e -> Log.e("rxprob", "filmPOJOObservable getShowInfo" + e.getMessage()));
    }

    //Set display of movie details
    private void setMovieInfoDisplay(FilmPOJO filmPOJO) {
        movieBinding.setFilm(filmPOJO);
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
            mCastAdapter.setClickListener(castID -> {
                if (mListener != null) {
                    mListener.onCastSelected(castID);
                }
            });
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mFilmDisposable != null && !mFilmDisposable.isDisposed()) {
            mFilmDisposable.dispose();
        }
    }

    @Override
    public void onCastClick(String castID) {
        if (mListener != null) {
            mListener.onCastSelected(castID);
        }
    }

    public interface OnFragmentInteractionListener {
        void onCastSelected(String castID);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("id", mMovieId);
        outState.putBoolean(KEY_ISTV, isTV);
    }

}
