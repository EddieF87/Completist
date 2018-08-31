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
import io.reactivex.disposables.Disposable;
import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.FilmByPerson;
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

    private TextView mNameView;
    private TextView mBioView;
    private ImageView mPosterView;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private RecyclerView mMoviesRecyclerView;
    private MovieAdapter mMovieAdapter;
    private List<MyMovie> mCurrentFilmList;
    private MovieDao mMovieDao;

    private MovieViewModel movieViewModel;
    private Disposable mPersonDisposable;
    private Disposable mFilmsByPersonDisposable;
    private OnFragmentInteractionListener mListener;

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
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        mNameView = rootView.findViewById(R.id.person_name);
        mBioView = rootView.findViewById(R.id.person_summary);
        mPosterView = rootView.findViewById(R.id.person_poster);
        mMoviesRecyclerView = rootView.findViewById(R.id.film_list);
        mCollapsingToolbarLayout = rootView.findViewById(R.id.collapsing_toolbar);
        mMoviesRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
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
        mPersonDisposable = personObservable.subscribe(this::setViews);
        mFilmsByPersonDisposable = filmRVObservable.subscribe(this::setRecyclerView);
    }

    //Set display with info for selected actor/director
    private void setViews(PersonPOJO personPOJO) {
        String name = personPOJO.getName();
        String bio = personPOJO.getBiography();
        String known_for_department = personPOJO.getKnown_for_department();
        String posterUrl = POSTER_BASE_URL + personPOJO.getProfile_path();
        String title = name + " (" + known_for_department + ")";

        Picasso.get().load(posterUrl)
                .placeholder(R.drawable.ic_sharp_account_box_92px)
                .error(R.drawable.ic_sharp_account_box_92px)
                .into(mPosterView);

        mCollapsingToolbarLayout.setTitle(title);
        mNameView.setText(name);
        mBioView.setText(bio);
    }

    //Populate recyclerview with films from actor/director
    private void setRecyclerView(List<FilmByPerson> filmByPersonList) {
        mCurrentFilmList = new ArrayList<>();

        for (FilmByPerson film: filmByPersonList) {
            mCurrentFilmList.add(new MyMovie(Integer.parseInt(film.getId()), film.getTitle(), 0, 0, film.getPoster_path()));
        }
        if (mMoviesRecyclerView == null) {
            View rootView = getView();
            if (rootView == null) {
                return;
            }
            mMoviesRecyclerView = rootView.findViewById(R.id.film_list);
            mMoviesRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        }
        if (mMovieAdapter == null) {
            mMovieAdapter = new MovieAdapter(mCurrentFilmList, getActivity());
            mMovieAdapter.setClickListener(this);
        } else {
            mMovieAdapter.setCurrentMovieList(mCurrentFilmList);
        }
        mMoviesRecyclerView.setAdapter(mMovieAdapter);
        if(mMovieDao == null) {
            MovieRoomDB db = MovieRoomDB.getDatabase(getActivity().getApplication());
            this.mMovieDao = db.movieDao();
        }
        new checkExistenceAsyncTask2(mMovieDao).execute(mCurrentFilmList);
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
        if (mPersonDisposable != null && !mPersonDisposable.isDisposed()) {
            mPersonDisposable.dispose();
        }
        if (mFilmsByPersonDisposable != null && !mFilmsByPersonDisposable.isDisposed()) {
            mFilmsByPersonDisposable.dispose();
        }
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
        if(mMovieDao == null) {
            MovieRoomDB db = MovieRoomDB.getDatabase(getActivity().getApplication());
            this.mMovieDao = db.movieDao();
        }
        MyMovie film = mCurrentFilmList.get(pos);
        film.setWatchType(watchType);
        new checkExistenceAsyncTask1(mMovieDao).execute(film);
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


    private class checkExistenceAsyncTask1 extends android.os.AsyncTask<MyMovie, Void, List<MyMovie>> {

        private final MovieDao mAsyncTaskDao;
        private MyMovie myMovie;

        checkExistenceAsyncTask1(MovieDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<MyMovie> doInBackground(final MyMovie... params) {
            myMovie = params[0];
            return mAsyncTaskDao.checkIfExists(String.valueOf(myMovie.getMovie_id()));
        }

        @Override
        protected void onPostExecute(List<MyMovie> list) {
            if(list == null || list.isEmpty()) {
                new insertAsyncTask(mMovieDao).execute(myMovie);
            } else {
                new deleteAsyncTask(mMovieDao).execute(myMovie.getMovie_id());
            }
        }
    }


    private class checkExistenceAsyncTask2 extends android.os.AsyncTask<List<MyMovie>, Void, List<MyMovie>> {

        private final MovieDao mAsyncTaskDao;

        checkExistenceAsyncTask2(MovieDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<MyMovie> doInBackground(List<MyMovie>... lists) {
            List<String> ids = new ArrayList<>();
            for(MyMovie movie : lists[0]){
                ids.add(String.valueOf(movie.getMovie_id()));
            }
            return mAsyncTaskDao.checkIfListExists(ids);
        }

        @Override
        protected void onPostExecute(List<MyMovie> list) {
            updateWatched(list);
        }
    }

    private void updateWatched(List<MyMovie> watchedFilms) {
        NumberFormat f = new DecimalFormat("00");
        int numberOfMovies = mCurrentFilmList.size();
        int numberSeen = 0;

        for(MyMovie myMovie : watchedFilms) {
            MyMovie listMovie = findFilmInList(myMovie.getMovie_id());
            if(listMovie!= null) {
                listMovie.setWatchType(2);
                numberSeen++;
            }
        }
        int watchedPct = (numberSeen*100)/numberOfMovies;
        TextView watchedTracker = getView().findViewById(R.id.watched_tracker);
        String watchedNumbers = numberSeen + "/" + numberOfMovies + "  (" + f.format(watchedPct) + "%)";
        String watchedText = "Watched: " + watchedNumbers;
        watchedTracker.setText(watchedText);

        String title = mCollapsingToolbarLayout.getTitle().toString();
        title += "   " + watchedNumbers;
        mCollapsingToolbarLayout.setTitle(title);

        mMovieAdapter.notifyDataSetChanged();
    }

    private MyMovie findFilmInList(int id) {
        for (MyMovie movie : mCurrentFilmList) {
            if(id == movie.getMovie_id()) {
                return movie;
            }
        }
        return null;
    }

    private static class insertAsyncTask extends android.os.AsyncTask<MyMovie, Void, Void> {

        private final MovieDao mAsyncTaskDao;

        insertAsyncTask(MovieDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final MyMovie... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }


    private static class deleteAsyncTask extends android.os.AsyncTask<Integer, Void, Void> {

        private final MovieDao mAsyncTaskDao;

        deleteAsyncTask(MovieDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Integer... ints) {
            mAsyncTaskDao.removeMovie(String.valueOf(ints[0]));
            return null;
        }
    }
}
