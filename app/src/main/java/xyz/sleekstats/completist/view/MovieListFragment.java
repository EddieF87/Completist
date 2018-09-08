package xyz.sleekstats.completist.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.FilmByPerson;
import xyz.sleekstats.completist.model.FilmListDetails;
import xyz.sleekstats.completist.model.MovieDao;
import xyz.sleekstats.completist.model.MovieRoomDB;
import xyz.sleekstats.completist.model.MyMovie;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.viewmodel.MovieViewModel;

//Shows details of, and list of films by, a specific actor/director
public class MovieListFragment extends Fragment implements MovieAdapter.ItemClickListener {

    private static final String ARG_ID = "id";
    private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w200/";
    private String mPersonId;
    private String mPerson;
    private int mGrids;

    private TextView mNameView;
    private TextView mBioView;
    private ImageView mPosterView;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private RecyclerView mMoviesRecyclerView;
    private MovieAdapter mMovieAdapter;
    private List<MyMovie> mCurrentFilmList;
    private MovieDao mMovieDao;

    private MovieViewModel movieViewModel;
    private OnFragmentInteractionListener mListener;

    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private Disposable mDisposable;

    public MovieListFragment() {
    }

    public static MovieListFragment newInstance(String id) {
        MovieListFragment fragment = new MovieListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPersonId = getArguments().getString(ARG_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mGrids = getResources().getInteger(R.integer.grid_number);
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        mNameView = rootView.findViewById(R.id.person_name);
        mBioView = rootView.findViewById(R.id.person_summary);
        mPosterView = rootView.findViewById(R.id.person_poster);
        mMoviesRecyclerView = rootView.findViewById(R.id.film_list);
        mCollapsingToolbarLayout = rootView.findViewById(R.id.collapsing_toolbar);

        mMoviesRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), mGrids));
        setRetainInstance(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mPersonId = savedInstanceState.getString("id", mPersonId);
        }
        getFilmsForPerson(mPersonId);
    }

    //Retrieve person/film data from ViewModel
    public void getFilmsForPerson(String person_id) {
        if (mDisposable != null) {
            mDisposable.dispose();
        }

        mPersonId = person_id;
        if (movieViewModel == null) {
            movieViewModel = ViewModelProviders.of(requireActivity()).get(MovieViewModel.class);
        }

        Observable<PersonPOJO> personObservable = movieViewModel.getFilmsByPerson(person_id);

        Observable<List<FilmByPerson>> filmRVObservable = personObservable.map(s -> {
            if (s.getKnown_for_department().equals("Directing")) {
                return movieViewModel.filterCrew(s.getMovieCredits().getCrew());
            } else {
                return s.getMovieCredits().getCast();
            }
        });

        Observable<FilmListDetails> filmListDetailsObservable = Observable.zip(personObservable, filmRVObservable,
                FilmListDetails::new);
        mCompositeDisposable.add(filmListDetailsObservable.subscribe(this::setViews));
    }

    //Set display with info for selected actor/director
    private void setViews(FilmListDetails filmListDetails) {
        PersonPOJO personPOJO = filmListDetails.getPersonPOJO();
        List<FilmByPerson> filmByPersonList = filmListDetails.getFilmByPersonList();

        String name = personPOJO.getName();
        String bio = personPOJO.getBiography();
        String known_for_department = personPOJO.getKnown_for_department();
        String posterUrl = POSTER_BASE_URL + personPOJO.getProfile_path();
        mPerson = name + " (" + known_for_department + ")";

        Picasso.get().load(posterUrl)
                .placeholder(R.drawable.ic_sharp_account_box_92px)
                .error(R.drawable.ic_sharp_account_box_92px)
                .into(mPosterView);

//        mCollapsingToolbarLayout.setTitle(name);
        mNameView.setText(name);
        mBioView.setText(bio);

        setRecyclerView(filmByPersonList);
    }

    //Populate recyclerview with films from actor/director
    private void setRecyclerView(List<FilmByPerson> filmByPersonList) {
        mCurrentFilmList = new ArrayList<>();

        for (FilmByPerson film : filmByPersonList) {
            mCurrentFilmList.add(new MyMovie(Integer.parseInt(film.getId()), film.getTitle(), 0, 0, film.getPoster_path()));
        }
        if (mMoviesRecyclerView == null) {
            View rootView = getView();
            if (rootView == null) {
                return;
            }
            mMoviesRecyclerView = rootView.findViewById(R.id.film_list);
            mGrids = getResources().getInteger(R.integer.grid_number);
            mMoviesRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), mGrids));
        }
        if (mMovieAdapter == null) {
            mMovieAdapter = new MovieAdapter(mCurrentFilmList, getActivity());
            mMovieAdapter.setClickListener(this);
        } else {
            mMovieAdapter.setCurrentMovieList(mCurrentFilmList);
        }
        mMoviesRecyclerView.setAdapter(mMovieAdapter);
        if (mMovieDao == null) {
            MovieRoomDB db = MovieRoomDB.getDatabase(getActivity().getApplication());
            this.mMovieDao = db.movieDao();
        }

        List<String> ids = new ArrayList<>();
        for (MyMovie movie : mCurrentFilmList) {
            ids.add(String.valueOf(movie.getMovie_id()));
        }
        mDisposable = mMovieDao.checkIfListExists(ids).observeOn(AndroidSchedulers.mainThread()).subscribe(this::updateFilmsWatched);
        mCompositeDisposable.add(mDisposable);
    }

    @Override
    public void onFilmClick(String movieID) {
        if (mListener != null) {
            mListener.onFilmSelected(movieID);
        }
    }

    @Override
    public void onFilmChecked(int pos, int watchType) {
        if (mMovieAdapter == null) {
            return;
        }
        if (mMovieDao == null) {
            MovieRoomDB db = MovieRoomDB.getDatabase(getActivity().getApplication());
            this.mMovieDao = db.movieDao();
        }
        MyMovie film = mCurrentFilmList.get(pos);
        film.setWatchType(watchType);

        mCompositeDisposable.add(mMovieDao.checkIfMovieExists(String.valueOf(film.getMovie_id()))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        success -> mMovieDao.removeMovie(String.valueOf(film.getMovie_id())),
                        error -> mMovieDao.insert(film)
                )
        );
        mMovieAdapter.notifyItemChanged(pos);
    }

    public interface OnFragmentInteractionListener {
        void onFilmSelected(String movieID);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("id", mPersonId);
    }

    private void updateFilmsWatched(List<MyMovie> watchedFilms) {

        int mTotalMovies = mCurrentFilmList.size();
        int mSeenMovies = 0;

        for (MyMovie myMovie : watchedFilms) {
            MyMovie listMovie = findFilmInList(myMovie.getMovie_id());
            if (listMovie != null) {
                listMovie.setWatchType(2);
                mSeenMovies++;
            }
        }

        mMovieAdapter.notifyDataSetChanged();

        updateWatchedStatus(mSeenMovies, mTotalMovies);
    }

    private void updateWatchedStatus(int numberSeen, int numberOfMovies) {

        NumberFormat f = new DecimalFormat("00");

        int watchedPct = (numberSeen * 100) / numberOfMovies;
        TextView watchedTracker = getView().findViewById(R.id.watched_tracker);
        String watchedNumbers = numberSeen + "/" + numberOfMovies + "  (" + f.format(watchedPct) + "%)";
        String watchedText = "Watched: " + watchedNumbers;
        watchedTracker.setText(watchedText);

        String title = mPerson + "   " + watchedNumbers;
        mCollapsingToolbarLayout.setTitle(title);
    }

    private MyMovie findFilmInList(int id) {
        for (MyMovie movie : mCurrentFilmList) {
            if (id == movie.getMovie_id()) {
                return movie;
            }
        }
        return null;
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mCompositeDisposable.clear();
    }
}
