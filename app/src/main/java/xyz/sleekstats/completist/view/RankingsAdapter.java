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

class RankingsAdapter extends DragItemAdapter<Pair<Long, String>, RankingsAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;
    private final boolean isRanked;

    RankingsAdapter(ArrayList<Pair<Long, String>> list, boolean ranked) {
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
        String movieText = mItemList.get(position).second;
        holder.mMovieTextView.setText(movieText);
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

    class ViewHolder extends DragItemAdapter.ViewHolder {
        private TextView mMovieTextView;
        private TextView mRankTextView;

        ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            mMovieTextView = itemView.findViewById(R.id.text);
            mRankTextView = itemView.findViewById(R.id.grabber);
        }

        @Override
        public void onItemClicked(View view) {
//            Toast.makeText(view.getContext(), "Item clicked", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onItemLongClicked(View view) {
//            Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
    }
}