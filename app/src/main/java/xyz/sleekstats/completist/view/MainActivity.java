package xyz.sleekstats.completist.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import xyz.sleekstats.completist.R;

public class MainActivity extends AppCompatActivity
        implements MovieListFragment.OnFragmentInteractionListener,
        MovieFragment.OnFragmentInteractionListener {

    private static final String TEST_ID = "7467";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = MovieListFragment.newInstance(TEST_ID, true);
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void onFilmSelected(String movieID) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = MovieFragment.newInstance(movieID);
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onCastSelected(String castID, boolean isDirector) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = MovieListFragment.newInstance(castID, isDirector);
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
