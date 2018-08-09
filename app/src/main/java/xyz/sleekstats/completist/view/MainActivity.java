package xyz.sleekstats.completist.view;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.ViewGroup;

import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.viewmodel.MovieViewModel;

public class MainActivity extends AppCompatActivity
        implements MovieListFragment.OnFragmentInteractionListener,
        MovieFragment.OnFragmentInteractionListener {


    //    private static final String TEST_ID = "287";
    private boolean isListView = true;
    private String movieID = "287";
    private String personID = "287";
    private ViewPager myViewPager;
    private MyPagerAdapter myPagerAdapter;
    private MovieFragment movieFragment;
    private MovieListFragment movieListFragment;
    private MovieViewModel movieViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null) {
            isListView = savedInstanceState.getBoolean("isListView");
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
//        movieViewModel = ViewModelProviders.of(this).get(MovieViewModel.class);

        TabLayout tabLayout = findViewById(R.id.my_tab_layout);
        tabLayout.setupWithViewPager(myViewPager);
        if(!isListView) {
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
                    if(movieListFragment == null) {
                        movieListFragment = MovieListFragment.newInstance(personID);
                        movieListFragment.getId();
                    }
                    return movieListFragment;
                case 1:
                    if(movieFragment == null) {
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
        return true;
    }
}
