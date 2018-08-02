package xyz.sleekstats.completist.view;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.CastInfo;

//Load director and cast details for a film in a recyclerview
public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder>{

    private List<CastInfo> castInfoList;
    private ItemClickListener mClickListener;
    private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w200/";

    public CastAdapter(List<CastInfo> castInfoList) {
        this.castInfoList = castInfoList;
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cast_item, parent, false);
        return new CastViewHolder(constraintLayout);
    }

    //Load name and poster details for director/cast
    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        CastInfo castInfo = castInfoList.get(position);
        String posterURL = POSTER_BASE_URL + castInfo.getProfile_path();
        String name = castInfo.getName();
        String id = castInfo.getId();

        Picasso.get().load(posterURL)
                .placeholder(R.drawable.ic_sharp_account_box_24px)
                .error(R.drawable.ic_sharp_account_box_24px)
                .into(holder.mCastPosterView);

        holder.mCastNameView.setText(name);
        holder.mView.setTag(id);
    }

    @Override
    public int getItemCount() {
        return castInfoList.size();
    }

    class CastViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private View mView;
        private ImageView mCastPosterView;
        private TextView mCastNameView;

        public CastViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mCastPosterView = itemView.findViewById(R.id.cast_poster);
            mCastNameView = itemView.findViewById(R.id.cast_name);
            mView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String id = mView.getTag().toString();
            if (mClickListener != null) mClickListener.onCastClick(id, false);
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    //When director/actor is clicked, go to their list of films
    public interface ItemClickListener{
        void onCastClick(String movieID, boolean isDirector);
    }
}
