package com.anrapps.ultimatelogcat;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.anrapps.ultimatelogcat.adapter.AdapterLog;
import com.anrapps.ultimatelogcat.logcat.Log;
import com.anrapps.ultimatelogcat.logcat.Logcat;
import com.anrapps.ultimatelogcat.util.PrefUtils;
import com.anrapps.ultimatelogcat.util.UIUtils;
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
	private Handler mLogHandler = new Handler() {
		@Override public void handleMessage(Message msg) {
			switch (msg.what){
                case Logcat.CAT_LOGS:
                    List<Log> catLogs = (List<Log>) msg.obj;
                    updateLogs(catLogs);
                    break;
                case Logcat.CLEAR_LOGS:
					if (mRecyclerAdapter.getItemCount() > MAX_LOG_ITEMS)
                    	mRecyclerAdapter.removeFirstItems(mRecyclerAdapter.getItemCount() - MAX_LOG_ITEMS);
                    break;
				case Logcat.REMOVE_LOGS:
					mRecyclerAdapter.clear();
            }
		}
		
	};

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
		
		final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		mRecyclerAdapter = new AdapterLog();
		
		mRecyclerView = (RecyclerView) findViewById(R.id.activity_main_recyclerview);
        mRecyclerView.setHasFixedSize(true);        
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRecyclerAdapter);
		mRecyclerView.setOnScrollListener(new UIUtils.ScrollManager(toolbarContainer != null ?
                toolbarContainer : toolbar){
				@Override public void onScrolled(RecyclerView r, int dx, int dy) {
					mAutoScroll = !r.canScrollVertically(1);
					}
				});
		UIUtils.setToolbarTopPadding(mRecyclerView);
    }

	@Override
	protected void onResume() {
		super.onResume();
		//TODO: Get level, buffer, etc from PrefUtils
		if (mLogcat == null) mLogcat = new Logcat(mLogHandler, Level.V, Format.BRIEF, Buffer.MAIN);
		mLogcat.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
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
				android.util.Log.wtf("TAG", "Autoscroll menu: " + mAutoScroll);
				return true;	
        	default: 
				return super.onOptionsItemSelected(item);
		}
    }
	
	public static void start(Activity from, boolean finish) {
		from.startActivity(new Intent(from, ActivityMain.class));
		if (finish) from.finish();
	}
	
	private boolean isApi16OrGreater() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}
	
	private void updateLogs(final List<Log> logList){
		for (Log log : logList){
			mRecyclerAdapter.add(log);	
			
			if (mAutoScroll) 
				mRecyclerView.smoothScrollToPosition(mRecyclerAdapter.getItemCount() - 1);
				//mListView.smoothScrollToPosition(mListAdapter.getCount() - 1);			
		}
        //mColorAnimator.startInterpolation(750, 25, mLastActionBarColor, logList.get(logList.size()-1).getLevel().getColor());

		//if (mAutoScroll && logList.size() > 100) mListView.setSelection(mListAdapter.getCount() -1);
	}
	
}
