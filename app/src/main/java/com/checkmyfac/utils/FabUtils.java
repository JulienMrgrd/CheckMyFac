package com.checkmyfac.utils;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;

import com.checkmyfac.R;

public abstract class FabUtils {

    public static boolean CLOSE_FAB = false;
    public static boolean OPEN_FAB = true;

    public static void animateFab(final Context context, final FloatingActionButton fab, final boolean open) {
        fab.clearAnimation();
        // Scale down animation
        ScaleAnimation shrink =  new ScaleAnimation(1f, 0.5f, 1f, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(100);     // animation duration in milliseconds
        shrink.setInterpolator(new DecelerateInterpolator());
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Change FAB color and icon
                fab.setRotation(180);
                PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 0);
                ObjectAnimator animation_left = ObjectAnimator.ofPropertyValuesHolder(fab, pvhR);
                animation_left.start();
                if(!open){
                    fab.setImageDrawable(context.getResources().getDrawable(R.drawable.dots_vertical, null));
                } else {
                    fab.setImageDrawable(context.getResources().getDrawable(R.drawable.close, null));
                }
                // Scale up animation
                ScaleAnimation expand =  new ScaleAnimation(0.5f, 1f, 0.5f, 1f, Animation.RELATIVE_TO_SELF,
                        0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                expand.setDuration(100);     // animation duration in milliseconds
                expand.setInterpolator(new AccelerateInterpolator());
                fab.startAnimation(expand);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        fab.startAnimation(shrink);
    }
}
