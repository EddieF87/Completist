package xyz.sleekstats.completist.view;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.databinding.CastItemBinding;
import xyz.sleekstats.completist.model.CastInfo;
import xyz.sleekstats.completist.model.FilmByPerson;

//Load director and cast details for a film in a recyclerview
public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder>{

    private List<CastInfo> castInfoList;
    private ItemClickListener mClickListener;

    public CastAdapter(List<CastInfo> castInfoList) {
        this.castInfoList = castInfoList;
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        CastItemBinding castItemBinding = CastItemBinding.inflate(layoutInflater, parent, false);
        return new CastViewHolder(castItemBinding);
    }

    //Load name and poster details for director/cast
    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        holder.bind(castInfoList.get(position));
    }

    @Override
    public int getItemCount() {
        return castInfoList.size();
    }

    class CastViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private View mView;
        private final CastItemBinding castItemBinding;

        public CastViewHolder(CastItemBinding binding) {
            super(binding.getRoot());
            this.castItemBinding = binding;
            mView = castItemBinding.getRoot();
            mView.setOnClickListener(this);
        }

        public void bind(CastInfo castInfo) {
            mView.setTag(castInfo.getId());
            castItemBinding.setCast(castInfo);
            castItemBinding.executePendingBindings();
        }


        @Override
        public void onClick(View view) {
            String id = mView.getTag().toString();
            if (mClickListener != null) mClickListener.onCastClick(id);
        }
    }

    public void setCastInfoList(List<CastInfo> castInfoList) {
        this.castInfoList = castInfoList;
        notifyDataSetChanged();
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    //When director/actor is clicked, go to their list of films
    public interface ItemClickListener{
        void onCastClick(String castID);
    }
}
