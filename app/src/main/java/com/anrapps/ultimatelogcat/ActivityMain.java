package com.anrapps.ultimatelogcat;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.anrapps.ultimatelogcat.adapter.AdapterLog;
import com.anrapps.ultimatelogcat.logcat.Log;
import com.anrapps.ultimatelogcat.logcat.Logcat;
import com.anrapps.ultimatelogcat.util.PrefUtils;
import com.anrapps.ultimatelogcat.util.UIUtils;

import java.lang.ref.WeakReference;
import java.util.List;

import com.anrapps.ultimatelogcat.logcat.Level;

public class ActivityMain extends ActionBarActivity implements AdapterView.OnItemSelectedListener {
	
	public static final int MAX_LOG_ITEMS = 500;

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private AdapterLog mRecyclerAdapter;
	private boolean mAutoScroll = true;

	private Logcat mLogcat;
    private Handler mLogHandler;

    private boolean mToolbarSpinnerSelected = false;
    private int mToolbarColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if (!PrefUtils.isWizardDone(this) && isApi16OrGreater()) {
			ActivityWizard.start(this, true);
			return;
		}

        setContentView(R.layout.activity_main);

        View toolbarContainer = findViewById(R.id.toolbar_actionbar_container);
		mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        final ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                getSupportActionBar().getThemedContext(),
                R.array.log_levels,
                R.layout.toolbar_spinner_item);
        spinnerAdapter.setDropDownViewResource(R.layout.toolbar_spinner_item_dropdown);

        View spinnerContainer = LayoutInflater.from(this).inflate(R.layout.toolbar_spinner,
                mToolbar, false);
        mToolbar.addView(spinnerContainer, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Spinner spinner = (Spinner) spinnerContainer.findViewById(R.id.actionbar_spinner);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(PrefUtils.getLevel(this).ordinal());
        spinner.setOnItemSelectedListener(this);
		
		final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
		mRecyclerAdapter = new AdapterLog();

		mRecyclerView = (RecyclerView) findViewById(R.id.activity_main_recyclerview);
        mRecyclerView.setHasFixedSize(true);        
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    if (mRecyclerView.canScrollVertically(1)) mAutoScroll = false;
                return false;
            }
        });
        mRecyclerView.setOnScrollListener(new UIUtils.ScrollManager(
                toolbarContainer != null ? toolbarContainer : mToolbar) {
            @Override
            public void onScrolled(RecyclerView r, int dx, int dy) {
                super.onScrolled(r, dx, dy);
                if (!r.canScrollVertically(1)) mAutoScroll = true;
            }
        });

        mToolbarColor = PrefUtils.getLevel(this).getColor();
        mToolbar.setBackgroundColor(mToolbarColor);
		UIUtils.setToolbarTopPadding(mRecyclerView);
    }

	@Override
	protected void onResume() {
		super.onResume();
        if (mLogHandler == null) mLogHandler = new Handler(this);
        if (mLogcat == null) mLogcat = new Logcat(
                mLogHandler,
                PrefUtils.getLevel(this),
                PrefUtils.getFormat(this),
                PrefUtils.getBuffer(this));
        mLogcat.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
        mLogHandler = null;
        if (mLogcat != null) mLogcat.stop();
        mLogcat = null;
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_filter);
        if (searchItem != null) {
            searchItem.setIcon(R.drawable.ic_action_search);
            final SearchView view = (SearchView) searchItem.getActionView();
            if (view != null) {
                view.setOnCloseListener(new SearchView.OnCloseListener() {
                    @Override
                    public boolean onClose() {
                        PrefUtils.removeSearchFilter(ActivityMain.this);
                        mLogcat.setSearchFilter("");
                        return false;
                    }
                });
                view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        view.clearFocus();
                        PrefUtils.setSearchFilter(ActivityMain.this, s);
                        if (mLogcat != null) {
                            mLogcat.setSearchFilter(s);
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });
                final String filter = PrefUtils.getSearchFilter(this);
                if (!TextUtils.isEmpty(filter)) {
                    view.setQuery(filter, true);
                    view.setIconified(false);
                    view.clearFocus();
                }
                view.setQueryHint(getString(R.string.action_filter));
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
			case R.id.action_settings:
                ActivitySettings.start(this, false);
				return true;
            default:
				return super.onOptionsItemSelected(item);
		}
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (!mToolbarSpinnerSelected) {
            mToolbarSpinnerSelected = true;
            return;
        }
        final Level level = Level.get(position);
        PrefUtils.setLevel(this, level);
        mLogcat.setLevel(level);
        changeToolbarBackgroundColor(level.getColor());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    public static void start(Activity from, boolean finish) {
		from.startActivity(new Intent(from, ActivityMain.class));
		if (finish) from.finish();
	}
	
	private boolean isApi16OrGreater() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}

    private void changeToolbarBackgroundColor(final int toColor) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), mToolbarColor, toColor);
        colorAnimation.setDuration(300);
        colorAnimation.setInterpolator(new AccelerateInterpolator());
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animator) {
            mToolbar.setBackgroundColor((Integer) animator.getAnimatedValue());
            }
        });
        colorAnimation.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
            @Override public void onAnimationEnd(Animator animation) {
                mToolbarColor = toColor;
            }
        });
        colorAnimation.start();
    }

    private void updateLogs(final List<Log> logList) {
        final boolean scroll = mAutoScroll;
        int currentSize = mRecyclerAdapter.getItemCount();
        mRecyclerAdapter.addAll(currentSize, logList);
        if (scroll) mRecyclerView.smoothScrollToPosition(mRecyclerAdapter.getItemCount() - 1);
    }

    private static class Handler extends android.os.Handler {

        private final WeakReference<ActivityMain> mActivity;

        public Handler(ActivityMain activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ActivityMain activity = mActivity.get();
            if (activity == null) return;
            switch (msg.what){
                case Logcat.CAT_LOGS:
                    @SuppressWarnings("unchecked")
                        List<Log> catLogs = (List<Log>) msg.obj;
                    activity.updateLogs(catLogs);
                    break;
                case Logcat.CLEAR_LOGS:
                    if (!activity.mRecyclerView.canScrollVertically(-1)) return;
                    if (activity.mRecyclerAdapter.getItemCount() > MAX_LOG_ITEMS)
                        activity.mRecyclerAdapter.removeFirstItems(
                                activity.mRecyclerAdapter.getItemCount() - MAX_LOG_ITEMS);
                    break;
                case Logcat.REMOVE_LOGS:
                    activity.mRecyclerAdapter.clear();
                    break;
            }
        }

    }

}
