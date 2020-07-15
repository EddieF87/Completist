package xyz.sleekstats.completist.view

import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import xyz.sleekstats.completist.R
import xyz.sleekstats.completist.model.MediaPOJO
import xyz.sleekstats.completist.model.MediaQueryPOJO
import xyz.sleekstats.completist.viewmodel.MovieViewModel
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    var myViewPager: ViewPager2? = null
    private var myPagerAdapter: MyPagerAdapter? = null
    private var mBottomNavigationView: BottomNavigationView? = null
    private var movieDetailsFragment: MovieDetailsFragment? = null
    private var movieListFragment: MovieListFragment? = null
    private var myListsFragment: MyListsFragment? = null
    private var rankingsFragment: RankingsFragment? = null
    private var movieViewModel: MovieViewModel? = null
    private var mSearchAdapter: SimpleCursorAdapter? = null
    private val mainCompositeDisposable = CompositeDisposable()
    private var mSearchDisposable: Disposable? = null
    private var viewPagerSubject: PublishSubject<Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (movieViewModel == null) {
            movieViewModel = ViewModelProvider(this@MainActivity).get(MovieViewModel::class.java)
        }
        startPager()
        val adView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    override fun onStart() {
        super.onStart()
        viewPagerSubject = movieViewModel!!.viewPagerSubject
        mainCompositeDisposable.add(viewPagerSubject!!
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ i: Int -> myViewPager?.currentItem = i }
                ) { e: Throwable -> Log.e("rxprob", "viewPagerSubject e=${e.message}") }
        )
    }

    private fun startPager() {
        myViewPager = findViewById(R.id.my_view_pager)
        if (myPagerAdapter == null) {
            myPagerAdapter = MyPagerAdapter()
        }
        myViewPager?.apply {
            offscreenPageLimit = 3
            adapter = myPagerAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    mBottomNavigationView?.menu?.getItem(position)?.isChecked = true
                    isUserInputEnabled = position != 3
                }
            }
            )
        }
        mBottomNavigationView = findViewById(R.id.bottom_nav)
        mBottomNavigationView?.setOnNavigationItemSelectedListener { item: MenuItem ->
            val i: Int = when (item.itemId) {
                R.id.navigation_lists -> 0
                R.id.navigation_actor -> 1
                R.id.navigation_movie -> 2
                R.id.navigation_rank -> 3
                else -> return@setOnNavigationItemSelectedListener false
            }
            myViewPager?.currentItem = i
            true
        }
    }

    private inner class MyPagerAdapter : FragmentStateAdapter(this) {

        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> {
                if (myListsFragment == null) {
                    myListsFragment = MyListsFragment()
                }
                myListsFragment!!
            }
            1 -> {
                if (movieListFragment == null) {
                    movieListFragment = MovieListFragment()
                }
                movieListFragment!!
            }
            2 -> {
                if (movieDetailsFragment == null) {
                    movieDetailsFragment = MovieDetailsFragment()
                }
                movieDetailsFragment!!
            }
            3 -> {
                if (rankingsFragment == null) {
                    rankingsFragment = RankingsFragment()
                }
                rankingsFragment!!
            }
            else -> Fragment()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setIconifiedByDefault(true)
        mSearchDisposable = RxSearchView
                .queryTextChanges(searchView)
                .skip(1)
                .debounce(600, TimeUnit.MILLISECONDS)
                .filter { charSequence: CharSequence? -> !TextUtils.isEmpty(charSequence) }
                .map { obj: CharSequence -> obj.toString() }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .switchMap { query: String? -> movieViewModel!!.queryMedia(query) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ s: MediaQueryPOJO -> populateAdapter(s.results) }
                ) { e: Throwable -> Log.e("rxprob", "RxSearchView.queryTextChanges e=${e.message}") }
        val from = arrayOf(SEARCH_TITLE, SEARCH_ID)
        val to = intArrayOf(R.id.search_title)
        if (mSearchAdapter == null) {
            mSearchAdapter = SimpleCursorAdapter(this@MainActivity, R.layout.search_item,
                    null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
        }
        searchView.suggestionsAdapter = mSearchAdapter
        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionClick(position: Int): Boolean {
                val cursor = mSearchAdapter!!.cursor
                cursor.moveToPosition(position)
                val id = cursor.getString(cursor.getColumnIndex(SEARCH_ID))
                when (cursor.getString(cursor.getColumnIndex(SEARCH_TYPE))) {
                    "person" -> movieViewModel!!.getFilmsByPerson(id)
                    "movie" -> movieViewModel!!.getMovieInfo(id)
                    else -> movieViewModel!!.getShowInfo(id)
                }
                return true
            }

            override fun onSuggestionSelect(position: Int): Boolean = true
        })
        return true
    }

    private fun populateAdapter(mediaPOJOS: List<MediaPOJO>?) {
        if (mediaPOJOS == null) {
            return
        }
        val columns = arrayOf(
                BaseColumns._ID,
                SEARCH_TITLE,
                SEARCH_ID,
                SEARCH_TYPE
        )
        val cursor = MatrixCursor(columns)
        for (i in mediaPOJOS.indices) {
            val mediaPOJO = mediaPOJOS[i]
            val type = mediaPOJO.media_type
            val id = mediaPOJO.id
            val name = "${mediaPOJO.title} (${type.toUpperCase(Locale.getDefault())})"
            val row = arrayOf(i.toString(), name, id, type)
            cursor.addRow(row)
        }
        mSearchAdapter!!.changeCursor(cursor)
    }

    override fun onStop() {
        super.onStop()
        mainCompositeDisposable.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mSearchDisposable != null) {
            mSearchDisposable!!.dispose()
        }
    }

    companion object {
        private const val SEARCH_TITLE = "title"
        private const val SEARCH_ID = "search_id"
        private const val SEARCH_TYPE = "search_type"
    }
}