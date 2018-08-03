package xyz.sleekstats.completist.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import xyz.sleekstats.completist.model.FilmByPerson;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.viewmodel.MovieListViewModel;

//Shows details of, and list of films by, a specific actor/director
public class MovieListFragment extends Fragment {

    private static final String ARG_ID = "id";
    private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w200/";
    private String mPersonId;

    private TextView mNameView;
    private TextView mBioView;
    private ImageView mPosterView;
    private RecyclerView mMoviesRecyclerView;
    private MovieAdapter mMovieAdapter;

    private MovieListViewModel movieListViewModel;
    private Disposable mPersonDisposable;
    private Disposable mFilmsByPersonDisposable;
    private OnFragmentInteractionListener mListener;

    public MovieListFragment() { }

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
        mMoviesRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getFilmsForPerson(mPersonId);
    }

    //Retrieve person/film data from ViewModel
    private void getFilmsForPerson(String person_id) {

        if(movieListViewModel == null) {
            movieListViewModel = new MovieListViewModel(requireActivity().getApplication());
        }

        Observable<PersonPOJO> personObservable = movieListViewModel.getFilmsByPerson(person_id);

        Observable<List<FilmByPerson>> filmRVObservable = personObservable.map(s -> {
            if (s.getKnown_for_department().equals("Directing")) {
                return movieListViewModel.filterCrew(s.getMovieCredits().getCrew());
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
        String posterUrl = POSTER_BASE_URL + personPOJO.getProfile_path();

        Picasso.get().load(posterUrl)
                .placeholder(R.drawable.ic_sharp_account_box_92px)
                .error(R.drawable.ic_sharp_account_box_92px)
                .into(mPosterView);

        mNameView.setText(name);
        mBioView.setText(bio);
    }

    //Populate recyclerview with films from actor/director
    private void setRecyclerView(List<FilmByPerson> filmByPersonList) {
        if(mMoviesRecyclerView == null) {
            View rootView = getView();
            if(rootView == null){ return; }
            mMoviesRecyclerView = rootView.findViewById(R.id.film_list);
            mMoviesRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        }
        if(mMovieAdapter == null) {
            mMovieAdapter = new MovieAdapter(filmByPersonList);
            mMovieAdapter.setClickListener(movieID -> {
                if (mListener != null) {
                    mListener.onFilmSelected(movieID);
                }
            });
        }
        mMoviesRecyclerView.setAdapter(mMovieAdapter);
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
        if(mPersonDisposable != null && !mPersonDisposable.isDisposed()) {
            mPersonDisposable.dispose();
        }
        if(mFilmsByPersonDisposable != null && !mFilmsByPersonDisposable.isDisposed()) {
            mFilmsByPersonDisposable.dispose();
        }
    }

    public interface OnFragmentInteractionListener {
        void onFilmSelected(String movieID);
    }
}
