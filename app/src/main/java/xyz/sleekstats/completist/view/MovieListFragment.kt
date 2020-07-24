package xyz.sleekstats.completist.view

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding2.widget.RxAdapterView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import xyz.sleekstats.completist.R
import xyz.sleekstats.completist.databinding.FragmentListBinding
import xyz.sleekstats.completist.databinding.MovieKeys
import xyz.sleekstats.completist.model.MediaByPerson
import xyz.sleekstats.completist.model.PersonPOJO
import xyz.sleekstats.completist.model.WatchCount
import xyz.sleekstats.completist.viewmodel.MovieViewModel

//Shows details of, and list of films by, a specific actor/director
class MovieListFragment : Fragment(), MovieAdapter.ItemClickListener, View.OnClickListener {

    private var mRoleSpinner: Spinner? = null
    private var mListSaveButton: FloatingActionButton? = null
    private var mMoviesRecyclerView: RecyclerView? = null
    private var mMovieAdapter: MovieAdapter? = null
    private var mCurrentFilmList = mutableListOf<MediaByPerson>()
    private var summaryTextView: TextView? = null
    private val onScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (!recyclerView.canScrollVertically(1)) {
                movieViewModel.onScrollEnd()
            }
        }
    }
    private val listCompositeDisposable = CompositeDisposable()
    private var fragmentListBinding: FragmentListBinding? = null
    private var filmListPublishSubject: PublishSubject<List<MediaByPerson>>? = null
    private var watchCountPublishSubject: PublishSubject<WatchCount>? = null


    private val movieViewModel by activityViewModels<MovieViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_list, container, false)
        fragmentListBinding?.listClick = this
        val rootView = fragmentListBinding?.root
        rootView?.let { initRecyclerView(it) }
        mListSaveButton = rootView?.findViewById(R.id.listSaveButton)
        mRoleSpinner = rootView?.findViewById(R.id.role_spinner)
        val adapter = ArrayAdapter.createFromResource(requireActivity(),
                R.array.list_options_array, R.layout.spinner_item)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        mRoleSpinner?.let {
            it.adapter = adapter
            listCompositeDisposable.add(RxAdapterView.itemSelections(it)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ pos -> movieViewModel.onSpin(pos) }
                    ) { e: Throwable -> Log.e(TAG_RXERROR, "Spinner error: " + e.message) }
            )
        }
        mListSaveButton?.setOnClickListener {
            movieViewModel.addOrRemoveList()?.let {
                listCompositeDisposable.add(
                        it.observeOn(AndroidSchedulers.mainThread()
                        ).subscribe(
                                { setSaveButton(R.drawable.ic_add_black_24dp) },
                                { error: Throwable -> Log.e(TAG_RXERROR, "addOrRemoveList" + error.message) }
                        ) { setSaveButton(R.drawable.ic_done_green_24dp) }
                )
            }
        }
        summaryTextView = rootView?.findViewById(R.id.person_summary)
        return rootView
    }

    private fun setSaveButton(drawable: Int) {
        mListSaveButton?.apply {
            hide()
            setImageResource(drawable)
            show()
        }
        val textView = view?.findViewById<TextView>(R.id.person_name)
        if (drawable == R.drawable.ic_done_green_24dp) {
            textView?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done_green_24dp, 0)
            setForeground(R.drawable.poster_border_watched)
        } else {
            textView?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            setForeground(R.drawable.poster_border)
        }
    }

    private fun setForeground(drawableID: Int) {
        if (Build.VERSION.SDK_INT > 23) {
            val posterView = view?.findViewById<ImageView>(R.id.person_poster)
            posterView?.foreground = ContextCompat.getDrawable(requireActivity(), drawableID)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val personPublishSubject = movieViewModel.personPublishSubject
        filmListPublishSubject = movieViewModel.filmListPublishSubject
        watchCountPublishSubject = movieViewModel.watchCountPublishSubject

        listCompositeDisposable.add(personPublishSubject.subscribe({ personPOJO: PersonPOJO -> setPersonView(personPOJO) }
        ) { e: Throwable -> Log.e(TAG_RXERROR, "personPublishSubject e=" + e.message) })

        filmListPublishSubject?.let {
            listCompositeDisposable.add(it.subscribe({ mediaByPersonList: List<MediaByPerson> -> setRecyclerView(mediaByPersonList) }
            ) { e: Throwable ->
                Log.d("fata", "filmListPublishSubject")
                movieViewModel.finishScrollLoading()
                Log.e(TAG_RXERROR, "filmListPublishSubject e=" + e.message)
            })
        }
        watchCountPublishSubject?.let {
            listCompositeDisposable.add(it.subscribe({ count: WatchCount? -> fragmentListBinding?.watchCount = count }
            ) { e: Throwable -> Log.e(TAG_RXERROR, "watchCountPublishSubject e=" + e.message) })
        }
        movieViewModel.films
    }

    //Set display with info for selected actor/director
    private fun setPersonView(personPOJO: PersonPOJO) {
        fragmentListBinding?.person = personPOJO
        setSummaryText(personPOJO.biography)
        setSpinner(personPOJO.known_for_department)

        when (val id = personPOJO.id) {
            MovieKeys.LIST_POPULAR, MovieKeys.LIST_NOWPLAYING, MovieKeys.LIST_TOPRATED, MovieKeys.LIST_GENRE -> {
                addScrollListener()
                mListSaveButton?.hide()
            }
            MovieKeys.LIST_WATCHED, MovieKeys.LIST_QUEUED -> {
                mListSaveButton?.hide()
                removeScrollListener()
            }
            else -> {
                removeScrollListener()
                listCompositeDisposable.add(
                        movieViewModel.checkIfListExists(id).subscribeOn(Schedulers.io()
                        ).observeOn(AndroidSchedulers.mainThread()
                        ).subscribe({ setSaveButton(R.drawable.ic_done_green_24dp) },
                                { setSaveButton(R.drawable.ic_add_black_24dp) }
                        ) { setSaveButton(R.drawable.ic_add_black_24dp) }
                )
            }
        }
    }

    private fun setSpinner(knownFor: String) {
        val oldPos = mRoleSpinner?.selectedItemPosition
        val currentPos: Int
        movieViewModel.setInitialSpin(true)
        if (mRoleSpinner == null) {
            return
        }
        when (knownFor) {
            "Movies" -> {
                mRoleSpinner?.visibility = View.GONE
                return
            }
            "Acting" -> currentPos = 1
            "Directing", "Writing", "Screenplay" -> currentPos = 2
            else -> currentPos = 0
        }
        mRoleSpinner?.visibility = View.VISIBLE
        if (currentPos == oldPos) {
            movieViewModel.setInitialSpin(false)
            return
        }
        mRoleSpinner?.setSelection(currentPos)
    }

    private fun initRecyclerView(rootView: View) {
        mMoviesRecyclerView = rootView.findViewById(R.id.film_list)
        val grids = resources.getInteger(R.integer.grid_number)
        mMoviesRecyclerView?.layoutManager = GridLayoutManager(requireContext(), grids)
    }

    private fun addScrollListener() {
        if (mMoviesRecyclerView == null) {
            initRecyclerView(view ?: return)
        }
        mMoviesRecyclerView?.addOnScrollListener(onScrollListener)
    }

    private fun removeScrollListener() {
        if (mMoviesRecyclerView == null) {
            initRecyclerView(view ?: return)
        }
        mMoviesRecyclerView?.removeOnScrollListener(onScrollListener)
    }

    //Populate recyclerview with films from actor/director
    private fun setRecyclerView(mediaByPersonList: List<MediaByPerson>) {
        mCurrentFilmList.clear()
        mCurrentFilmList.addAll(mediaByPersonList)

        if (mMoviesRecyclerView == null) {
            view?.let {
                initRecyclerView(it)
            }
        }
        if (mMovieAdapter == null) {
            mMovieAdapter = MovieAdapter(mCurrentFilmList)
            mMovieAdapter?.setClickListener(this)
            mMoviesRecyclerView?.adapter = mMovieAdapter
        } else {
            mMovieAdapter?.notifyDataSetChanged()
        }
        movieViewModel.finishScrollLoading()
    }

    override fun onFilmClick(movieID: String?, isFilm: Boolean) = if (isFilm) movieViewModel.getMovieInfo(movieID) else movieViewModel.getShowInfo(movieID)

    override fun onFilmWatched(pos: Int) {
        val film = mCurrentFilmList[pos]
        listCompositeDisposable.add(
                movieViewModel.onMovieWatchedFromList(film).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ setItemWatched(film, film.isWatched, pos) }
                        ) { setItemWatched(film, film.isWatched, pos) }
        )
    }

    override fun onFilmQueued(pos: Int) {
        val film = mCurrentFilmList[pos]
        listCompositeDisposable.add(
                movieViewModel.onMovieQueuedFromList(film).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()
                ).subscribe({ setItemQueued(film, film.isQueued, pos) }
                ) { setItemQueued(film, film.isQueued, pos) }
        )
    }

    private fun setItemWatched(film: MediaByPerson, watched: Boolean, pos: Int) {
        if (mMovieAdapter == null) {
            return
        }
        film.isWatched = watched
        mMovieAdapter?.notifyItemChanged(pos)
    }

    private fun setItemQueued(film: MediaByPerson, queued: Boolean, pos: Int) {
        if (mMovieAdapter == null) {
            return
        }
        film.isQueued = queued
        mMovieAdapter?.notifyItemChanged(pos)
    }

    override fun onDetach() {
        super.onDetach()
        listCompositeDisposable.clear()
    }

    private fun setSummaryText(text: String) {
        val lines = resources.getInteger(R.integer.lines)
        summaryTextView?.apply {
            this.text = text
            viewTreeObserver?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver?.removeOnGlobalLayoutListener(this)
                    if (lineCount > lines) {
                        setReadMore(text)
                    }
                }
            })

        }
    }

    private fun setReadMore(text: String) {
        val lines = resources.getInteger(R.integer.lines)
        val end = (lines * (summaryTextView?.getOffsetForPosition(summaryTextView?.width?.toFloat()
                ?: 0f, 0f) ?: 0).coerceAtMost(text.length))
        val readMoreText = "... (Read More)"
        val readMoreLength = readMoreText.length
        if (end > readMoreLength) {
            val displayed = (text.substring(0, end - readMoreLength) + readMoreText).replace("\n".toRegex(), " ")
            val ss = SpannableString(displayed)
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) = openSummaryDialog(text)

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }
            ss.setSpan(clickableSpan, displayed.length - readMoreLength + 4, displayed.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            summaryTextView?.apply {
                this.text = ss
                movementMethod = LinkMovementMethod.getInstance()
                highlightColor = Color.TRANSPARENT
            }
        }
    }

    private fun openSummaryDialog(text: String) {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val newFragment: DialogFragment = PersonSummaryDialog.newInstance(text)
        newFragment.show(fragmentTransaction, "")
    }

    override fun onClick(view: View) {
        try {
            view.getTag(R.id.id)?.toString()?.let {
                when (view.id) {
                    R.id.tmdb_view -> goToSite("https://www.themoviedb.org/person/$it")
                    R.id.imdb_view -> goToSite("https://www.imdb.com/name/$it")
                }
            }
                    ?: Toast.makeText(requireContext(), "Error: Link not found", Toast.LENGTH_SHORT).show()
        } catch (ignored: Exception) {
        }
    }

    private fun goToSite(site: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(site)))

    companion object {
        private const val TAG_RXERROR = "rxprobMovieList"
    }
}