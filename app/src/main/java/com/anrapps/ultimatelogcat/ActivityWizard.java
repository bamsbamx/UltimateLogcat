package com.anrapps.ultimatelogcat;

import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;
import android.app.Fragment;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.anrapps.ultimatelogcat.util.PrefUtils;
import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ActivityWizard extends ActionBarActivity implements OnClickListener {

    private ViewPager mViewPager;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(0, 0);
		setContentView(R.layout.activity_wizard);

        mViewPager = (ViewPager) findViewById(R.id.activity_wizard_viewpager);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setAdapter(new FragmentStatePagerAdapter(getFragmentManager()) {
            @Override public Fragment getItem(int position) {
                return FragmentWizard.newInstance(position);
            }
            @Override public int getCount() {
                return 2;
            }
        });
	}

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 0)
            super.onBackPressed();
        else mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
    }

    @Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.fragment_wizard_intro_root_button:
				getPermissionViaRoot();
				break;
			case R.id.fragment_wizard_intro_adb_button:
				mViewPager.setCurrentItem(1);
				break;
            case R.id.fragment_wizard_done_button:
                PrefUtils.setWizardDone(this, true);
                ActivityMain.start(this, true);
                break;
		}
	}
	
	public static void start(Activity from, boolean finish) {
		from.startActivity(new Intent(from, ActivityWizard.class));
		if (finish) from.finish();
	}
	
	private void getPermissionViaRoot() {
        Command command = new Command(0, ("pm grant " + getPackageName() + " android.permission.READ_LOGS | echo \"logcat\"")) {
            private boolean success = true;
            @Override public void commandOutput(int id, String line) {
                if (line.toLowerCase().contains("exception")) success = false;
                super.commandOutput(id, line);
            }
            @Override public void commandTerminated(int id, String reason) {
                Toast.makeText(ActivityWizard.this, R.string.text_root_error, Toast.LENGTH_LONG).show();
            }
            @Override public void commandCompleted(int id, int exitCode) {
                Toast.makeText(ActivityWizard.this, success ? "success" : "error", Toast.LENGTH_SHORT).show();
                if (success) {
                    PrefUtils.setWizardDone(ActivityWizard.this, true);
                    ActivityMain.start(ActivityWizard.this, true);
                }
                else Toast.makeText(ActivityWizard.this, R.string.text_root_error, Toast.LENGTH_LONG).show();
            }
        };
        try {
            RootShell.getShell(true).add(command);
        } catch (IOException | RootDeniedException | TimeoutException e) {
            Toast.makeText(ActivityWizard.this, R.string.text_root_error, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


	public static class FragmentWizard extends Fragment {

        private static final String BUNDLE_KEY_FRAGMENT_POSITION = "fragment_position";

        private Button buttonSu, buttonAdb, buttonDone;

        public static FragmentWizard newInstance(int position) {
			Bundle args = new Bundle();
			args.putInt(BUNDLE_KEY_FRAGMENT_POSITION, position);
			FragmentWizard fragment = new FragmentWizard();
			fragment.setArguments(args);
			return fragment;
		}

        public FragmentWizard() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final int position = getArguments().getInt(BUNDLE_KEY_FRAGMENT_POSITION);
            final int layoutIdToInflate = position == 0 ?
                    R.layout.fragment_wizard_intro : R.layout.fragment_wizard_adb;
            View rootView = inflater.inflate(layoutIdToInflate, container, false);

            if (position == 0) {
                buttonSu = (Button) rootView.findViewById(R.id.fragment_wizard_intro_root_button);
		        buttonAdb = (Button) rootView.findViewById(R.id.fragment_wizard_intro_adb_button);
                if (getActivity() instanceof OnClickListener) {
                    buttonSu.setOnClickListener((OnClickListener) getActivity());
                    buttonAdb.setOnClickListener((OnClickListener) getActivity());
                }
            } else if (position == 1) {
                buttonDone = (Button) rootView.findViewById(R.id.fragment_wizard_done_button);
                if (getActivity() instanceof OnClickListener)
                    buttonDone.setOnClickListener((OnClickListener) getActivity());
            }
            return rootView;
        }
    }
}
