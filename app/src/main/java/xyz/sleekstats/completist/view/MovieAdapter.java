package xyz.sleekstats.completist.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.databinding.FilmItemBinding;
import xyz.sleekstats.completist.model.FilmByPerson;

//Load film names and posters for a specific actor/director in a recyclerview
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.FilmViewHolder> {

    private List<FilmByPerson> mCurrentMovieList;
    private ItemClickListener mClickListener;

    public MovieAdapter(List<FilmByPerson> filmByPersonList) {
        this.mCurrentMovieList = filmByPersonList;
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        FilmItemBinding filmItemBinding = FilmItemBinding.inflate(layoutInflater, parent, false);
        return new FilmViewHolder(filmItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        FilmByPerson movie = mCurrentMovieList.get(position);
        holder.bind(movie);
        holder.setPos(position);
    }

    public void setCurrentMovieList(List<FilmByPerson> mCurrentMovieList) {
        this.mCurrentMovieList = mCurrentMovieList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mCurrentMovieList.size();
    }

    class FilmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final CardView mView;
        private int listPos;
        private FilmByPerson mFilm;
        private final FilmItemBinding filmItemBinding;

        public FilmViewHolder(FilmItemBinding binding) {
            super(binding.getRoot());
            this.filmItemBinding = binding;
            mView = (CardView) itemView;
            itemView.findViewById(R.id.title).setOnClickListener(this);
            itemView.findViewById(R.id.poster).setOnClickListener(this);
            itemView.findViewById(R.id.watched_btn).setOnClickListener(this);
            itemView.findViewById(R.id.later_btn).setOnClickListener(this);
            itemView.findViewById(R.id.ignore_btn).setOnClickListener(this);
        }

        public void bind(FilmByPerson film) {
            String id = film.getId();
            mView.setTag(id);
            mFilm = film;
            filmItemBinding.setFilm(film);
            filmItemBinding.executePendingBindings();
        }

        public void setPos(int pos) {
            listPos = pos;
        }


        @Override
        public void onClick(View view) {
            if (mClickListener == null) {
                return;
            }
            String id = String.valueOf(mView.getTag());
            int viewId = view.getId();
            switch (viewId) {
                case R.id.title:
                case R.id.poster:
                    mClickListener.onFilmClick(id);
                    break;
                case R.id.watched_btn:
                    mClickListener.onFilmWatched(listPos);
                    break;
                case R.id.later_btn:
                    mClickListener.onFilmQueued(listPos);
                    break;
                case R.id.ignore_btn:
                    break;
            }
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onFilmClick(String movieID);
        void onFilmWatched(int pos);
        void onFilmQueued(int pos);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
