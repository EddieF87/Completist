package xyz.sleekstats.completist.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.service.MovieDao;
import xyz.sleekstats.completist.model.MovieRoomDB;
import xyz.sleekstats.completist.model.MyList;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.viewmodel.MovieViewModel;

public class MyListsFragment extends Fragment implements MyListsAdapter.ItemClickListener {

    private RecyclerView mPopularListRV;
    private RecyclerView mSavedListRV;
    private MyListsAdapter mPopularListAdapter;
    private MyListsAdapter mSavedListAdapter;
    private final CompositeDisposable myListsCompositeDisposable = new CompositeDisposable();
    private MovieViewModel movieViewModel;
    private OnFragmentInteractionListener mListener;
    private MovieDao mMovieDao;

    public MyListsFragment() {
        // Required empty public constructor
    }

    public static MyListsFragment newInstance() {
        MyListsFragment fragment = new MyListsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_lists, container, false);

        Button popularMoviesButton =  view.findViewById(R.id.popular_movies_btn);
        popularMoviesButton.setOnClickListener(btn -> onListClick(""));

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
                            for(PersonPOJO personPOJO : personPOJOList) {
                                myLists.add(new MyList(Integer.parseInt(personPOJO.getId()),
                                        personPOJO.getName(), -1, personPOJO.getProfile_path()));
                            }
                            return Observable.just(myLists);
                        }
                )
                .subscribe(this::loadPopularRV,
                        e -> Log.e("rxprob", "queryPopular loadPopularRV" + e.getMessage())));
        if (mMovieDao == null) {
            MovieRoomDB db = MovieRoomDB.getDatabase(getActivity().getApplication());
            this.mMovieDao = db.movieDao();
        }
        myListsCompositeDisposable.add(mMovieDao.getSavedLists()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::loadSavedRV,
                        e -> Log.e("rxprob", "getSavedLists loadSavedRV" + e.getMessage())));
    }

    private void loadRV(List<MyList> myLists, RecyclerView rv, MyListsAdapter myListsAdapter) {

        if (myListsAdapter == null) {
            myListsAdapter = new MyListsAdapter(myLists);
            myListsAdapter.setClickListener(this);
        }
        rv.setAdapter(myListsAdapter);
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
        myListsCompositeDisposable.clear();
    }

    @Override
    public void onListClick(String listID) {
        if (mListener != null) {
            mListener.onCastSelected(listID);
        }
    }

    public interface OnFragmentInteractionListener {
        void onCastSelected(String castID);
    }
}
