package xyz.sleekstats.completist.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.FilmByPerson;

//Load film names and posters for a specific actor/director in a recyclerview
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.FilmViewHolder> {

    private List<FilmByPerson> filmByPersonList;
    private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w200/";
    private ItemClickListener mClickListener;

    public MovieAdapter(List<FilmByPerson> filmByPersonList) {
        this.filmByPersonList = filmByPersonList;
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.film_item, parent, false);
        return new FilmViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {

        FilmByPerson filmByPerson = filmByPersonList.get(position);

        String title = filmByPerson.getTitle();
        holder.mTitleView.setText(title);

        String posterPath = filmByPerson.getPoster_path();
        String posterURL = POSTER_BASE_URL + posterPath;

        String id = filmByPerson.getId();
        holder.mView.setTag(id);

        Picasso.get().load(posterURL)
                .placeholder(R.drawable.ic_sharp_movie_92px)
                .error(R.drawable.ic_sharp_movie_92px)
                .into(holder.mPosterView);
    }

    public void setFilmByPersonList(List<FilmByPerson> filmByPersonList) {
        this.filmByPersonList = filmByPersonList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filmByPersonList.size();
    }

    class FilmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private View mView;
        private ImageView mPosterView;
        private TextView mTitleView;

        public FilmViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mTitleView = itemView.findViewById(R.id.title);
            mPosterView = itemView.findViewById(R.id.poster);
            mView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String id = (String) view.getTag();
            if (mClickListener != null) mClickListener.onFilmClick(id);
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener{
        void onFilmClick(String movieID);
    }
}
