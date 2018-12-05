package xyz.sleekstats.completist.view;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.woxthebox.draglistview.BoardView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;
import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.FilmByPerson;
import xyz.sleekstats.completist.viewmodel.MovieViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class RankingsFragment extends Fragment {


    private BoardView mBoardView;
    private boolean itemDragStarted;
    private MovieViewModel movieViewModel;
    private final CompositeDisposable listCompositeDisposable = new CompositeDisposable();
    private PublishSubject<List<FilmByPerson>> rankingsSubject;

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
        movieViewModel.publishSavedMovies();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
            public void onItemDragStarted(int column, int row) {
                Log.d("rankingsdr", "onItemDragStarted" + column +", " + row);
                itemDragStarted = true;
            }

            @Override
            public void onItemDragEnded(int fromColumn, int fromRow, int toColumn, int toRow) {
                if(itemDragStarted) {
                    itemDragStarted = false;
                } else {
                    Log.d("rankingsfals", "itemdragstarted is false!");
                    return;
                }
                Log.d("rankings", "onItemDragEnded  old = " + fromColumn + ", " + fromRow + "  new = "  + toColumn + ", "  + toRow);
                String movieID;
                try {
                    movieID = String.valueOf(mBoardView.getAdapter(toColumn).getUniqueItemId(toRow));
                } catch (Exception e) {
                    Log.e("rankingslis", e.getMessage());
                    return;
                }

                if (fromColumn != toColumn || fromRow != toRow) {
                    if(fromColumn != toColumn) {
                        if(toColumn == 0) {
                            movieViewModel.updateRankingRemove(movieID, fromRow);
                            Log.d("rankings", "updateRankingRemove " + movieID);
                        } else {
                            movieViewModel.updateRankingNew(movieID, toRow);
                            Log.d("rankings", "updateRankingNew " + movieID);
                        }
                    } else if (toColumn == 1){
                        if(fromRow > toRow) {
                            movieViewModel.updateRankingUp(movieID, fromRow, toRow);
                            Log.d("rankings", "updateRankingUp " + movieID);
                        } else {
                            movieViewModel.updateRankingDown(movieID, fromRow, toRow);
                            Log.d("rankings", "updateRankingDown " + movieID);
                        }
                    }
                }
            }

            @Override
            public void onItemChangedPosition(int oldColumn, int oldRow, int newColumn, int newRow) {
                //Toast.makeText(mBoardView.getContext(), "Position changed - column: " + newColumn + " row: " + newRow, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemChangedColumn(int oldColumn, int newColumn) {
                if(newColumn > oldColumn) {

                }
            }

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
            public boolean canDragItemAtPosition(int column, int dragPosition) {
                // Add logic here to prevent an item to be dragged
                return true;
            }

            @Override
            public boolean canDropItemAtPosition(int oldColumn, int oldRow, int newColumn, int newRow) {
                // Add logic here to prevent an item to be dropped
                return true;
            }
        });
        return view;
    }

    private void addMovies(List<FilmByPerson> movies) {
        List<FilmByPerson> rankedList = new ArrayList<>();
        List<FilmByPerson> unrankedList = new ArrayList<>();

        for (FilmByPerson movie : movies) {
            Log.d("rankingslist", movie.getTitle() + " " + movie.getRanking());
            if(movie.getRanking() >= 0) {
                rankedList.add(movie);
            } else {
                unrankedList.add(movie);
            }
        }
        addColumn(unrankedList, false);
        addColumn(rankedList, true);
    }

    private void addColumn(List<FilmByPerson> movies, boolean ranked) {

        final ArrayList<Pair<Long, String>> mItemArray = new ArrayList<>();
        for (int i = 0; i < movies.size(); i++) {
            FilmByPerson movie = movies.get(i);
            mItemArray.add(new Pair<>(Long.parseLong(movie.getId()), movie.getTitle()));
        }

        final RankingsAdapter listAdapter = new RankingsAdapter(mItemArray, ranked);
        final View header = View.inflate(getActivity(), R.layout.column_header, null);
        String rankedString = ranked ? "Rankings" : "Unranked";
        ((TextView) header.findViewById(R.id.text)).setText(rankedString);
        ((TextView) header.findViewById(R.id.item_count)).setText(String.valueOf(movies.size()));
        mBoardView.addColumn(listAdapter, header, header, false);
    }

}
