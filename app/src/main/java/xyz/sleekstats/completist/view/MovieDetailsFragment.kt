package xyz.sleekstats.completist.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import xyz.sleekstats.completist.R
import xyz.sleekstats.completist.databinding.FragmentMovieBinding
import xyz.sleekstats.completist.model.CastCredits
import xyz.sleekstats.completist.model.CastInfo
import xyz.sleekstats.completist.model.Genre
import xyz.sleekstats.completist.model.MediaPOJO
import xyz.sleekstats.completist.viewmodel.MovieViewModel
import java.util.*

//Shows details for selected film, including director/cast, rating, and summary
class MovieDetailsFragment : Fragment(), CastAdapter.ItemClickListener, View.OnClickListener {
    private var mFilm: MediaPOJO? = null
    private var mCastView: RecyclerView? = null
    private var mCastAdapter: CastAdapter? = null
    private var filmDetailsSubject: PublishSubject<MediaPOJO>? = null
    private var movieBinding: FragmentMovieBinding? = null
    private val listCompositeDisposable = CompositeDisposable()

    private val movieViewModel by activityViewModels<MovieViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        movieBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_movie, container, false)
        movieBinding!!.let { binding ->
            binding.detailsClick = this
            filmDetailsSubject = movieViewModel.filmDetailsPublishSubject
            filmDetailsSubject?.let {
                listCompositeDisposable.add(
                        it.subscribe({ mediaPOJO: MediaPOJO? -> setMovieInfoDisplay(mediaPOJO) }
                        ) { e: Throwable -> Log.e(TAG_RXERROR, " getFilmDetailsPublishSubject e = " + e.message) }
                )
            }
            val rootView = binding.root
            mCastView = rootView.findViewById(R.id.cast_recyclerview)
            mCastView?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            rootView.findViewById<View>(R.id.details_watched_btn).setOnClickListener { setMovieInfoDisplay(movieViewModel.onMovieWatchedFromDetails(mFilm)) }
            rootView.findViewById<View>(R.id.details_queue_btn).setOnClickListener { setMovieInfoDisplay(movieViewModel.onMovieQueuedFromDetails(mFilm)) }
            return rootView
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        movieViewModel.showOrFilm()
    }

    //Set display of movie details
    private fun setMovieInfoDisplay(mediaPOJO: MediaPOJO?) {
        if (mediaPOJO == null) {
            return
        }
        mFilm = mediaPOJO
        movieBinding?.film = mFilm
        setCastRecyclerView(mediaPOJO.castCredits)
    }

    //Populate recyclerview of cast names and images
    private fun setCastRecyclerView(castCredits: CastCredits?) {
        if (castCredits == null) {
            return
        }
        val castInfos: MutableList<CastInfo> = ArrayList(castCredits.cast)
        val crewInfos: List<CastInfo> = ArrayList(castCredits.crew)
        crewInfos.firstOrNull { it.job == "Director" }?.let {
            castInfos.add(0, it)
        }

        if (mCastView == null) {
            val rootView = view ?: return
            mCastView = rootView.findViewById(R.id.cast_recyclerview)
            mCastView?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }
        if (mCastAdapter == null) {
            mCastAdapter = CastAdapter(castInfos)
            mCastAdapter?.setClickListener(this)
            mCastView?.adapter = mCastAdapter
        } else {
            mCastAdapter?.apply {
                setCastInfoList(castInfos)
                notifyDataSetChanged()
            }
        }
    }

    override fun onCastClick(castID: String?) {
        movieViewModel.updateFilms(castID)
    }

    override fun onClick(view: View) {
        val id: String
        when (view.id) {
            R.id.movie_genre -> {
                if (view.tag == null) {
                    return
                }
                val genre = view.tag as Genre
                movieViewModel.getFilmsByGenre(genre, mFilm?.isFilm == true)
            }
            R.id.tmdb_view -> {
                id = view.getTag(R.id.id).toString()
                val tvOrMovieString = if (view.getTag(R.id.tvOrMovie) as Boolean) "movie" else "tv"
                goToSite("https://www.themoviedb.org/$tvOrMovieString/$id")
            }
            R.id.imdb_view -> {
                val tvOrMovie = view.getTag(R.id.tvOrMovie) as Boolean
                if (!tvOrMovie) {
                    return
                }
                id = view.getTag(R.id.id).toString()
                goToSite("https://www.imdb.com/title/$id")
            }
            R.id.similar_view -> {
                if (view.tag == null) {
                    return
                }
                val filmID = view.tag as String
                if (mFilm?.isFilm == true) {
                    movieViewModel.getMovieInfo(filmID)
                } else {
                    movieViewModel.getShowInfo(filmID)
                }
            }
        }
    }

    private fun goToSite(site: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(site))
        startActivity(browserIntent)
    }

    companion object {
        private const val TAG_RXERROR = "rxprobMovieDetails"
    }
}