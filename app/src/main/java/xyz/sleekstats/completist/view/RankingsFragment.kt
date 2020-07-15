package xyz.sleekstats.completist.view

import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.widget.SearchView
import androidx.core.util.Pair
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView
import com.woxthebox.draglistview.BoardView
import com.woxthebox.draglistview.BoardView.BoardCallback
import com.woxthebox.draglistview.BoardView.BoardListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import xyz.sleekstats.completist.R
import xyz.sleekstats.completist.model.MediaByPerson
import xyz.sleekstats.completist.model.MediaPOJO
import xyz.sleekstats.completist.model.MediaQueryPOJO
import xyz.sleekstats.completist.viewmodel.MovieViewModel
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
class RankingsFragment : Fragment(), RankingsAdapter.ItemClickListener {
    private var mBoardView: BoardView? = null
    private var itemDragStarted = false
    private val listCompositeDisposable = CompositeDisposable()
    private var rankingsSubject: PublishSubject<List<MediaByPerson>>? = null
    private var mUnrankedAListdapter: RankingsAdapter? = null
    private var mRankedAListdapter: RankingsAdapter? = null
    private var mSearchAdapter: SimpleCursorAdapter? = null
    private val rankedList: MutableList<MediaByPerson> = ArrayList()

    val movieViewModel by activityViewModels<MovieViewModel>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        rankingsSubject = movieViewModel.rankingsSubject
        rankingsSubject?.let {
            listCompositeDisposable.add(
                    it.subscribe({ movies: List<MediaByPerson> -> addMovies(movies) }
                    ) { e: Throwable -> Log.e("rxprob", "rankingsSubject e=" + e.message) })
        }
        movieViewModel.publishRankedMovies()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rankings, container, false)
        mBoardView = view.findViewById(R.id.board_view)
        mBoardView?.apply {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    setColumnWidth(width / 2)
                }
            })
            setSnapToColumnsWhenScrolling(true)
            setSnapToColumnWhenDragging(true)
            setSnapDragItemToTouch(true)
            setSnapToColumnInLandscape(false)
            setColumnSnapPosition(BoardView.ColumnSnapPosition.CENTER)
            setBoardListener(object : BoardListener {
                override fun onItemDragStarted(column: Int, row: Int) {
                    itemDragStarted = true
                }

                override fun onItemDragEnded(fromColumn: Int, fromRow: Int, toColumn: Int, toRow: Int) {
                    itemDragStarted = if (itemDragStarted) {
                        false
                    } else {
                        return
                    }
                    val movie: MediaByPerson
                    val movieID: String
                    try {
                        val adapter = mBoardView?.getAdapter(toColumn) as RankingsAdapter
                        movieID = adapter.getUniqueItemId(toRow).toString()
                        movie = adapter.getFilmOrShow(toRow)
                    } catch (e: Exception) {
                        return
                    }
                    if (fromColumn != toColumn || fromRow != toRow) {
                        if (fromColumn != toColumn) {
                            if (toColumn == 0) {
                                rankedList.remove(movie)
                                movieViewModel.updateRankingRemove(movieID, fromRow)
                            } else {
                                rankedList.add(movie)
                                movieViewModel.updateRankingNew(movie, toRow)
                            }
                        } else if (toColumn == 1) {
                            if (fromRow > toRow) {
                                movieViewModel.updateRankingUp(movieID, fromRow, toRow)
                            } else {
                                movieViewModel.updateRankingDown(movieID, fromRow, toRow)
                            }
                        }
                    }
                }

                override fun onItemChangedPosition(oldColumn: Int, oldRow: Int, newColumn: Int, newRow: Int) = Unit
                override fun onItemChangedColumn(oldColumn: Int, newColumn: Int) = Unit
                override fun onFocusedColumnChanged(oldColumn: Int, newColumn: Int) = Unit
                override fun onColumnDragStarted(position: Int) = Unit
                override fun onColumnDragChangedPosition(oldPosition: Int, newPosition: Int) = Unit
                override fun onColumnDragEnded(position: Int) = Unit
            })
            setBoardCallback(object : BoardCallback {
                override fun canDragItemAtPosition(column: Int, dragPosition: Int): Boolean = true

                override fun canDropItemAtPosition(oldColumn: Int, oldRow: Int, newColumn: Int, newRow: Int): Boolean = true
            })

        }
        view.findViewById<View>(R.id.load_popular_ranks).setOnClickListener {
            listCompositeDisposable.add(movieViewModel.popularForRankings
                    .subscribe({ movies: List<MediaByPerson> -> addUnrankedColumn(movies) }
                    ) { e: Throwable -> Log.e("rxprob", "getPopularForRankings e=" + e.message) }
            )
        }
        view.findViewById<View>(R.id.load_watched_ranks).setOnClickListener {
            listCompositeDisposable.add(movieViewModel.watchedForRankings
                    .subscribe({ movies: List<MediaByPerson> -> addUnrankedColumn(movies) }
                    ) { e: Throwable -> Log.e("rxprob", "getWatchedForRankings e=" + e.message) }
            )
        }
        val toggleButton = view.findViewById<ToggleButton>(R.id.toggle_ranks)
        toggleButton.isChecked = movieViewModel.isFilmRankings
        toggleButton.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            movieViewModel.setFilmShowRankings(b)
            movieViewModel.publishRankedMovies()
        }
        val searchView: SearchView = view.findViewById(R.id.search_ranks)
        searchView.setOnClickListener { searchView.isIconified = false }
        listCompositeDisposable.add(RxSearchView
                .queryTextChanges(searchView)
                .skip(1)
                .debounce(600, TimeUnit.MILLISECONDS)
                .filter { charSequence: CharSequence? -> !TextUtils.isEmpty(charSequence) }
                .map { obj: CharSequence -> obj.toString() }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .switchMap { query: String? -> movieViewModel.queryRankings(query) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ s: MediaQueryPOJO -> populateAdapter(s.results) }
                ) { e: Throwable -> Log.e("rxprob", "RxSearchView.queryTextChanges e=" + e.message) }
        )
        val from = arrayOf(SEARCH_TITLE, SEARCH_ID)
        val to = intArrayOf(R.id.search_title)
        if (mSearchAdapter == null) {
            mSearchAdapter = SimpleCursorAdapter(requireActivity(), R.layout.search_item,
                    null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
        }
        searchView.suggestionsAdapter = mSearchAdapter
        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionClick(position: Int): Boolean {
                val cursor = mSearchAdapter?.cursor
                cursor?.moveToPosition(position)
                val id = cursor?.getString(cursor.getColumnIndex(SEARCH_ID))
                movieViewModel.getMediaForRankings(id)
                searchView.setQuery("", false)
                searchView.clearFocus()
                return true
            }

            override fun onSuggestionSelect(position: Int): Boolean = true
        })
        return view
    }

    private fun addMovies(movies: List<MediaByPerson>) {
        rankedList.clear()
        val unrankedList: MutableList<MediaByPerson> = ArrayList()
        for (movie in movies) {
            if (movie.ranking >= 0) {
                rankedList.add(movie)
            } else {
                unrankedList.add(movie)
            }
        }
        addUnrankedColumn(unrankedList)
        addRankedColumn(rankedList)
    }

    private fun addRankedColumn(movies: List<MediaByPerson>) {
        val mItemArray = ArrayList<Pair<Long, MediaByPerson>>()
        for (i in movies.indices) {
            val movie = movies[i]
            mItemArray.add(Pair(movie.id.toLong(), movie))
        }
        if (mRankedAListdapter == null) {
            mRankedAListdapter = RankingsAdapter(mItemArray, true)
            mRankedAListdapter?.setClickListener(this)
            val header = View.inflate(activity, R.layout.column_header, null)
            (header.findViewById<View>(R.id.text) as TextView).setText(R.string.rankings)
            mBoardView?.addColumn(mRankedAListdapter, header, header, false)
        } else {
            mRankedAListdapter?.itemList = mItemArray
        }
    }

    private fun addUnrankedColumn(movies: List<MediaByPerson>) {
        val mItemArray = ArrayList<Pair<Long, MediaByPerson>>()
        for (i in movies.indices) {
            val movie = movies[i]
            if (rankedList.contains(movie)) {
                continue
            }
            mItemArray.add(Pair(movie.id.toLong(), movie))
        }
        if (mUnrankedAListdapter == null) {
            mUnrankedAListdapter = RankingsAdapter(mItemArray, false)
            mUnrankedAListdapter?.setClickListener(this)
            val header = View.inflate(activity, R.layout.column_header, null)
            (header.findViewById<View>(R.id.text) as TextView).setText(R.string.unranked)
            mBoardView?.addColumn(mUnrankedAListdapter, header, header, false)
        } else {
            mUnrankedAListdapter?.itemList = mItemArray
        }
    }

    override fun onFilmClick(id: String?) = movieViewModel.getMovieInfo(id)

    override fun onShowClick(id: String?) = movieViewModel.getShowInfo(id)

    private fun populateAdapter(mediaPOJOS: List<MediaPOJO>?) {
        if (mediaPOJOS == null) {
            return
        }
        val columns = arrayOf(
                BaseColumns._ID,
                SEARCH_TITLE,
                SEARCH_ID
        )
        val cursor = MatrixCursor(columns)
        for (i in mediaPOJOS.indices) {
            val mediaPOJO = mediaPOJOS[i]
            val id = mediaPOJO.id
            val name = mediaPOJO.title
            val row = arrayOf(i.toString(), name, id)
            cursor.addRow(row)
        }
        mSearchAdapter?.changeCursor(cursor)
    }

    companion object {
        private const val SEARCH_TITLE = "title"
        private const val SEARCH_ID = "search_id"
        private const val SEARCH_TYPE = "search_type"
    }
}