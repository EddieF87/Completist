package xyz.sleekstats.completist.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.sleekstats.completist.databinding.CastItemBinding
import xyz.sleekstats.completist.model.CastInfo
import xyz.sleekstats.completist.view.CastAdapter.CastViewHolder

//Load director and cast details for a film in a recyclerview
class CastAdapter(private var castInfoList: List<CastInfo>) : RecyclerView.Adapter<CastViewHolder>() {

    private var mClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder =
            CastViewHolder(CastItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    //Load name and poster details for director/cast
    override fun onBindViewHolder(holder: CastViewHolder, position: Int) = holder.bind(castInfoList[position])

    override fun getItemCount(): Int = castInfoList.size

    inner class CastViewHolder(private val castItemBinding: CastItemBinding) : RecyclerView.ViewHolder(castItemBinding.root), View.OnClickListener {

        private val mView: View = castItemBinding.root

        init {
            mView.setOnClickListener(this)
        }

        fun bind(castInfo: CastInfo) {
            mView.tag = castInfo.id
            castItemBinding.cast = castInfo
            castItemBinding.executePendingBindings()
        }

        override fun onClick(view: View) {
            mClickListener?.onCastClick(mView.tag.toString())
        }
    }

    fun setCastInfoList(castInfoList: List<CastInfo>) {
        this.castInfoList = castInfoList
        notifyDataSetChanged()
    }

    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    //When director/actor is clicked, go to their list of films
    interface ItemClickListener {
        fun onCastClick(castID: String?)
    }

}