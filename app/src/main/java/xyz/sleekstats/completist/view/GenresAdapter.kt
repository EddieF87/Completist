package xyz.sleekstats.completist.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xyz.sleekstats.completist.R
import xyz.sleekstats.completist.model.Genre
import xyz.sleekstats.completist.view.GenresAdapter.GenreViewHolder

class GenresAdapter(private val genreList: List<Genre?>) : RecyclerView.Adapter<GenreViewHolder>() {

    private var mClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val frameLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.genre_item, parent, false) as FrameLayout
        return GenreViewHolder(frameLayout)
    }

    //Load name and poster details for director/cast
    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val genre = genreList[position]
        holder.mView.tag = genre
        holder.mView.text = genre?.name
    }

    override fun getItemCount(): Int = genreList.size

    inner class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val mView: TextView = itemView.findViewById(R.id.genre_title)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            mClickListener?.onGenreClick(mView.tag as Genre)
        }
    }

    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onGenreClick(genre: Genre?)
    }

}