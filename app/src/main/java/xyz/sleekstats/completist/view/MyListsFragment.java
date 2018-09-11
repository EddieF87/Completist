package xyz.sleekstats.completist.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.PersonPOJO;
import xyz.sleekstats.completist.model.PersonQueryPOJO;
import xyz.sleekstats.completist.viewmodel.MovieViewModel;

public class MyListsFragment extends Fragment implements MyListsAdapter.ItemClickListener {

    private RecyclerView mPopularListRV;
    private MyListsAdapter mPopularListAdapter;
    private final CompositeDisposable myListsCompositeDisposable = new CompositeDisposable();
    private MovieViewModel movieViewModel;
    private OnFragmentInteractionListener mListener;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_lists, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (movieViewModel == null) {
            movieViewModel = ViewModelProviders.of(requireActivity()).get(MovieViewModel.class);
        }
        myListsCompositeDisposable.add(movieViewModel.queryPopular().subscribe(this::loadRVs));
    }

    private void loadRVs(PersonQueryPOJO personQueryPOJO){
        List<PersonPOJO> personPOJOList = personQueryPOJO.getResults();
        if(mPopularListRV == null) {
            View rootView = getView();
            if (rootView == null) {
                return;
            }
            mPopularListRV = rootView.findViewById(R.id.pop_lists_rv);
            mPopularListRV.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        }
        if(mPopularListAdapter == null){
            mPopularListAdapter = new MyListsAdapter(personPOJOList);
            mPopularListAdapter.setClickListener(this);
        }
        mPopularListRV.setAdapter(mPopularListAdapter);
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
