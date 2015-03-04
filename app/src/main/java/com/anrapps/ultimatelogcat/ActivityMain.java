package com.anrapps.ultimatelogcat;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.anrapps.ultimatelogcat.adapter.AdapterLog;
import com.anrapps.ultimatelogcat.logcat.Log;
import com.anrapps.ultimatelogcat.logcat.Logcat;
import com.anrapps.ultimatelogcat.util.PrefUtils;
import com.anrapps.ultimatelogcat.util.UIUtils;

import java.lang.ref.WeakReference;
import java.util.List;

import com.anrapps.ultimatelogcat.logcat.Level;
import com.anrapps.ultimatelogcat.logcat.Buffer;
import com.anrapps.ultimatelogcat.logcat.Format;

public class ActivityMain extends ActionBarActivity {
	
	public static final int MAX_LOG_ITEMS = 500;

	private RecyclerView mRecyclerView;
    private AdapterLog mRecyclerAdapter;
	private boolean mAutoScroll = true;

	private Logcat mLogcat;
    private Handler mLogHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if (!PrefUtils.isWizardDone(this) && isApi16OrGreater()) {
			ActivityWizard.start(this, true);
			return;
		}

        setContentView(R.layout.activity_main);

        View toolbarContainer = findViewById(R.id.toolbar_actionbar_container);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);		
		setSupportActionBar(toolbar);
		
		final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
		mRecyclerAdapter = new AdapterLog();

		mRecyclerView = (RecyclerView) findViewById(R.id.activity_main_recyclerview);
        mRecyclerView.setHasFixedSize(true);        
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    if (mRecyclerView.canScrollVertically(1)) mAutoScroll = false;
                return false;
            }
        });
		mRecyclerView.setOnScrollListener(new UIUtils.ScrollManager(
                toolbarContainer != null ? toolbarContainer : toolbar){
				@Override public void onScrolled(RecyclerView r, int dx, int dy) {
                    super.onScrolled(r, dx, dy);
                    if (!r.canScrollVertically(1)) mAutoScroll = true;
                }
				});
		UIUtils.setToolbarTopPadding(mRecyclerView);
    }

	@Override
	protected void onResume() {
		super.onResume();
		//TODO: Get level, buffer, etc from PrefUtils
        if (mLogHandler == null) mLogHandler = new Handler(this);
        if (mLogcat == null) mLogcat = new Logcat(mLogHandler, Level.V, Format.BRIEF, Buffer.MAIN);
		mLogcat.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
        mLogHandler = null;
        if (mLogcat != null) mLogcat.stop();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
			case R.id.action_settings:
				return true;
        	default:
				return super.onOptionsItemSelected(item);
		}
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_VOLUME_UP:
                mAutoScroll = false;
                mRecyclerView.scrollToPosition(mRecyclerAdapter.getItemCount() -1);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                mAutoScroll = true;
                mRecyclerView.scrollToPosition(mRecyclerAdapter.getItemCount() -1);
                return true;
        }
        return false;
    }

    public static void start(Activity from, boolean finish) {
		from.startActivity(new Intent(from, ActivityMain.class));
		if (finish) from.finish();
	}
	
	private boolean isApi16OrGreater() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
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
