package xyz.sleekstats.completist.view;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

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
    private TextView summaryTextView;

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
                R.array.list_options_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mRoleSpinner.setAdapter(adapter);
        listCompositeDisposable.add(RxAdapterView.itemSelections(mRoleSpinner)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .skip(1)
                        .subscribe(pos -> movieViewModel.onSpin(pos),
                                e -> Log.e(TAG_RXERROR, "Spinner error: " + e.getMessage())
                        )
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
        summaryTextView = rootView.findViewById(R.id.person_summary);
        mMoviesRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), mGrids));
        return rootView;
    }

    private void setSaveButton(int drawable) {
        mListSaveButton.hide();
        mListSaveButton.setImageResource(drawable);
        mListSaveButton.show();
        if(drawable == R.drawable.ic_done_green_24dp) {
            getView().findViewById(R.id.saved_note).setVisibility(View.VISIBLE);
        } else {
            getView().findViewById(R.id.saved_note).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        personPublishSubject = movieViewModel.getPersonPublishSubject();
        filmListPublishSubject = movieViewModel.getFilmListPublishSubject();
        watchCountPublishSubject = movieViewModel.getWatchCountPublishSubject();

        listCompositeDisposable.add(personPublishSubject.subscribe(this::setPersonView,
                e -> Log.e(TAG_RXERROR, "personPublishSubject e=" + e.getMessage())));
        listCompositeDisposable.add(filmListPublishSubject.subscribe(this::setRecyclerView,
                e -> Log.e(TAG_RXERROR, "filmListPublishSubject e=" + e.getMessage())));
        listCompositeDisposable.add(watchCountPublishSubject.subscribe(count -> fragmentListBinding.setWatchCount(count),
                e -> Log.e(TAG_RXERROR, "watchCountPublishSubject e=" + e.getMessage())));

        movieViewModel.getFilms();
    }

    //Set display with info for selected actor/director
    private void setPersonView(PersonPOJO personPOJO) {

        fragmentListBinding.setPerson(personPOJO);
        setSummaryText(personPOJO.getBiography());
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
            mMoviesRecyclerView.setAdapter(mMovieAdapter);
        } else {
            mMovieAdapter.setCurrentMovieList(mCurrentFilmList);
            mMovieAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFilmClick(String movieID, boolean isFilm) {
        if(isFilm) {
            movieViewModel.getMovieInfo(movieID);
        } else {
            movieViewModel.getShowInfo(movieID);
        }
    }

    @Override
    public void onFilmWatched(int pos) {

        FilmByPerson film = mCurrentFilmList.get(pos);

        listCompositeDisposable.add(movieViewModel.onMovieWatchedFromList(film)
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

        listCompositeDisposable.add(movieViewModel.onMovieQueuedFromList(film)
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

    private void setSummaryText(String text) {

        int lines = getResources().getInteger(R.integer.lines);
        summaryTextView.setText(text);

        if (summaryTextView.getLineCount() > lines) {

            int end = (lines * (summaryTextView.getOffsetForPosition(summaryTextView.getWidth(), 0) + 1));
            end = Math.min(end, text.length());

            String readMoreText = "... (Read More)";
            int readMoreLength = readMoreText.length();
            if(end > readMoreLength) {
                String displayed = text.substring(0, end-readMoreLength) + readMoreText;
                SpannableString ss = new SpannableString(displayed);
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        openSummaryDialog(text);
                    }
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }
                };
                ss.setSpan(clickableSpan, (displayed.length() - readMoreLength + 4), displayed.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                summaryTextView.setText(ss);
                summaryTextView.setMovementMethod(LinkMovementMethod.getInstance());
                summaryTextView.setHighlightColor(Color.TRANSPARENT);
            }
        }
    }

    private void openSummaryDialog(String text){
        FragmentManager fragmentManager = (requireActivity()).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = PersonSummaryDialog.newInstance(text);
        newFragment.show(fragmentTransaction, "");
    }
}
