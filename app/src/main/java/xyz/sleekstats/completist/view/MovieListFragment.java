package xyz.sleekstats.completist.view;

import android.content.Context;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.CastCredits;
import xyz.sleekstats.completist.model.MovieCredits;
import xyz.sleekstats.completist.service.Repo;
import xyz.sleekstats.completist.model.FilmByPerson;
import xyz.sleekstats.completist.model.PersonPOJO;

//Shows details of, and list of films by, a specific actor/director
public class MovieListFragment extends Fragment {

    private static final String ARG_ID = "id";
    private static final String TAG = MovieListFragment.class.getName();
    private static final String ARG_DIRECTOR = "director";
    private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w200/";

    private List<String> titles;
    private String mPersonId;
    private boolean mIsDirector;

    private TextView mNameView;
    private TextView mBioView;
    private ImageView mPosterView;


    private OnFragmentInteractionListener mListener;

    public MovieListFragment() {
    }

    public static MovieListFragment newInstance(String id, boolean isDirector) {
        MovieListFragment fragment = new MovieListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        args.putBoolean(ARG_DIRECTOR, isDirector);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPersonId = getArguments().getString(ARG_ID);
            mIsDirector = getArguments().getBoolean(ARG_DIRECTOR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        mNameView = rootView.findViewById(R.id.person_name);
        mBioView = rootView.findViewById(R.id.person_summary);
        mPosterView = rootView.findViewById(R.id.person_poster);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getFilmsForPerson(mPersonId);
    }

    public void getFilmsForPerson(String person_id) {

        Repo repo = new Repo();
        repo.getData();

        Observable<PersonPOJO> personPOJOObservable = repo.getFilmsByPerson(person_id);


        Observer<PersonPOJO> personPOJOObserver = new Observer<PersonPOJO>() {
            @Override
            public void onSubscribe(Disposable d) {
                if(titles == null) {
                    titles = new ArrayList<>();
                } else {
                    titles.clear();
                }
            }

            @Override
            public void onNext(PersonPOJO personPOJO) {
                List<FilmByPerson> filmByPersonList;
                MovieCredits movieCredits = personPOJO.getMovieCredits();

                //Determine if person's main role is director and then create list of roles
                mIsDirector = personPOJO.getKnown_for_department().equals("Directing");
                if(mIsDirector) {
                    filmByPersonList = new ArrayList<>();
                    List<FilmByPerson> crewList = movieCredits.getCrew();
                    //For director filter out roles besides directing
                    for (FilmByPerson film : crewList) {
                        if(film.getJob().equals("Director")) {
                            filmByPersonList.add(film);
                        }
                    }
                } else {
                    filmByPersonList = movieCredits.getCast();
                }
                String name = personPOJO.getName();
                String bio = personPOJO.getBiography();
                String posterUrl = POSTER_BASE_URL + personPOJO.getProfile_path();

                Picasso.get().load(posterUrl)
                        .placeholder(R.drawable.ic_sharp_account_box_92px)
                        .error(R.drawable.ic_sharp_account_box_92px)
                        .into(mPosterView);

                mNameView.setText(name);
                mBioView.setText(bio);
                Log.d("ddd", mBioView.getLineCount() + "");


                RecyclerView recyclerView = getView().findViewById(R.id.film_list);
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
                FilmsAdapter filmsAdapter = new FilmsAdapter(filmByPersonList);
                filmsAdapter.setClickListener(new FilmsAdapter.ItemClickListener() {
                    @Override
                    public void onFilmClick(String movieID) {
                        if (mListener != null) {
                            mListener.onFilmSelected(movieID);
                        }
                    }
                });
                recyclerView.setAdapter(filmsAdapter);
            }

            @Override
            public void onError(Throwable e) {
                Log.d("ddd", e.getMessage());
            }

            @Override
            public void onComplete() {
            }
        };
        personPOJOObservable.subscribe(personPOJOObserver);
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
    }

    public interface OnFragmentInteractionListener {
        void onFilmSelected(String movieID);
    }
}
