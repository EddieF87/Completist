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
import xyz.sleekstats.completist.model.PersonPOJO;

public class MyListsAdapter extends RecyclerView.Adapter<MyListsAdapter.ListViewHolder>{

    private List<PersonPOJO> personList;
    private ItemClickListener mClickListener;
    private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w200/";

    public MyListsAdapter(List<PersonPOJO> castInfoList) {
        this.personList = castInfoList;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cast_item, parent, false);
        return new ListViewHolder(constraintLayout);
    }

    //Load name and poster details for director/cast
    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        PersonPOJO person = personList.get(position);
        String posterURL = POSTER_BASE_URL + person.getProfile_path();
        String name = person.getName();
        String id = person.getId();

        Picasso.get().load(posterURL)
                .placeholder(R.drawable.ic_person_92px)
                .error(R.drawable.ic_person_92px)
                .into(holder.mCastPosterView);

        holder.mCastNameView.setText(name);
        holder.mView.setTag(id);
    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private View mView;
        private ImageView mCastPosterView;
        private TextView mCastNameView;

        public ListViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mCastPosterView = itemView.findViewById(R.id.cast_poster);
            mCastNameView = itemView.findViewById(R.id.cast_name);
            mView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String id = mView.getTag().toString();
            if (mClickListener != null) mClickListener.onCastClick(id);
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    //When director/actor is clicked, go to their list of films
    public interface ItemClickListener{
        void onCastClick(String movieID);
    }
}