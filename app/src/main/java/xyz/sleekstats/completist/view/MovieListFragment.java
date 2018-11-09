package xyz.sleekstats.completist.view;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.jakewharton.rxbinding2.widget.RxAdapterView;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.databinding.FragmentListBinding;
import xyz.sleekstats.completist.databinding.MovieKeys;
import xyz.sleekstats.completist.model.FilmByPerson;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.model.WatchCount;
import xyz.sleekstats.completist.viewmodel.MovieViewModel;

//Shows details of, and list of films by, a specific actor/director
public class MovieListFragment extends Fragment implements MovieAdapter.ItemClickListener {

    private static final String TAG_RXERROR = "rxprobMovieList";
    private int mGrids;

    private Spinner mRoleSpinner;
    private FloatingActionButton mListSaveButton;
    private RecyclerView mMoviesRecyclerView;
    private MovieAdapter mMovieAdapter;
    private List<FilmByPerson> mCurrentFilmList;

    private MovieViewModel movieViewModel;

    private final CompositeDisposable listCompositeDisposable = new CompositeDisposable();
    private FragmentListBinding fragmentListBinding;

    private PublishSubject<PersonPOJO> personPublishSubject;
    private PublishSubject<List<FilmByPerson>> filmListPublishSubject;
    private PublishSubject<WatchCount> watchCountPublishSubject;

    public MovieListFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (movieViewModel == null) {
            movieViewModel = ViewModelProviders.of(requireActivity()).get(MovieViewModel.class);
        }
        fragmentListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_list, container, false);

        mGrids = getResources().getInteger(R.integer.grid_number);
        View rootView = fragmentListBinding.getRoot();
        mMoviesRecyclerView = rootView.findViewById(R.id.film_list);
        mListSaveButton = rootView.findViewById(R.id.listSaveButton);
        mRoleSpinner = rootView.findViewById(R.id.role_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireActivity(),
                R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mRoleSpinner.setAdapter(adapter);
        listCompositeDisposable.add(RxAdapterView.itemSelections(mRoleSpinner)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .skip(1)
                        .doOnError(e -> Log.e(TAG_RXERROR, "Spinner error: " + e.getMessage()))
                        .subscribe(pos -> movieViewModel.onSpin(pos))
        );

        mListSaveButton.setOnClickListener(view -> listCompositeDisposable.add(movieViewModel.addOrRemoveList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> setSaveButton(R.drawable.ic_add_black_24dp),
                        error -> Log.e(TAG_RXERROR, "addOrRemoveList" + error.getMessage()),
                        () -> setSaveButton(R.drawable.ic_done_green_24dp)
                )
        )
        );
        mMoviesRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), mGrids));
        return rootView;
    }

    private void setSaveButton(int drawable) {
        mListSaveButton.hide();
        mListSaveButton.setImageResource(drawable);
        mListSaveButton.show();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        personPublishSubject = movieViewModel.getPersonPublishSubject();
        filmListPublishSubject = movieViewModel.getFilmListPublishSubject();
        watchCountPublishSubject = movieViewModel.getWatchCountPublishSubject();

        listCompositeDisposable.add(personPublishSubject.subscribe(this::setPersonView));
        listCompositeDisposable.add(filmListPublishSubject.subscribe(this::setRecyclerView));
        listCompositeDisposable.add(watchCountPublishSubject.subscribe(count -> fragmentListBinding.setWatchCount(count)));

        movieViewModel.getFilms();
    }

    //Set display with info for selected actor/director
    private void setPersonView(PersonPOJO personPOJO) {

        fragmentListBinding.setPerson(personPOJO);
        setSpinner(personPOJO.getKnown_for_department());

        String id = personPOJO.getId();
        switch (id) {
            case MovieKeys.LIST_WATCHED:
            case MovieKeys.LIST_NOWPLAYING:
            case MovieKeys.LIST_POPULAR:
            case MovieKeys.LIST_QUEUED:
            case MovieKeys.LIST_TOPRATED:
                mListSaveButton.hide();
                break;
            default:
                listCompositeDisposable.add(movieViewModel.checkIfListExists(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(success -> setSaveButton(R.drawable.ic_done_green_24dp),
                                error -> setSaveButton(R.drawable.ic_add_black_24dp),
                                () -> setSaveButton(R.drawable.ic_add_black_24dp)
                        )
                );
        }
    }

    private void setSpinner(String knownFor) {

        if (mRoleSpinner == null) {
            return;
        }
        switch (knownFor) {
            case "Movies":
                mRoleSpinner.setVisibility(View.INVISIBLE);
                return;
            case "Acting":
                mRoleSpinner.setSelection(1);
                break;
            case "Directing":
            case "Writing":
            case "Screenplay":
                mRoleSpinner.setSelection(2);
                break;
            default:
                mRoleSpinner.setSelection(0);
        }
        mRoleSpinner.setVisibility(View.VISIBLE);
    }

    //Populate recyclerview with films from actor/director
    private void setRecyclerView(List<FilmByPerson> filmByPersonList) {

        mCurrentFilmList = filmByPersonList;

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
            mMovieAdapter = new MovieAdapter(mCurrentFilmList);
            mMovieAdapter.setClickListener(this);
        } else {
            mMovieAdapter.setCurrentMovieList(mCurrentFilmList);
        }
        mMoviesRecyclerView.setAdapter(mMovieAdapter);
    }

    @Override
    public void onFilmClick(String movieID) {
        movieViewModel.getMovieInfo(movieID);
    }

    @Override
    public void onFilmWatched(int pos) {

        FilmByPerson film = mCurrentFilmList.get(pos);

        listCompositeDisposable.add(movieViewModel.onMovieWatched(film)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> setItemWatched(film, film.isWatched(), pos),
                        error -> setItemWatched(film, film.isWatched(), pos)
                )
        );
    }

    @Override
    public void onFilmQueued(int pos) {

        FilmByPerson film = mCurrentFilmList.get(pos);

        listCompositeDisposable.add(movieViewModel.onMovieQueued(film)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> setItemQueued(film, film.isQueued(), pos),
                        error -> setItemQueued(film, film.isQueued(), pos)
                )
        );
    }

    private void setItemWatched(FilmByPerson film, boolean watched, int pos){
        if (mMovieAdapter == null) {
            return;
        }
        film.setWatched(watched);
        mMovieAdapter.notifyItemChanged(pos);
    }

    private void setItemQueued(FilmByPerson film, boolean queued, int pos){
        if (mMovieAdapter == null) {
            return;
        }
        film.setQueued(queued);
        mMovieAdapter.notifyItemChanged(pos);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listCompositeDisposable.clear();
    }
}
