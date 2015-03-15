package com.anrapps.ultimatelogcat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ActivityShortcut extends Activity {

    public static final String ACTION_START_CRASH_FINDER = "start_crash_finder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.getAction().equals(ACTION_START_CRASH_FINDER))
            CrashFinder.start(this);

        intent = new Intent();
        Intent launchIntent = new Intent(this, ActivityShortcut.class);
        launchIntent.setAction(ACTION_START_CRASH_FINDER);
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.shortcut_start_crash_finder));
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher));


        setResult(RESULT_OK, intent);

        finish();

    }
}
