package xyz.sleekstats.completist.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import xyz.sleekstats.completist.R
import xyz.sleekstats.completist.model.MyList
import xyz.sleekstats.completist.view.MyListsAdapter.ListViewHolder
import java.text.DecimalFormat
import java.text.NumberFormat

class MyListsAdapter(private val mLists: List<MyList>) : RecyclerView.Adapter<ListViewHolder>() {
    private var mClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder =
            ListViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.cast_item, parent, false) as CardView)

    //Load name and poster details for director/cast
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val myList = mLists[position]
        val posterURL = POSTER_BASE_URL + myList.list_img
        val name = myList.list_name
        val id = myList.list_id.toString()
        val pct = myList.list_pct
        Picasso.get().load(posterURL)
                .placeholder(R.drawable.ic_person_gold_92dp)
                .error(R.drawable.ic_person_gold_92dp)
                .into(holder.mCastPosterView)
        holder.mCastNameView.text = name
        if (pct >= 0) {
            val pctString = f.format(pct.toLong()) + "%"
            holder.mWatchedView.text = pctString
            holder.mWatchedView.visibility = View.VISIBLE
        }
        holder.mView.tag = id
    }

    override fun getItemCount(): Int = mLists.size

    inner class ListViewHolder(val mView: View) : RecyclerView.ViewHolder(mView), View.OnClickListener {

        init {
            mView.setOnClickListener(this)
        }

        val mCastPosterView: ImageView = mView.findViewById(R.id.cast_poster)
        val mCastNameView: TextView = mView.findViewById(R.id.cast_name)
        val mWatchedView: TextView = mView.findViewById(R.id.list_pct)

        override fun onClick(view: View) {
            mClickListener?.onListClick(mView.tag?.toString())
        }
    }

    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    //When director/actor is clicked, go to their list of films
    interface ItemClickListener {
        fun onListClick(movieID: String?)
    }

    companion object {
        private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w200/"
        private val f: NumberFormat = DecimalFormat("00")
    }

}