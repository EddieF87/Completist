package xyz.sleekstats.completist.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import xyz.sleekstats.completist.R;

public class MainActivity extends AppCompatActivity
        implements MovieListFragment.OnFragmentInteractionListener,
        MovieFragment.OnFragmentInteractionListener {

    private static final String TEST_ID = "7467";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        replaceFragment(MovieListFragment.newInstance(TEST_ID)).commit();
    }

    @Override
    public void onFilmSelected(String movieID) {
        replaceFragment(MovieFragment.newInstance(movieID))
                .addToBackStack(null).commit();
    }

    @Override
    public void onCastSelected(String castID) {
        replaceFragment(MovieListFragment.newInstance(castID))
                .addToBackStack(null).commit();
    }

    @SuppressLint("CommitTransaction")
    private FragmentTransaction replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        return fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment);
    }
}
