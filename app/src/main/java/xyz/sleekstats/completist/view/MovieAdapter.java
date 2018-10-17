package xyz.sleekstats.completist.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import xyz.sleekstats.completist.model.MyMovie;

//Load film names and posters for a specific actor/director in a recyclerview
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.FilmViewHolder> {

    private List<MyMovie> mCurrentMovieList;
    private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w92/";
    private ItemClickListener mClickListener;
    private Context mContext;

    public MovieAdapter(List<MyMovie> filmByPersonList, Context context) {
        this.mCurrentMovieList = filmByPersonList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.film_item, parent, false);
        return new FilmViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        MyMovie movie = mCurrentMovieList.get(position);
        holder.setMovie(movie);
        holder.setPos(position);
    }

    public void setCurrentMovieList(List<MyMovie> mCurrentMovieList) {
        this.mCurrentMovieList = mCurrentMovieList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mCurrentMovieList.size();
    }

    class FilmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CardView mView;
        private ImageView mPosterView;
        private ImageView mWatchedView;
        private ImageView mLaterView;
        private ImageView mDeleteView;
        private TextView mTitleView;
        private int listPos;
        private MyMovie myMovie;

        public FilmViewHolder(View itemView) {
            super(itemView);
            mView = (CardView) itemView;
            mTitleView = itemView.findViewById(R.id.title);
            mPosterView = itemView.findViewById(R.id.poster);
            mWatchedView = itemView.findViewById(R.id.watched_btn);
            mLaterView = itemView.findViewById(R.id.later_btn);
            mDeleteView = itemView.findViewById(R.id.ignore_btn);
            mPosterView.setOnClickListener(this);
            mTitleView.setOnClickListener(this);
            mWatchedView.setOnClickListener(this);
            mLaterView.setOnClickListener(this);
            mDeleteView.setOnClickListener(this);
        }

        public void setPos(int pos) {
            listPos = pos;
        }

        public void setMovie(MyMovie movie){
            myMovie = movie;
            String title = movie.getTitle();
            mTitleView.setText(title);

            String posterPath = movie.getPoster();
            String posterURL = POSTER_BASE_URL + posterPath;

            int id = movie.getMovie_id();
            mView.setTag(id);

            Picasso.get().load(posterURL)
                    .placeholder(R.drawable.ic_sharp_movie_92px)
                    .error(R.drawable.ic_sharp_movie_92px)
                    .into(mPosterView);

            int watchType= movie.getWatchType();
            if(watchType < 2) {
                mView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                mWatchedView.setAlpha(.3f);
            } else {
                mView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.colorWatched));
                mWatchedView.setAlpha(1f);
            }
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
                    if(myMovie.getWatchType() < 2) {
                        mClickListener.onFilmChecked(listPos, 2);
                    } else {
                        mClickListener.onFilmChecked(listPos, 1);
                    }
                    break;
                case R.id.later_btn:
                    if(myMovie.getWatchType() < 2) {
                        mClickListener.onFilmChecked(listPos, 2);
                    } else {
                        mClickListener.onFilmChecked(listPos, 1);
                    }
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
        void onFilmChecked(int pos, int watchType);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
