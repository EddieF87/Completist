package xyz.sleekstats.completist.databinding;

import android.support.v4.content.ContextCompat;

import xyz.sleekstats.completist.R;
import xyz.sleekstats.completist.model.WatchCount;

public class BindingUtils {

    public static String setCollapsingToolBarText(String name, WatchCount watchCount) {
        if (watchCount == null) {
            return name;
        }
        return name + ": " + watchCount.getWatched() + "/" + watchCount.getTotal()
                + " (" + watchCount.getWatchedPct() + "%)";
    }

    public static float setFilmItemButtonAlpha(int watchType) {
        if (watchType < 2) {
            return .3f;
        } else {
            return 1f;
        }
    }

    public static int setFilmItemBackground(int watchType) {
        if (watchType < 2) {
            return R.color.colorAccent;
        } else {
            return R.color.colorWatched;
        }
    }
}
