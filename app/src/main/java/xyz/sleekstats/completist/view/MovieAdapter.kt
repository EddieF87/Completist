package xyz.sleekstats.completist.view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import xyz.sleekstats.completist.R
import xyz.sleekstats.completist.databinding.FilmItemBinding
import xyz.sleekstats.completist.model.MediaByPerson
import xyz.sleekstats.completist.view.MovieAdapter.FilmViewHolder

//Load film names and posters for a specific actor/director in a recyclerview
class MovieAdapter(private var mCurrentMovieList: List<MediaByPerson>) : RecyclerView.Adapter<FilmViewHolder>() {

    private var mClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder =
            FilmViewHolder(FilmItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        holder.apply {
            bind(mCurrentMovieList[position])
            setPos(position)
        }
    }

    override fun getItemCount(): Int = mCurrentMovieList.size

    inner class FilmViewHolder(private val filmItemBinding: FilmItemBinding) : RecyclerView.ViewHolder(filmItemBinding.root), View.OnClickListener {
        private val mView: CardView = itemView as CardView
        private var listPos = 0
        private var mFilm: MediaByPerson? = null

        init {
            itemView.findViewById<View>(R.id.title).setOnClickListener(this)
            itemView.findViewById<View>(R.id.poster).setOnClickListener(this)
            itemView.findViewById<View>(R.id.watched_btn).setOnClickListener(this)
            itemView.findViewById<View>(R.id.later_btn).setOnClickListener(this)
        }

        fun bind(film: MediaByPerson) {
            val id = film.id
            mView.tag = id
            mFilm = film
            filmItemBinding.film = film
            filmItemBinding.executePendingBindings()
        }

        fun setPos(pos: Int) {
            listPos = pos
        }

        override fun onClick(view: View) {
            mClickListener?.let {
                val id = mView.tag.toString()
                when (view.id) {
                    R.id.title, R.id.poster -> it.onFilmClick(id, mFilm?.isFilm == true)
                    R.id.watched_btn -> it.onFilmWatched(listPos)
                    R.id.later_btn -> it.onFilmQueued(listPos)
                }
            }
        }
    }

    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    override fun getItemId(position: Int): Long = position.toLong()

    interface ItemClickListener {
        fun onFilmClick(movieID: String?, isFilm: Boolean)
        fun onFilmWatched(pos: Int)
        fun onFilmQueued(pos: Int)
    }

}