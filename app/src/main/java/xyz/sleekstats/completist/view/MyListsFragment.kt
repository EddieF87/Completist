package xyz.sleekstats.completist.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import xyz.sleekstats.completist.R
import xyz.sleekstats.completist.databinding.MovieKeys
import xyz.sleekstats.completist.model.Genre
import xyz.sleekstats.completist.model.GenreList
import xyz.sleekstats.completist.model.MyList
import xyz.sleekstats.completist.view.GenresDialog.GenreSelector
import xyz.sleekstats.completist.viewmodel.MovieViewModel

class MyListsFragment : Fragment(), MyListsAdapter.ItemClickListener, GenreSelector {
    private var mPopularListRV: RecyclerView? = null
    private var mSavedListRV: RecyclerView? = null
    private var mPopularListAdapter: MyListsAdapter? = null
    private val mSavedListAdapter: MyListsAdapter? = null
    private val myListsCompositeDisposable = CompositeDisposable()
    private var radioGroup1: RadioGroup? = null
    private var radioGroup2: RadioGroup? = null
    private var popularActorsSubject: PublishSubject<List<MyList>>? = null

    private val movieViewModel by activityViewModels<MovieViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_lists, container, false)
        view.apply {
            radioGroup1 = findViewById(R.id.radio_group1)
            radioGroup2 = findViewById(R.id.radio_group2)
            findViewById<View>(R.id.watched_movies_btn).setOnClickListener { onListClick(MovieKeys.LIST_WATCHED) }
            findViewById<View>(R.id.popular_movies_btn).setOnClickListener { onListClick(MovieKeys.LIST_POPULAR) }
            findViewById<View>(R.id.genres_btn).setOnClickListener { onGenreClick() }
            findViewById<View>(R.id.nowshowing_movies_btn).setOnClickListener { onListClick(MovieKeys.LIST_NOWPLAYING) }
            findViewById<View>(R.id.top_movies_btn).setOnClickListener { onListClick(MovieKeys.LIST_TOPRATED) }
            findViewById<View>(R.id.scheduled_btn).setOnClickListener { onListClick(MovieKeys.LIST_QUEUED) }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        popularActorsSubject = movieViewModel.popularActorsSubject
        popularActorsSubject?.let {
            myListsCompositeDisposable.add(
                    it.subscribe({ myLists: List<MyList> -> loadPopularRV(myLists) }
                    ) { e: Throwable ->
                        movieViewModel.finishScrollLoading()
                        Log.e("rxprob", "getPopularActorsSubject loadPopularRV" + e.message)
                    }
            )
        }
        movieViewModel.popularActors
        myListsCompositeDisposable.add(
                movieViewModel.savedLists.subscribe({ myLists: List<MyList> -> loadSavedRV(myLists) }
                ) { e: Throwable -> Log.e("rxprob", "getSavedLists loadSavedRV" + e.message) }
        )

    }

    private fun loadRV(myLists: List<MyList>, rv: RecyclerView?, adapter: MyListsAdapter?) {
        var myListsAdapter = adapter
        if (myListsAdapter == null) {
            myListsAdapter = MyListsAdapter(myLists)
            myListsAdapter.setClickListener(this)
            rv?.adapter = myListsAdapter
        } else {
            myListsAdapter.notifyDataSetChanged()
        }
    }

    private fun loadPopularRV(myLists: List<MyList>) {
        if (mPopularListRV == null) {
            val rootView = view
            if (rootView == null) {
                movieViewModel.finishScrollLoading()
                return
            }
            mPopularListRV = rootView.findViewById(R.id.pop_lists_rv)
            mPopularListRV?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            mPopularListRV?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollHorizontally(1)) {
                        movieViewModel.queryPopularActors()
                    }
                }
            })
        }
        if (mPopularListAdapter == null) {
            mPopularListAdapter = MyListsAdapter(myLists)
            mPopularListAdapter?.setClickListener(this)
            mPopularListRV?.adapter = mPopularListAdapter
        } else {
            mPopularListAdapter?.notifyDataSetChanged()
        }
        movieViewModel.finishScrollLoading()
    }

    private fun loadSavedRV(myLists: List<MyList>) {
        if (mSavedListRV == null) {
            val rootView = view ?: return
            mSavedListRV = rootView.findViewById(R.id.saved_lists_rv)
            mSavedListRV?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }
        loadRV(myLists, mSavedListRV, mSavedListAdapter)
    }

    override fun onDetach() {
        super.onDetach()
        myListsCompositeDisposable.clear()
    }

    private fun onGenreClick() {
        clearRadioGroups()
        myListsCompositeDisposable.add(
                movieViewModel.getGenreList(true).subscribe({ genres: GenreList? -> openGenresDialog(genres) }
                ) { e: Throwable -> Log.e("rxprob", "getSavedLists loadSavedRV" + e.message) }
        )
    }

    private fun openGenresDialog(genres: GenreList?) {
        if (genres == null) {
            return
        }
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val newFragment = GenresDialog.newInstance(genres)
        newFragment.show(fragmentTransaction, "")
        newFragment.setGenreSelector(this)
    }

    override fun onListClick(movieID: String?) {
        clearRadioGroups()
        movieViewModel.updateFilms(movieID)
    }

    private fun clearRadioGroups() {
        radioGroup1?.clearCheck()
        radioGroup2?.clearCheck()
    }

    override fun onGenreSelected(genre: Genre?) {
        genre?.let {
            movieViewModel.getFilmsByGenre(genre, true)
        }
    }
}