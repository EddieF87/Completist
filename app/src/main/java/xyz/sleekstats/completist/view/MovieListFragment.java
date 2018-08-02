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

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.service.Repo;
import xyz.sleekstats.completist.model.FilmByPerson;
import xyz.sleekstats.completist.model.PersonPOJO;

//Shows list of films by a specific actor/director
public class MovieListFragment extends Fragment {

    private static final String ARG_ID = "id";
    private static final String TAG = MovieListFragment.class.getName();
    private static final String ARG_DIRECTOR = "director";

    private List<String> titles;
    private String mPersonId;
    private boolean mIsDirector;

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
        return inflater.inflate(R.layout.fragment_movie_list, container, false);
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

                if(mIsDirector) {
                    filmByPersonList = personPOJO.getCrew();
                } else {
                    filmByPersonList = personPOJO.getCast();
                }

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
                Log.d(TAG, e.getMessage());
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
