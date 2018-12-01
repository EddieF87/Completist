package xyz.sleekstats.completist.view;

import android.arch.lifecycle.ViewModelProviders;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.FilmPOJO;
import xyz.sleekstats.completist.viewmodel.MovieViewModel;

public class MainActivity extends AppCompatActivity {

    private static final String SEARCH_TITLE = "title";
    private static final String SEARCH_ID = "search_id";
    private static final String SEARCH_TYPE = "search_type";

    private ViewPager myViewPager;
    private MyPagerAdapter myPagerAdapter;
    private BottomNavigationView mBottomNavigationView;
    private MovieDetailsFragment movieDetailsFragment;
    private MovieListFragment movieListFragment;
    private MyListsFragment myListsFragment;
    private MovieViewModel movieViewModel;
    private SimpleCursorAdapter mSearchAdapter;
    private final CompositeDisposable mainCompositeDisposable = new CompositeDisposable();
    private Disposable mSearchDisposable;

    private PublishSubject<Integer> viewPagerSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (movieViewModel == null) {
            movieViewModel = ViewModelProviders.of(MainActivity.this).get(MovieViewModel.class);
        }
        startPager();
    }
    @Override
    protected void onStart() {
        super.onStart();
        viewPagerSubject = movieViewModel.getViewPagerSubject();
        mainCompositeDisposable.add(viewPagerSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(i -> myViewPager.setCurrentItem(i),
                        e -> Log.e("rxprob", "viewPagerSubject e=" + e.getMessage())
                )
        );
    }

    private void startPager() {
        myViewPager = findViewById(R.id.my_view_pager);
        if (myPagerAdapter == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            myPagerAdapter = new MyPagerAdapter(fragmentManager);
        }
        myViewPager.setOffscreenPageLimit(3);
        myViewPager.setAdapter(myPagerAdapter);

        mBottomNavigationView = findViewById(R.id.bottom_nav);

        mBottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    int i;
                    switch (item.getItemId()) {
                        case R.id.navigation_lists:
                            i = 0;
                            break;
                        case R.id.navigation_actor:
                            i = 1;
                            break;
                        case R.id.navigation_movie:
                            i = 2;
                            break;
                        default:
                            return false;
                    }
                    myViewPager.setCurrentItem(i);
                    return true;
                });

        myViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mBottomNavigationView.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (myListsFragment == null) {
                        myListsFragment = new MyListsFragment();
                    }
                    return myListsFragment;
                case 1:
                    if (movieListFragment == null) {
                        movieListFragment = new MovieListFragment();
                    }
                    return movieListFragment;
                case 2:
                    if (movieDetailsFragment == null) {
                        movieDetailsFragment = new MovieDetailsFragment();
                    }
                    return movieDetailsFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);

            switch (position) {
                case 0:
                    myListsFragment = (MyListsFragment) createdFragment;
                    break;
                case 1:
                    movieListFragment = (MovieListFragment) createdFragment;
                    break;
                case 2:
                    movieDetailsFragment = (MovieDetailsFragment) createdFragment;
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

        mSearchDisposable = RxSearchView
                .queryTextChanges(searchView)
                .skip(1)
                .debounce(600, TimeUnit.MILLISECONDS)
                .filter(charSequence -> !TextUtils.isEmpty(charSequence))
                .map(CharSequence::toString)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .switchMap(query -> movieViewModel.queryMedia(query))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> populateAdapter(s.getResults()),
                        e -> Log.e("rxprob", "RxSearchView.queryTextChanges e=" + e.getMessage())
                );

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
                        movieViewModel.getFilmsByPerson(id);
                        break;
                    case "movie":
                        movieViewModel.getMovieInfo(id);
                        break;
                    default:
                        movieViewModel.getShowInfo(id);
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

    private void populateAdapter(List<FilmPOJO> filmPOJOs) {
        if (filmPOJOs == null) {
            return;
        }
        String[] columns = {
                BaseColumns._ID,
                SEARCH_TITLE,
                SEARCH_ID,
                SEARCH_TYPE
        };

        MatrixCursor cursor = new MatrixCursor(columns);

        for (int i = 0; i < filmPOJOs.size(); i++) {

            FilmPOJO filmPOJO = filmPOJOs.get(i);

            String type = filmPOJO.getMedia_type();
            String id = filmPOJO.getId();
            String name = filmPOJO.getTitle() + " (" + type.toUpperCase() + ")";

            String[] row = {Integer.toString(i), name, id, type};
            cursor.addRow(row);
        }
        mSearchAdapter.changeCursor(cursor);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mainCompositeDisposable.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mSearchDisposable != null) {mSearchDisposable.dispose();}
    }
}
