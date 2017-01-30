package za.co.inflationcalc.utils;

import android.animation.Animator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import za.co.inflationcalc.R;
import za.co.inflationcalc.utils.callback.FadeInAnimationCompletedCallback;
import za.co.inflationcalc.utils.callback.FadeOutAnimationCompletedCallback;

/**
 * Contains helper methods related to view animations
 * <p/>
 * Created by Laurie on 1/30/2017.
 */

public class AnimationUtil {
    public static void startPopInAnimation(final Context context, final View view) {
        Animation expandIn = AnimationUtils.loadAnimation(context, R.anim.pop_in);
        view.startAnimation(expandIn);
    }

    public static void startPopOutAnimation(final Context context, final View view) {
        Animation expandIn = AnimationUtils.loadAnimation(context, R.anim.pop_out);
        view.startAnimation(expandIn);

        expandIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Ignore
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Ignore
            }
        });
    }

    /**
     * Convenience method for fading the specified view in
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
    public static void fadeViewIn(final View view) {
        fadeViewIn(view, null);
    }

    /**
     * Convenience method for fading the specified view in
     * @param callback Callback that will be called when the animation has completed
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
    public static void fadeViewIn(final View v, @Nullable final FadeInAnimationCompletedCallback callback) {

        if (v.getVisibility() != View.VISIBLE) {

            ViewPropertyAnimator anim;
            anim = v.animate();

            if (anim != null) {
                anim.setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (v.getVisibility() != View.VISIBLE) {
                            LogUtil.d("For some reason the view is not visible after fade in. Setting it to visible.");
                            v.setVisibility(View.VISIBLE);
                        }

                        if (callback != null) {
                            callback.onCompleted();
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });

                v.setAlpha(0);
                v.setVisibility(View.VISIBLE);
                anim.alpha(1);
            } else {
                v.setAlpha(1);
                v.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Convenience method for fading the specified view out
     */
    public static void fadeViewOut(final View v) {
        fadeViewOut(v, 500);
    }

    /**
     * Fade out and hide an image view over a specified duration.
     *
     * @param view     The view that will be faded out
     * @param duration The duration in milliseconds of the fade out.
     */
    private static void fadeViewOut(final View view, final int duration) {
        fadeViewOut(view, duration, null);
    }

    /**
     * Fade out and hide an image view over a specified duration.
     *
     * @param view     The view that will be faded out
     * @param duration The duration in milliseconds of the fade out.
     * @param callback Callback that will be called when the animation has completed
     */
    private static void fadeViewOut(final View view, final int duration, @Nullable final FadeOutAnimationCompletedCallback callback) {
        if (view == null) {
            if (callback != null) {
                callback.onCompleted();
            }
            return;
        }

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(duration);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);

                if (callback != null) {
                    callback.onCompleted();
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        view.startAnimation(fadeOut);
    }
}
