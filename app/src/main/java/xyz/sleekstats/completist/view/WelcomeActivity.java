package xyz.sleekstats.completist.view;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.ads.MobileAds;

import xyz.sleekstats.completist.R;

public class WelcomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        MobileAds.initialize(this, "ca-app-pub-5443559095909539~1441728975");
    }

    public void goToMain(View view) {
        view.setClickable(false);
        Intent intent;
        if(view.getId() == R.id.tmdb_acknowledgment_imageView) {
            view.setClickable(true);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.themoviedb.org/"));
            startActivity(intent);
        } else {
            intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
