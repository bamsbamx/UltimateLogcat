package com.anrapps.ultimatelogcat.logcat;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.io.File;
import android.os.Environment;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class LogSaver {
	
	private final Context mContext;
    private final OnLogSavedListener mLogSavedListener;

    public LogSaver(Context context, OnLogSavedListener logSavedListener){
		this.mContext = context;
        this.mLogSavedListener = logSavedListener;
    }

	@SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveLogs(final List<Log> logList) {
		if (!isExternalStorageWriteable()) {
			android.util.Log.e(mContext.getPackageName(), "External storage is not mounted");
			return;
		}

		new Thread(() -> {
            File storage = Environment.getExternalStorageDirectory();
            @SuppressLint("SimpleDateFormat")
                final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd_HH:mm:ss");
            final String fileName = "Log_" + sdf.format(Calendar.getInstance().getTime()) + ".ulc";
            File destiny = new File(storage.toString() + "/UltimateLogcat", fileName);
            destiny.getParentFile().mkdirs();

            try {
                OutputStream os = new FileOutputStream(destiny);
                for (Log log : logList) {
os.write((log.getMessage() + "\n").getBytes());
}
os.close();
mLogSavedListener.onLogSaved(true);
} catch (IOException e) {
mLogSavedListener.onLogSaved(false);
                e.printStackTrace(); }
        }).start();
	}


    public interface OnLogSavedListener {
        void onLogSaved(boolean success);
    }

	private boolean isExternalStorageWriteable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	
	
}
