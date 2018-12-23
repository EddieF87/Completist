package xyz.sleekstats.completist.view;


import android.arch.lifecycle.ViewModelProviders;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;
import com.woxthebox.draglistview.BoardView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.MediaByPerson;
import xyz.sleekstats.completist.model.MediaPOJO;
import xyz.sleekstats.completist.viewmodel.MovieViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class RankingsFragment extends Fragment
        implements RankingsAdapter.ItemClickListener {

    private BoardView mBoardView;
    private boolean itemDragStarted;
    private MovieViewModel movieViewModel;
    private final CompositeDisposable listCompositeDisposable = new CompositeDisposable();
    private PublishSubject<List<MediaByPerson>> rankingsSubject;
    private RankingsAdapter mUnrankedAListdapter;
    private RankingsAdapter mRankedAListdapter;
    private SimpleCursorAdapter mSearchAdapter;
    private final List<MediaByPerson> rankedList = new ArrayList<>();


    private static final String SEARCH_TITLE = "title";
    private static final String SEARCH_ID = "search_id";
    private static final String SEARCH_TYPE = "search_type";

    public RankingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (movieViewModel == null) {
            movieViewModel = ViewModelProviders.of(requireActivity()).get(MovieViewModel.class);
        }
        rankingsSubject = movieViewModel.getRankingsSubject();
        listCompositeDisposable.add(rankingsSubject.subscribe(this::addMovies,
                e -> Log.e("rxprob", "rankingsSubject e=" + e.getMessage())));
        movieViewModel.publishRankedMovies();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rankings, container, false);
        mBoardView = view.findViewById(R.id.board_view);
        mBoardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBoardView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mBoardView.setColumnWidth(mBoardView.getWidth() / 2);
            }
        });
        mBoardView.setSnapToColumnsWhenScrolling(true);
        mBoardView.setSnapToColumnWhenDragging(true);
        mBoardView.setSnapDragItemToTouch(true);
        mBoardView.setSnapToColumnInLandscape(false);
        mBoardView.setColumnSnapPosition(BoardView.ColumnSnapPosition.CENTER);
        mBoardView.setBoardListener(new BoardView.BoardListener() {

            @Override
            public void onItemDragStarted(int column, int row) { itemDragStarted = true; }

            @Override
            public void onItemDragEnded(int fromColumn, int fromRow, int toColumn, int toRow) {
                if(itemDragStarted) {
                    itemDragStarted = false;
                } else {
                    return;
                }

                MediaByPerson movie;
                String movieID;
                try {
                    RankingsAdapter adapter = (RankingsAdapter) mBoardView.getAdapter(toColumn);
                    movieID = String.valueOf(adapter.getUniqueItemId(toRow));
                    movie = adapter.getFilmOrShow(toRow);
                } catch (Exception e) {
                    return;
                }

                if (fromColumn != toColumn || fromRow != toRow) {
                    if(fromColumn != toColumn) {
                        if(toColumn == 0) {
                            rankedList.remove(movie);
                            movieViewModel.updateRankingRemove(movieID, fromRow);
                        } else {
                            rankedList.add(movie);
                            movieViewModel.updateRankingNew(movie, toRow);
                        }
                    } else if (toColumn == 1){
                        if(fromRow > toRow) {
                            movieViewModel.updateRankingUp(movieID, fromRow, toRow);
                        } else {
                            movieViewModel.updateRankingDown(movieID, fromRow, toRow);
                        }
                    }
                }
            }

            @Override
            public void onItemChangedPosition(int oldColumn, int oldRow, int newColumn, int newRow) { }
            @Override
            public void onItemChangedColumn(int oldColumn, int newColumn) { }
            @Override
            public void onFocusedColumnChanged(int oldColumn, int newColumn) { }
            @Override
            public void onColumnDragStarted(int position) { }
            @Override
            public void onColumnDragChangedPosition(int oldPosition, int newPosition) { }
            @Override
            public void onColumnDragEnded(int position) { }
        });
        mBoardView.setBoardCallback(new BoardView.BoardCallback() {
            @Override
            public boolean canDragItemAtPosition(int column, int dragPosition) { return true; }
            @Override
            public boolean canDropItemAtPosition(int oldColumn, int oldRow, int newColumn, int newRow) { return true; }
        });

        view.findViewById(R.id.load_popular_ranks).setOnClickListener(
                v -> listCompositeDisposable.add(movieViewModel.getPopularForRankings()
                        .subscribe(this::addUnrankedColumn,
                                e -> Log.e("rxprob", "getPopularForRankings e=" + e.getMessage())
                        )
                )
        );

        view.findViewById(R.id.load_watched_ranks).setOnClickListener(
                v -> listCompositeDisposable.add(movieViewModel.getWatchedForRankings()
                        .subscribe(this::addUnrankedColumn,
                                e -> Log.e("rxprob", "getWatchedForRankings e=" + e.getMessage())
                        )
                )
        );


        if (movieViewModel == null) {
            movieViewModel = ViewModelProviders.of(requireActivity()).get(MovieViewModel.class);
        }

        ToggleButton toggleButton = view.findViewById(R.id.toggle_ranks);
        toggleButton.setChecked(movieViewModel.isFilmRankings());
        toggleButton.setOnCheckedChangeListener((compoundButton, b) -> {
            movieViewModel.setFilmShowRankings(b);
            movieViewModel.publishRankedMovies();
        });

        SearchView searchView = view.findViewById(R.id.search_ranks);

        searchView.setOnClickListener(v -> searchView.setIconified(false));

       listCompositeDisposable.add(RxSearchView
                .queryTextChanges(searchView)
                .skip(1)
                .debounce(600, TimeUnit.MILLISECONDS)
                .filter(charSequence -> !TextUtils.isEmpty(charSequence))
                .map(CharSequence::toString)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .switchMap(query -> movieViewModel.queryRankings(query))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> populateAdapter(s.getResults()),
                        e -> Log.e("rxprob", "RxSearchView.queryTextChanges e=" + e.getMessage())
                )
       );

        final String[] from = new String[]{SEARCH_TITLE, SEARCH_ID};
        final int[] to = new int[]{R.id.search_title};

        if (mSearchAdapter == null) {
            mSearchAdapter = new SimpleCursorAdapter(requireActivity(), R.layout.search_item,
                    null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        }
        searchView.setSuggestionsAdapter(mSearchAdapter);

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = mSearchAdapter.getCursor();
                cursor.moveToPosition(position);
                String id = cursor.getString(cursor.getColumnIndex(SEARCH_ID));
                movieViewModel.getMediaForRankings(id);
                searchView.setQuery("", false);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }
        });
        return view;
    }

    private void addMovies(List<MediaByPerson> movies) {
        rankedList.clear();
        List<MediaByPerson> unrankedList = new ArrayList<>();

        for (MediaByPerson movie : movies) {
            if(movie.getRanking() >= 0) {
                rankedList.add(movie);
            } else {
                unrankedList.add(movie);
            }
        }
        addUnrankedColumn(unrankedList);
        addRankedColumn(rankedList);
    }

    private void addRankedColumn(List<MediaByPerson> movies) {

        final ArrayList<Pair<Long, MediaByPerson>> mItemArray = new ArrayList<>();
        for (int i = 0; i < movies.size(); i++) {
            MediaByPerson movie = movies.get(i);
            mItemArray.add(new Pair<>(Long.parseLong(movie.getId()), movie));
        }

        if(mRankedAListdapter == null) {
            mRankedAListdapter = new RankingsAdapter(mItemArray, true);
            mRankedAListdapter.setClickListener(this);
            final View header = View.inflate(getActivity(), R.layout.column_header, null);
            ((TextView) header.findViewById(R.id.text)).setText(R.string.rankings);
            mBoardView.addColumn(mRankedAListdapter, header, header, false);
        } else {
            mRankedAListdapter.setItemList(mItemArray);
        }
    }



    private void addUnrankedColumn(List<MediaByPerson> movies) {

        final ArrayList<Pair<Long, MediaByPerson>> mItemArray = new ArrayList<>();
        for (int i = 0; i < movies.size(); i++) {
            MediaByPerson movie = movies.get(i);
            if(rankedList.contains(movie)) {
                continue;
            }
            mItemArray.add(new Pair<>(Long.parseLong(movie.getId()), movie));
        }

        if(mUnrankedAListdapter == null) {
            mUnrankedAListdapter = new RankingsAdapter(mItemArray, false);
            mUnrankedAListdapter.setClickListener(this);
            final View header = View.inflate(getActivity(), R.layout.column_header, null);
            ((TextView) header.findViewById(R.id.text)).setText("Unranked");
            mBoardView.addColumn(mUnrankedAListdapter, header, header, false);
        } else {
            mUnrankedAListdapter.setItemList(mItemArray);
        }
    }

    @Override
    public void onFilmClick(String id) {
        movieViewModel.getMovieInfo(id);
    }

    @Override
    public void onShowClick(String id) {
        movieViewModel.getShowInfo(id);
    }

    private void populateAdapter(List<MediaPOJO> mediaPOJOS) {
        if (mediaPOJOS == null) {
            return;
        }
        String[] columns = {
                BaseColumns._ID,
                SEARCH_TITLE,
                SEARCH_ID
        };

        MatrixCursor cursor = new MatrixCursor(columns);

        for (int i = 0; i < mediaPOJOS.size(); i++) {

            MediaPOJO mediaPOJO = mediaPOJOS.get(i);

            String id = mediaPOJO.getId();
            String name = mediaPOJO.getTitle();

            String[] row = {Integer.toString(i), name, id};
            cursor.addRow(row);
        }
        mSearchAdapter.changeCursor(cursor);
    }
}
