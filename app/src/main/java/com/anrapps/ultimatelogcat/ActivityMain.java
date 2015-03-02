package com.anrapps.ultimatelogcat;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import com.anrapps.ultimatelogcat.adapter.AdapterLog;
import android.support.v7.widget.Toolbar;
import com.anrapps.ultimatelogcat.util.UIUtils;
import com.anrapps.ultimatelogcat.util.PrefUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.View;

public class ActivityMain extends ActionBarActivity {
	
	private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

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
		
		String[] dataset = new String[1020];
		for (int i = 0; i<200; i++) {
			dataset[i] = "Item " + i;
		}
		
		final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		mAdapter = new AdapterLog(dataset);
		
		mRecyclerView = (RecyclerView) findViewById(R.id.activity_main_recyclerview);
        mRecyclerView.setHasFixedSize(true);        
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.setOnScrollListener(new UIUtils.ScrollManager(toolbarContainer != null ?
                toolbarContainer : toolbar));
		UIUtils.setToolbarTopPadding(mRecyclerView);
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
				PrefUtils.clearWizardDone(this);
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
}
