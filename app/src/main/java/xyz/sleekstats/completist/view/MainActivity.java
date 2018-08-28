package xyz.sleekstats.completist.view;

import android.arch.lifecycle.ViewModelProviders;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.ViewGroup;

import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.model.MediaPOJO;
import xyz.sleekstats.completist.viewmodel.MovieViewModel;

public class MainActivity extends AppCompatActivity
        implements MovieListFragment.OnFragmentInteractionListener,
        MovieFragment.OnFragmentInteractionListener {

    private boolean isListView = true;
    private String movieID = "287";
    private String personID = "287";
    private static final String SEARCH_TITLE = "title";
    private static final String SEARCH_ID = "search_id";
    private static final String SEARCH_TYPE = "search_type";

    private ViewPager myViewPager;
    private MyPagerAdapter myPagerAdapter;
    private MovieFragment movieFragment;
    private MovieListFragment movieListFragment;
    private MovieViewModel movieViewModel;
    private SimpleCursorAdapter mSearchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            isListView = savedInstanceState.getBoolean("isListView");
        }
        if (movieViewModel == null) {
            movieViewModel = ViewModelProviders.of(MainActivity.this).get(MovieViewModel.class);
        }

        startPager();
    }


    private void startPager() {
        myViewPager = findViewById(R.id.my_view_pager);
        if (myPagerAdapter == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            myPagerAdapter = new MyPagerAdapter(fragmentManager);
        }
        myViewPager.setOffscreenPageLimit(2);
        myViewPager.setAdapter(myPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.my_tab_layout);
        tabLayout.setupWithViewPager(myViewPager);
        if (!isListView) {
            myViewPager.setCurrentItem(1);
        }
    }

    @Override
    public void onFilmSelected(String movieID) {
        isListView = false;
        this.movieID = movieID;
        myViewPager.setCurrentItem(1);
        movieFragment = (MovieFragment) myPagerAdapter.getItem(1);
        movieFragment.getFilm(movieID);
    }

    @Override
    public void onCastSelected(String castID) {
        isListView = true;
        this.personID = castID;
        myViewPager.setCurrentItem(0);
        movieListFragment = (MovieListFragment) myPagerAdapter.getItem(0);
        movieListFragment.getFilmsForPerson(castID);
    }

    public void onShowSelected(String showID) {
        isListView = false;
        this.movieID = showID;
        myViewPager.setCurrentItem(1);
        movieFragment = (MovieFragment) myPagerAdapter.getItem(1);
        movieFragment.getShow(showID);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isListView", isListView);
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (movieListFragment == null) {
                        movieListFragment = MovieListFragment.newInstance(personID);
                    }
                    return movieListFragment;
                case 1:
                    if (movieFragment == null) {
                        movieFragment = MovieFragment.newInstance(movieID);
                    }
                    return movieFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "List";
                case 1:
                    return "Movie";
                default:
                    return null;
            }
        }

        @NonNull
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);

            switch (position) {
                case 0:
                    movieListFragment = (MovieListFragment) createdFragment;
                    break;
                case 1:
                    movieFragment = (MovieFragment) createdFragment;
                    break;
            }
            return createdFragment;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setIconifiedByDefault(true);

        RxSearchView.queryTextChanges(searchView)
                .skip(1)
                .debounce(600, TimeUnit.MILLISECONDS)
                .filter(charSequence -> !TextUtils.isEmpty(charSequence))
                .map(CharSequence::toString)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .switchMap(query -> movieViewModel.queryMedia(query))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> populateAdapter(s.getResults()));

        final String[] from = new String[]{SEARCH_TITLE, SEARCH_ID};
        final int[] to = new int[]{R.id.search_title};

        if (mSearchAdapter == null) {
            mSearchAdapter = new SimpleCursorAdapter(MainActivity.this, R.layout.search_item,
                    null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        }
        searchView.setSuggestionsAdapter(mSearchAdapter);

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionClick(int position) {

                Cursor cursor = mSearchAdapter.getCursor();
                cursor.moveToPosition(position);

                String id = cursor.getString(cursor.getColumnIndex(SEARCH_ID));
                String type = cursor.getString(cursor.getColumnIndex(SEARCH_TYPE));

                switch (type) {
                    case "person":
                        onCastSelected(id);
                        break;
                    case "movie":
                        onFilmSelected(id);
                        break;
                    default:
                        onShowSelected(id);
                        break;
                }
                return true;
            }

            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }
        });
        return true;
    }

    private void populateAdapter(List<MediaPOJO> mediaPOJOS) {
        if(mediaPOJOS==null) {
            return;
        }
        String[] columns = {
                BaseColumns._ID,
                SEARCH_TITLE,
                SEARCH_ID,
                SEARCH_TYPE
        };

        MatrixCursor cursor = new MatrixCursor(columns);

        for (int i = 0; i < mediaPOJOS.size(); i++) {

            MediaPOJO mediaPOJO = mediaPOJOS.get(i);

            String type = mediaPOJO.getMedia_type();
            String id = mediaPOJO.getId();
            StringBuilder nameBuilder = new StringBuilder();

            if (type.equals("movie")) {
                nameBuilder.append(mediaPOJO.getTitle());
            } else {
                nameBuilder.append(mediaPOJO.getName());
            }
            nameBuilder.append(" (").append(type.toUpperCase()).append(")");
            String name = nameBuilder.toString();

            String[] row = {Integer.toString(i), name, id, type};
            cursor.addRow(row);
        }
        mSearchAdapter.changeCursor(cursor);
    }

}
