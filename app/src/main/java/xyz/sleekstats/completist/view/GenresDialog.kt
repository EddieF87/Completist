package xyz.sleekstats.completist.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import xyz.sleekstats.completist.R
import xyz.sleekstats.completist.model.Genre
import xyz.sleekstats.completist.model.GenreList

class GenresDialog : DialogFragment(), GenresAdapter.ItemClickListener {
    private var mGenreSelector: GenreSelector? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = arguments ?: return super.onCreateDialog(savedInstanceState)
        val genresList: GenreList? = bundle.getParcelable(KEY_GENRES)
        val genres = genresList?.genres ?: emptyList()
        @SuppressLint("InflateParams") val rootView = LayoutInflater.from(activity).inflate(R.layout.dialog_genres, null)
        val recyclerView: RecyclerView = rootView.findViewById(R.id.genres_rv)
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        val genresAdapter = GenresAdapter(genres)
        genresAdapter.setClickListener(this)
        recyclerView.adapter = genresAdapter

        val alertDialog = AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_DARK)
                .setView(rootView)
                .setTitle("Genres")
                .setNegativeButton(R.string.back) { dialog: DialogInterface?, _: Int ->
                    dialog?.dismiss()
                }
                .create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
        return alertDialog
    }

    override fun onGenreClick(genre: Genre?) {
        mGenreSelector?.onGenreSelected(genre)
        dismiss()
    }

    fun setGenreSelector(genreSelector: GenreSelector?) {
        mGenreSelector = genreSelector
    }

    interface GenreSelector {
        fun onGenreSelected(genre: Genre?)
    }

    companion object {
        private const val KEY_GENRES = "key_genres"
        fun newInstance(genreList: GenreList?): GenresDialog {
            val args = Bundle()
            val fragment = GenresDialog()
            args.putParcelable(KEY_GENRES, genreList)
            fragment.arguments = args
            return fragment
        }
    }
}