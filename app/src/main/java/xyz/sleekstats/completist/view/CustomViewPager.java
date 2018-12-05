package xyz.sleekstats.completist.view;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import xyz.sleekstats.completist.R;

public class CustomViewPager extends ViewPager {

    public CustomViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Override of the onInterceptTouchEvent which allows swiping to be disabled when chart is selected
     *
     * @param ev The MotionEvent object
     * @return Call to super if true, otherwise returns false
     */

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int position = this.getCurrentItem();
        Log.d("oooppp", "o = " + position);
        if (position == 3) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }
}

