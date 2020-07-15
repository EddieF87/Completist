package xyz.sleekstats.completist.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.util.Pair
import com.woxthebox.draglistview.DragItemAdapter
import xyz.sleekstats.completist.R
import xyz.sleekstats.completist.model.MediaByPerson
import java.util.*

internal class RankingsAdapter(list: ArrayList<Pair<Long, MediaByPerson>>, ranked: Boolean) : DragItemAdapter<Pair<Long, MediaByPerson>, RankingsAdapter.ViewHolder>() {

    private val mLayoutId: Int = R.layout.rank_item
    private val mGrabHandleId: Int = R.id.grabber
    private val mDragOnLongPress: Boolean = false
    private val isRanked: Boolean = ranked
    private var mClickListener: ItemClickListener? = null

    init {
        itemList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(mLayoutId, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.bindFilm(mItemList[position]?.second)
        if (isRanked) {
            val positionText = (position + 1).toString()
            holder.mRankTextView.text = positionText
        }
        holder.itemView.tag = mItemList[position]
    }

    override fun getUniqueItemId(position: Int): Long = mItemList[position]?.first!!

    fun getFilmOrShow(position: Int): MediaByPerson = mItemList[position]?.second!!

    internal inner class ViewHolder(itemView: View) : DragItemAdapter.ViewHolder(itemView, mGrabHandleId, mDragOnLongPress) {

        private val mMovieTextView: TextView = itemView.findViewById(R.id.text)
        val mRankTextView: TextView = itemView.findViewById(R.id.grabber)
        private var mFilm: MediaByPerson? = null

        fun bindFilm(film: MediaByPerson?) {
            mFilm = film
            mMovieTextView.text = mFilm?.title
        }

        override fun onItemClicked(view: View) {}

        override fun onItemLongClicked(view: View): Boolean {
            if (mFilm?.isFilm == true) {
                mClickListener?.onFilmClick(mFilm?.id)
            } else {
                mClickListener?.onShowClick(mFilm?.id)
            }
            return true
        }

    }

    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onFilmClick(id: String?)
        fun onShowClick(id: String?)
    }
}