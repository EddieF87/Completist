package xyz.sleekstats.completist.view;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.databinding.MovieKeys;
import xyz.sleekstats.completist.model.Genre;
import xyz.sleekstats.completist.model.GenreList;
import xyz.sleekstats.completist.model.MyList;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.viewmodel.MovieViewModel;

public class MyListsFragment extends Fragment
        implements MyListsAdapter.ItemClickListener, GenresDialog.GenreSelector {

    private RecyclerView mPopularListRV;
    private RecyclerView mSavedListRV;
    private MyListsAdapter mPopularListAdapter;
    private MyListsAdapter mSavedListAdapter;
    private final CompositeDisposable myListsCompositeDisposable = new CompositeDisposable();
    private MovieViewModel movieViewModel;
    private RadioGroup radioGroup1;
    private RadioGroup radioGroup2;

    public MyListsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_lists, container, false);

        radioGroup1 = view.findViewById(R.id.radio_group1);
        radioGroup2 = view.findViewById(R.id.radio_group2);
        view.findViewById(R.id.watched_movies_btn).setOnClickListener(btn -> onListClick(MovieKeys.LIST_WATCHED));
        view.findViewById(R.id.popular_movies_btn).setOnClickListener(btn -> onListClick(MovieKeys.LIST_POPULAR));
        view.findViewById(R.id.genres_btn).setOnClickListener(btn -> onGenreClick());
        view.findViewById(R.id.nowshowing_movies_btn).setOnClickListener(btn -> onListClick(MovieKeys.LIST_NOWPLAYING));
        view.findViewById(R.id.top_movies_btn).setOnClickListener(btn -> onListClick(MovieKeys.LIST_TOPRATED));
        view.findViewById(R.id.scheduled_btn).setOnClickListener(btn -> onListClick(MovieKeys.LIST_QUEUED));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (movieViewModel == null) {
            movieViewModel = ViewModelProviders.of(requireActivity()).get(MovieViewModel.class);
        }
        myListsCompositeDisposable.add(movieViewModel.queryPopular()
                .flatMap(personQueryPOJO -> {
                            List<PersonPOJO> personPOJOList = personQueryPOJO.getResults();
                            List<MyList> myLists = new ArrayList<>();
                            for (PersonPOJO personPOJO : personPOJOList) {
                                myLists.add(new MyList(Integer.parseInt(personPOJO.getId()),
                                        personPOJO.getName(), -1, 1, personPOJO.getProfile_path()));
                            }
                            return Observable.just(myLists);
                        }
                )
                .subscribe(this::loadPopularRV,
                        e -> Log.e("rxprob", "queryPopular loadPopularRV" + e.getMessage())));

        myListsCompositeDisposable.add(movieViewModel.getSavedLists()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::loadSavedRV,
                        e -> Log.e("rxprob", "getSavedLists loadSavedRV" + e.getMessage())));
    }

    private void loadRV(List<MyList> myLists, RecyclerView rv, MyListsAdapter myListsAdapter) {

        if (myListsAdapter == null) {
            myListsAdapter = new MyListsAdapter(myLists);
            myListsAdapter.setClickListener(this);
            rv.setAdapter(myListsAdapter);
        } else {
            myListsAdapter.notifyDataSetChanged();
        }
    }

    private void loadPopularRV(List<MyList> myLists) {

        if (mPopularListRV == null) {
            View rootView = getView();
            if (rootView == null) {
                return;
            }
            mPopularListRV = rootView.findViewById(R.id.pop_lists_rv);
            mPopularListRV.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        }
        loadRV(myLists, mPopularListRV, mPopularListAdapter);
    }

    private void loadSavedRV(List<MyList> myLists) {
        if (mSavedListRV == null) {
            View rootView = getView();
            if (rootView == null) {
                return;
            }
            mSavedListRV = rootView.findViewById(R.id.saved_lists_rv);
            mSavedListRV.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        }
        loadRV(myLists, mSavedListRV, mSavedListAdapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        myListsCompositeDisposable.clear();
    }

    private void onGenreClick() {
        clearRadioGroups();
        myListsCompositeDisposable.add(movieViewModel.getGenreList(true)
                .subscribe(this::openGenresDialog)
        );
    }

    private void openGenresDialog(GenreList genres){
        if(genres == null) { return;}
        FragmentManager fragmentManager = (requireActivity()).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        GenresDialog newFragment = GenresDialog.newInstance(genres);
        newFragment.show(fragmentTransaction, "");
        newFragment.setGenreSelector(this);
    }

    @Override
    public void onListClick(String listID) {
        clearRadioGroups();
        movieViewModel.updateFilms(listID);
    }

    private void clearRadioGroups() {
        if (radioGroup1 != null) {
            radioGroup1.clearCheck();
        }
        if (radioGroup2 != null) {
            radioGroup2.clearCheck();
        }
    }

    @Override
    public void onGenreSelected(Genre genre) {
        movieViewModel.getFilmsByGenre(genre, true);
    }
}
