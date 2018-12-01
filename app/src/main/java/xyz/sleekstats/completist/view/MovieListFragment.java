package xyz.sleekstats.completist.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxAdapterView;

import java.util.List;
import java.util.Objects;

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
public class MovieListFragment extends Fragment implements MovieAdapter.ItemClickListener
        , View.OnClickListener {

    private static final String TAG_RXERROR = "rxprobMovieList";

    private Spinner mRoleSpinner;
    private FloatingActionButton mListSaveButton;
    private RecyclerView mMoviesRecyclerView;
    private MovieAdapter mMovieAdapter;
    private List<FilmByPerson> mCurrentFilmList;
    private TextView summaryTextView;
    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (!recyclerView.canScrollVertically(1)) {
                Log.d("pokemo", "!recyclerView.canScrollVertically");
                movieViewModel.onScrollEnd();
            }
        }
    };

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
        fragmentListBinding.setListClick(this);

        View rootView = fragmentListBinding.getRoot();
        initRecyclerView(rootView);
        mListSaveButton = rootView.findViewById(R.id.listSaveButton);
        mRoleSpinner = rootView.findViewById(R.id.role_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireActivity(),
                R.array.list_options_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);

        mRoleSpinner.setAdapter(adapter);
        listCompositeDisposable.add(RxAdapterView.itemSelections(mRoleSpinner)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
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
        return rootView;
    }

    private void setSaveButton(int drawable) {
        mListSaveButton.hide();
        mListSaveButton.setImageResource(drawable);
        mListSaveButton.show();
        TextView textView = Objects.requireNonNull(getView()).findViewById(R.id.person_name);
        if (drawable == R.drawable.ic_done_green_24dp) {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done_green_24dp, 0);
            setForeground(R.drawable.poster_border_watched);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            setForeground(R.drawable.poster_border);
        }
    }

    private void setForeground(int drawableID) {
        if (Build.VERSION.SDK_INT > 23) {
            ImageView posterView = Objects.requireNonNull(getView()).findViewById(R.id.person_poster);
            posterView.setForeground(ContextCompat.getDrawable(requireActivity(), drawableID));
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
                e -> {
                    movieViewModel.finishScrollLoading();
                    Log.e(TAG_RXERROR, "filmListPublishSubject e=" + e.getMessage());
                }));
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
            case MovieKeys.LIST_POPULAR:
            case MovieKeys.LIST_NOWPLAYING:
            case MovieKeys.LIST_TOPRATED:
            case MovieKeys.LIST_GENRE:
                addScrollListener();
                mListSaveButton.hide();
                break;
            case MovieKeys.LIST_WATCHED:
            case MovieKeys.LIST_QUEUED:
                mListSaveButton.hide();
                removeScrollListener();
                break;
            default:
                removeScrollListener();
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

        int oldPos = mRoleSpinner.getSelectedItemPosition();
        int currentPos;
        movieViewModel.setInitialSpin(true);

        if (mRoleSpinner == null) {
            return;
        }
        switch (knownFor) {
            case "Movies":
                mRoleSpinner.setVisibility(View.GONE);
                return;
            case "Acting":
                currentPos = 1;
                break;
            case "Directing":
            case "Writing":
            case "Screenplay":
                currentPos = 2;
                break;
            default:
                currentPos = 0;
        }
        mRoleSpinner.setVisibility(View.VISIBLE);
        if (currentPos == oldPos) {
            movieViewModel.setInitialSpin(false);
            return;
        }
        mRoleSpinner.setSelection(currentPos);
    }

    private void initRecyclerView(View rootView) {
        mMoviesRecyclerView = rootView.findViewById(R.id.film_list);
        int grids = getResources().getInteger(R.integer.grid_number);
        mMoviesRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), grids));
    }

    private void addScrollListener(){
        if (mMoviesRecyclerView == null) {
            View rootView = getView();
            if (rootView == null) { return; }
            initRecyclerView(rootView);
        }
        mMoviesRecyclerView.addOnScrollListener(onScrollListener);
    }


    private void removeScrollListener(){
        if (mMoviesRecyclerView == null) {
            View rootView = getView();
            if (rootView == null) { return; }
            initRecyclerView(rootView);
        }
        mMoviesRecyclerView.removeOnScrollListener(onScrollListener);
    }

    //Populate recyclerview with films from actor/director
    private void setRecyclerView(List<FilmByPerson> filmByPersonList) {

        mCurrentFilmList = filmByPersonList;
        if (mMoviesRecyclerView == null) {
            View rootView = getView();
            if (rootView == null) { return; }
            initRecyclerView(rootView);
        }
        if (mMovieAdapter == null) {
            mMovieAdapter = new MovieAdapter(mCurrentFilmList);
            mMovieAdapter.setClickListener(this);
            mMoviesRecyclerView.setAdapter(mMovieAdapter);
        } else {
            mMovieAdapter.setCurrentMovieList(mCurrentFilmList);
            mMovieAdapter.notifyDataSetChanged();
        }
        movieViewModel.finishScrollLoading();
    }

    @Override
    public void onFilmClick(String movieID, boolean isFilm) {
        if (isFilm) {
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

    private void setItemWatched(FilmByPerson film, boolean watched, int pos) {
        if (mMovieAdapter == null) {
            return;
        }
        film.setWatched(watched);
        mMovieAdapter.notifyItemChanged(pos);
    }

    private void setItemQueued(FilmByPerson film, boolean queued, int pos) {
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
        summaryTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (summaryTextView.getLineCount() > lines) {
                    summaryTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    setReadMore(summaryTextView.getText().toString());
                }
                summaryTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void setReadMore(String text) {
        int lines = getResources().getInteger(R.integer.lines);
        int end = (lines * (summaryTextView.getOffsetForPosition(summaryTextView.getWidth(), 0)));
        end = Math.min(end, text.length());

        String readMoreText = "... (Read More)";
        int readMoreLength = readMoreText.length();
        if (end > readMoreLength) {
            String displayed = (text.substring(0, end - readMoreLength) + readMoreText).replaceAll("\n", " ");
            SpannableString ss = new SpannableString(displayed);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    openSummaryDialog(text);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
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

    private void openSummaryDialog(String text) {
        FragmentManager fragmentManager = (requireActivity()).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DialogFragment newFragment = PersonSummaryDialog.newInstance(text);
        newFragment.show(fragmentTransaction, "");
    }

    @Override
    public void onClick(View view) {
        try {
            String id = view.getTag(R.id.id).toString();
            if(view.getId() == R.id.tmdb_view) {
                goToSite("https://www.themoviedb.org/person/" + id);
            } else if (view.getId() == R.id.imdb_view) {
                goToSite("https://www.imdb.com/name/" + id);
            }
        } catch (Exception ignored) { }
    }

    private void goToSite(String site) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(site));
        startActivity(browserIntent);
    }
}
