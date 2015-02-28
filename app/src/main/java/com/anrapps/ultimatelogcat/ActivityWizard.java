package com.anrapps.ultimatelogcat;

import android.support.v7.app.ActionBarActivity;
import android.os.PersistableBundle;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;
import com.anrapps.ultimatelogcat.util.PrefUtils;
import android.app.FragmentTransaction;
import android.app.FragmentManager;
import android.app.Fragment;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.animation.Animator;
import android.view.animation.DecelerateInterpolator;
import android.view.ViewAnimationUtils;
import android.util.Log;
import com.anrapps.ultimatelogcat.widget.AutoTextView;

public class ActivityWizard extends ActionBarActivity implements OnClickListener {

	private Button buttonSu, buttonAdb;
	
	//TODO: Hide fragment (if attached) onbackpressed
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(0, 0);
		setContentView(R.layout.activity_wizard);
		
		buttonSu = (Button) findViewById(R.id.activitywizardButton2);
		buttonAdb = (Button) findViewById(R.id.activitywizardButton3);
		buttonSu.setOnClickListener(this);
		buttonAdb.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.activitywizardButton2:
				getPermissionViaRoot();
				break;
			case R.id.activitywizardButton3:
				showAdbFragment();
				break;
		}
	}
	
	public static void start(Activity from, boolean finish) {
		from.startActivity(new Intent(from, ActivityWizard.class));
		if (finish) from.finish();
	}
	
	private void getPermissionViaRoot() {
		//TODO
	}
	
	private void showAdbFragment() {
		int[] coords = new int[2];
		buttonAdb.getLocationOnScreen(coords);
		int buttonX = (buttonAdb.getRight() - buttonAdb.getLeft())/2;
		int buttonY = (buttonAdb.getBottom() - buttonAdb.getTop())/2;
		FragmentWizard f = FragmentWizard.newInstance(
				coords[0]+buttonX, 
				coords[1]+buttonY);
		getFragmentManager().beginTransaction()
				.replace(R.id.activity_wizard_fragment_container, f)
				.commit();
	}
	
	public static class FragmentWizard extends Fragment {

		public static FragmentWizard newInstance(int centerX, int centerY) {
			Bundle args = new Bundle();
			args.putInt("cx", centerX);
			args.putInt("cy", centerY);
			FragmentWizard fragment = new FragmentWizard();
			fragment.setArguments(args);
			return fragment;

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_wizard, container, false);

			rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
					@Override
					public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
						v.removeOnLayoutChangeListener(this);
						int cx = getArguments().getInt("cx");
						int cy = getArguments().getInt("cy");
						int radius = (int)Math.hypot(right, bottom);

						Animator reveal = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, radius);
						reveal.setInterpolator(new DecelerateInterpolator(2f));
						reveal.setDuration(1000);
						reveal.start();
					}
				});
				//TODO:Change IDs
			Button doneButton = (Button) rootView.findViewById(R.id.fragmentwizardButton1);
			doneButton.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					ActivityMain.start(getActivity(), true);
					//TODO Set pref: PrefUtils.setWizardDone(getActivity(), true);
				}
			});
				
			AutoTextView autoTextView = (AutoTextView) rootView.findViewById(R.id.fragment_wizard_autotextview);
			autoTextView.setAutoText(R.string.text_adb_command);

			return rootView;
		}		
	}
}
