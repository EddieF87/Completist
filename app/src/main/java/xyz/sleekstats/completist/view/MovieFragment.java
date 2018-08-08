package xyz.sleekstats.completist.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.CastCredits;
import xyz.sleekstats.completist.model.CastInfo;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.model.Genre;
import xyz.sleekstats.completist.viewmodel.MovieViewModel;

//Shows details for selected film, including director/cast, rating, and summary
public class MovieFragment extends Fragment implements CastAdapter.ItemClickListener {

    private static final String ARG_ID = "id";
    private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500/";

    private String mMovieId;
    private TextView mTitleView;
    private TextView mOverviewView;
    private ImageView mPosterView;
    private TextView mGenreView;
    private RecyclerView mCastView;
    private CastAdapter mCastAdapter;

    private MovieViewModel movieViewModel;
    private Disposable mFilmDisposable;

    private OnFragmentInteractionListener mListener;

    public MovieFragment() {}

    public static MovieFragment newInstance(String id) {
        MovieFragment fragment = new MovieFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);
        mTitleView = rootView.findViewById(R.id.movie_title);
        mOverviewView = rootView.findViewById(R.id.movie_overview);
        mPosterView = rootView.findViewById(R.id.movie_poster);
        mGenreView = rootView.findViewById(R.id.movie_genre);
        mCastView = rootView.findViewById(R.id.cast_recyclerview);
        mCastView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        setRetainInstance(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            mMovieId = savedInstanceState.getString("id", mMovieId);
        }
        getFilm(mMovieId);
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
        mMovieId = movie_id;
        if(movieViewModel == null) {
            movieViewModel = ViewModelProviders.of(requireActivity()).get(MovieViewModel.class);
        }
        Observable<FilmPOJO> filmPOJOObservable = movieViewModel.getMovieInfo(movie_id);
        mFilmDisposable = filmPOJOObservable.subscribe(s -> {
                    setMovieInfoDisplay(s);
                    setCastRecyclerView(s.getCastCredits());
                }
        );
    }

    //Set display of movie details
    private void setMovieInfoDisplay(FilmPOJO filmPOJO) {
        String title = filmPOJO.getTitle();
        mTitleView.setText(title);

        String overview = filmPOJO.getOverview();
        if (overview != null) {
            mOverviewView.setText(overview);
        }

        String posterURL = POSTER_BASE_URL + filmPOJO.getPoster_path();
        Picasso.get().load(posterURL)
                .placeholder(R.drawable.ic_sharp_movie_92px)
                .error(R.drawable.ic_sharp_movie_92px)
                .into(mPosterView);

        List<Genre> genres = filmPOJO.getGenres();

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < genres.size(); i++) {
            String name = genres.get(i).getName();
            stringBuilder.append(name);
            if (i < genres.size() - 1) {
                stringBuilder.append("/ ");
            }
        }
        String genreString = stringBuilder.toString();
        mGenreView.setText(genreString);
    }

    //Populate recyclerview of cast names and images
    private void setCastRecyclerView(CastCredits castCredits) {

        List<CastInfo> castInfos = castCredits.getCast();
        List<CastInfo> crewInfos = castCredits.getCrew();
//                for(CastInfo castInfo : castInfos) {
//                }
        CastInfo directorInfo = getDirector(crewInfos);
        castInfos.add(0, directorInfo);

        if(mCastView == null) {
            View rootView = getView();
            if(rootView == null) { return; }
            mCastView = rootView.findViewById(R.id.cast_recyclerview);
            mCastView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        }
        if(mCastAdapter == null) {
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
        if(mFilmDisposable != null && !mFilmDisposable.isDisposed()) {
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
    }
}
