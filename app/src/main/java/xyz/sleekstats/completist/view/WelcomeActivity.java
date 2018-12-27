package xyz.sleekstats.completist.view;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.consent.DebugGeography;
import com.google.android.gms.ads.MobileAds;

import java.net.MalformedURLException;
import java.net.URL;

import xyz.sleekstats.completist.R;

public class WelcomeActivity extends AppCompatActivity {

    private ConsentForm form;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        MobileAds.initialize(this, "ca-app-pub-5443559095909539~1441728975");

//        checkForConsent();
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

    private void checkForConsent() {
        Log.d("pcpo", "checkForConsent");
        ConsentInformation consentInformation = ConsentInformation.getInstance(WelcomeActivity.this);
        consentInformation.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        String[] publisherIds = {"pub-5443559095909539"};

        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                switch (consentStatus) {
                    case UNKNOWN:
                        if (ConsentInformation.getInstance(getBaseContext())
                                .isRequestLocationInEeaOrUnknown()) {
                            requestConsent();
                        }
                }
            }
            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
            }
        });
    }

    private void requestConsent() {
        Log.d("pcpo", "requestConsent");
        URL privacyUrl = null;
        try {
            privacyUrl = new URL("https://sites.google.com/site/sleekstatsprivacypolicy/");
        } catch (MalformedURLException e) {
            Log.d("pcpo", "MalformedURLException " + e.getMessage());
            e.printStackTrace();
        }
        form = new ConsentForm.Builder(WelcomeActivity.this, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        Log.d("pcpo", "onConsentFormLoaded");
                        if (form != null) {
                            form.show();
                            Log.d("pcpo", "form.show");
                        }
                    }

                    @Override
                    public void onConsentFormError(String reason) {
                        super.onConsentFormError(reason);
                        Log.d("pcpo", "onConsentFormError  " + reason);
                    }

                    @Override
                    public void onConsentFormOpened() {
                        super.onConsentFormOpened();
                        Log.d("pcpo", "onConsentFormOpened");
                    }

                    @Override
                    public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        super.onConsentFormClosed(consentStatus, userPrefersAdFree);
                        Log.d("pcpo", "onConsentFormClosed");
                    }
                })
                .withAdFreeOption()
                .build();
        form.load();
        Log.d("pcpo", "form.load");
    }
}
