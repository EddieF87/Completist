package xyz.sleekstats.completist.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.ads.consent.*
import com.google.android.gms.ads.MobileAds
import xyz.sleekstats.completist.R
import xyz.sleekstats.completist.view.MainActivity
import java.net.MalformedURLException
import java.net.URL

class WelcomeActivity : AppCompatActivity() {
    private var form: ConsentForm? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        MobileAds.initialize(this, "ca-app-pub-5443559095909539~1441728975")

//        checkForConsent();
    }

    fun goToMain(view: View) {
        view.isClickable = false
        val intent: Intent
        if (view.id == R.id.tmdb_acknowledgment_imageView) {
            view.isClickable = true
            intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.themoviedb.org/"))
            startActivity(intent)
        } else {
            intent = Intent(this@WelcomeActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkForConsent() {
        val publisherIds = arrayOf("pub-5443559095909539")
        ConsentInformation.getInstance(this@WelcomeActivity).apply {
            debugGeography = DebugGeography.DEBUG_GEOGRAPHY_EEA
            requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
                override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                    if (consentStatus == ConsentStatus.UNKNOWN && ConsentInformation.getInstance(baseContext).isRequestLocationInEeaOrUnknown) {
                        requestConsent()
                    }
                }

                override fun onFailedToUpdateConsentInfo(errorDescription: String) {}
            })
        }
    }

    private fun requestConsent() {
        var privacyUrl: URL? = null
        try {
            privacyUrl = URL("https://sites.google.com/site/sleekstatsprivacypolicy/")
        } catch (e: MalformedURLException) {
            Log.d("pcpo", "MalformedURLException " + e.message)
            e.printStackTrace()
        }
        form = ConsentForm.Builder(this@WelcomeActivity, privacyUrl)
                .withListener(object : ConsentFormListener() {
                    override fun onConsentFormLoaded() {
                        if (form != null) {
                            form!!.show()
                        }
                    }
                })
                .withAdFreeOption()
                .build()
        form?.load()
    }
}