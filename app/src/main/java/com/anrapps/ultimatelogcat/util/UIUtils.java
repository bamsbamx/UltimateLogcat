package com.anrapps.ultimatelogcat.util;

import android.view.View;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import com.anrapps.ultimatelogcat.R;
import android.support.v7.widget.RecyclerView;
import android.view.animation.DecelerateInterpolator;
import android.animation.Animator;
import android.view.animation.AccelerateInterpolator;
import android.animation.ObjectAnimator;
import android.view.animation.Interpolator;

public class UIUtils {
	
	public static final int HEADER_HIDE_ANIM_DURATION = 200;
	
	private static final int[] RES_IDS_ACTION_BAR_SIZE = { R.attr.actionBarSize };
	
	public static void setToolbarTopPadding(View v) {
        int topPadding = calculateActionBarSize(v.getContext());
        v.setPadding(v.getPaddingLeft(), v.getPaddingTop() + topPadding,
					 v.getPaddingRight(), v.getPaddingBottom());
    }
	
	private static int calculateActionBarSize(Context context) {
        if (context == null) return 0;

        Resources.Theme curTheme = context.getTheme();
        if (curTheme == null) return 0;

        TypedArray att = curTheme.obtainStyledAttributes(RES_IDS_ACTION_BAR_SIZE);
        if (att == null) return 0;

        float size = att.getDimension(0, 0);
        att.recycle();
        return (int) size;
    }

	public static class ScrollManager extends RecyclerView.OnScrollListener {
		
		private int mTotalDy = 0;
		private View mHeaderView;
		private boolean shown = true;
		
		public ScrollManager(View t) {
			this.mHeaderView = t;
		}
		
		@Override
		public void onScrolled(RecyclerView r, int dx, int dy) {
			mTotalDy += dy;
			if (mTotalDy == 0) {
				setActionBarVisibility(mHeaderView, true);
				return;
			}
			
			if (Math.abs(dy) < 5) return;
			setActionBarVisibility(mHeaderView, dy < 0);
		}
		
		private void setActionBarVisibility(View toolbar, boolean visible) {
			if (visible && !shown) {
				runTranslateAnimation(toolbar, 0, new DecelerateInterpolator());
				shown = true;
			} else if (!visible && shown) {
				runTranslateAnimation(toolbar, -toolbar.getBottom(), new AccelerateInterpolator());
				shown = false;
			}
		}
		
		private void runTranslateAnimation(View view, int translateY, Interpolator interpolator) {
			Animator slideInAnimation = ObjectAnimator.ofFloat(view, "translationY", translateY);
			slideInAnimation.setDuration(HEADER_HIDE_ANIM_DURATION);
			slideInAnimation.setInterpolator(interpolator);
			slideInAnimation.start();
		}
	}
}
