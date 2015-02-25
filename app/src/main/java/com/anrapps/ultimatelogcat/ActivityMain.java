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

public class ActivityMain extends ActionBarActivity {
	
	private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		
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
		mRecyclerView.setOnScrollListener(new UIUtils.ScrollManager(toolbar));
		
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
				return true;	
        	default: 
				return super.onOptionsItemSelected(item);
		}
    }
}
