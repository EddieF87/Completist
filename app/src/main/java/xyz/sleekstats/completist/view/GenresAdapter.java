package xyz.sleekstats.completist.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.Genre;

public class GenresAdapter  extends RecyclerView.Adapter<GenresAdapter.GenreViewHolder>{

    private final List<Genre> genreList;
    private ItemClickListener mClickListener;

    public GenresAdapter(List<Genre> genres) {
        this.genreList = genres;
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.genre_item, parent, false);
        return new GenreViewHolder(frameLayout);
    }

    //Load name and poster details for director/cast
    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        Genre genre = genreList.get(position);
        holder.mView.setTag(genre);
        holder.mView.setText(genre.getName());
    }

    @Override
    public int getItemCount() {
        return genreList.size();
    }

    class GenreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mView;

        public GenreViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView.findViewById(R.id.genre_title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Genre genre = (Genre) mView.getTag();
            if (mClickListener != null) mClickListener.onGenreClick(genre);
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener{
        void onGenreClick(Genre genre);
    }
}
