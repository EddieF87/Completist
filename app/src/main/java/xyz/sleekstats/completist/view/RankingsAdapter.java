package xyz.sleekstats.completist.view;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.FilmByPerson;

class RankingsAdapter extends DragItemAdapter<Pair<Long, FilmByPerson>, RankingsAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;
    private final boolean isRanked;
    private ItemClickListener mClickListener;

    RankingsAdapter(ArrayList<Pair<Long, FilmByPerson>> list, boolean ranked) {
        mLayoutId = R.layout.rank_item;
        mGrabHandleId = R.id.grabber;
        mDragOnLongPress = false;
        isRanked = ranked;
        setItemList(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        FilmByPerson film = mItemList.get(position).second;
        holder.bindFilm(film);
        if(isRanked) {
            String positionText = String.valueOf(position+1);
            holder.mRankTextView.setText(positionText);
        }
        holder.itemView.setTag(mItemList.get(position));
    }

    @Override
    public long getUniqueItemId(int position) {
        return mItemList.get(position).first;
    }

    public FilmByPerson getFilmOrShow(int position) {
        return mItemList.get(position).second;
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        private TextView mMovieTextView;
        private TextView mRankTextView;
        private FilmByPerson mFilm;

        ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            mMovieTextView = itemView.findViewById(R.id.text);
            mRankTextView = itemView.findViewById(R.id.grabber);
        }

        private void bindFilm(FilmByPerson film) {
            mFilm = film;
            mMovieTextView.setText(mFilm.getTitle());
        }

        @Override
        public void onItemClicked(View view) {
        }

        @Override
        public boolean onItemLongClicked(View view) {
            if(mClickListener != null) {
                if(mFilm.isFilm()) {
                    mClickListener.onFilmClick(mFilm.getId());
                } else {
                    mClickListener.onShowClick(mFilm.getId());
                }
            }
            return true;
        }
    }


    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener{
        void onFilmClick(String id);
        void onShowClick(String id);
    }

}