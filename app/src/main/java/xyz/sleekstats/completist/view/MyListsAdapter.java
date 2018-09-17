package xyz.sleekstats.completist.view;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.MyList;

public class MyListsAdapter extends RecyclerView.Adapter<MyListsAdapter.ListViewHolder>{

    private List<MyList> mLists;
    private ItemClickListener mClickListener;
    private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w200/";
    private static final NumberFormat f = new DecimalFormat("00");

    public MyListsAdapter(List<MyList> lists) {
        this.mLists = lists;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RelativeLayout linearLayout = (RelativeLayout ) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cast_item, parent, false);
        return new ListViewHolder(linearLayout);
    }

    //Load name and poster details for director/cast
    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        MyList myList = mLists.get(position);
        String posterURL = POSTER_BASE_URL + myList.getList_img();
        String name = myList.getList_name();
        String id = String.valueOf(myList.getList_id());
        int pct = myList.getList_pct();

        Picasso.get().load(posterURL)
                .placeholder(R.drawable.ic_person_92px)
                .error(R.drawable.ic_person_92px)
                .into(holder.mCastPosterView);

        holder.mCastNameView.setText(name);

        if (pct >= 0) {
            String pctString = f.format(pct) + "%";
            holder.mWatchedView.setText(pctString);
            holder.mWatchedView.setVisibility(View.VISIBLE);
        }
        holder.mView.setTag(id);
    }

    @Override
    public int getItemCount() {
        return mLists.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private View mView;
        private ImageView mCastPosterView;
        private TextView mCastNameView;
        private TextView mWatchedView;

        public ListViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mCastPosterView = itemView.findViewById(R.id.cast_poster);
            mCastNameView = itemView.findViewById(R.id.cast_name);
            mWatchedView = itemView.findViewById(R.id.list_pct);
            mView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String id = mView.getTag().toString();
            if (mClickListener != null) mClickListener.onListClick(id);
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    //When director/actor is clicked, go to their list of films
    public interface ItemClickListener{
        void onListClick(String movieID);
    }
}