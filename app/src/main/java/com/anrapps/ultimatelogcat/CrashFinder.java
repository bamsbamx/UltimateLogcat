package com.anrapps.ultimatelogcat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.anrapps.ultimatelogcat.logcat.Buffer;
import com.anrapps.ultimatelogcat.logcat.Format;
import com.anrapps.ultimatelogcat.logcat.Level;
import com.anrapps.ultimatelogcat.logcat.Log;
import com.anrapps.ultimatelogcat.logcat.LogParser;
import com.anrapps.ultimatelogcat.logcat.LogSaver;
import com.anrapps.ultimatelogcat.logcat.Logcat;
import com.anrapps.ultimatelogcat.util.PrefUtils;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CrashFinder extends Service implements LogSaver.OnLogSavedListener{

    private static final int ID_FOREGROUND_NOTIFICATION = 1010;
    private static final String INTENT_ACTION_SAVE = "com.anrapps.ultimatelogcat.cf.save_logs";
    private static final String INTENT_ACTION_STOP = "com.anrapps.ultimatelogcat.cf.stop_logs";

    private static CrashFinder mInstance = null;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;

    private int foundErrors;
    private String mStartTime;
    private Handler mLogHandler;
    private Logcat mLogcat;
    private final List<Log> mLogList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(ID_FOREGROUND_NOTIFICATION, foregroundNotification());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ACTION_SAVE);
        intentFilter.addAction(INTENT_ACTION_STOP);
        registerReceiver(CFBroadcastReceiver, intentFilter);

        mLogHandler = new Handler(this);
        mLogcat = new Logcat(mLogHandler, Level.E, Format.TIME, Buffer.MAIN);
        mLogcat.start();

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        mStartTime = simpleDateFormat.format(Calendar.getInstance().getTime());

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLogcat.stop();
        mLogHandler = null;
        unregisterReceiver(CFBroadcastReceiver);
        mInstance = null;
    }

    @Override
    public void onLogSaved(final boolean success) {
        mLogHandler.post(() -> Toast.makeText(CrashFinder.this,
                success ? R.string.text_log_saved : R.string.error,
                Toast.LENGTH_SHORT).show());
    }


    public static boolean isRunning() {
        return !(mInstance == null);
    }

    public static void start(Activity from) {
        if (!isRunning())
            from.startService(new Intent(from, CrashFinder.class));
    }

    public static void stop() {
        if (isRunning()) mInstance.stopSelf();
        mInstance = null;
    }

    protected final BroadcastReceiver CFBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(INTENT_ACTION_SAVE)){
                new LogSaver(context, CrashFinder.this).saveLogs(mLogList);
            }else if (intent.getAction().equals(INTENT_ACTION_STOP)){
                mLogcat.stop();
                stop();
            }
        }
    };



    private Notification foregroundNotification() {

        PendingIntent saveContentIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(INTENT_ACTION_SAVE), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent stopContentIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(INTENT_ACTION_STOP), PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.crash_finder))
                .setContentText(getString(R.string.text_looking_for_crashes))
                .setTicker(getString(R.string.text_crash_finder_started))
                .setOnlyAlertOnce(false)
                .setOngoing(true)
                .addAction(R.drawable.ic_stat_action_save, getString(R.string.save), saveContentIntent)
                .addAction(R.drawable.ic_stat_action_close, getString(R.string.stop), stopContentIntent)
                .setSmallIcon(R.drawable.ic_stat_crash_finder)
                .setColor(Level.E.getColor());

        if (PrefUtils.getCFVibrate(this)) mNotificationBuilder.setVibrate(new long[]{0, 75, 75, 75});
        if (PrefUtils.getCFSound(this)) mNotificationBuilder.setDefaults(Notification.DEFAULT_SOUND);

        return mNotificationBuilder.build();
    }

    private static class Processor implements Runnable {

        final CrashFinder crashFinder;
        final List<Log> tLogList;
        private boolean lastLineWasE;

        public Processor(CrashFinder instance, List<Log> logList) {
            this.crashFinder = instance;
            this.tLogList = logList;
        }

        @Override
        public void run() {
            if (LogParser.getTimeStamp(tLogList.get(0)).compareTo(crashFinder.mStartTime) > 0)
                for (Log log : tLogList) {
                    if (log.getLevel() == Level.E)
                        if (log.getMessage().contains("AndroidRuntime")) {
                            crashFinder.mLogList.add(log);
                            if (!lastLineWasE) {
                                crashFinder.foundErrors++;
                                final String message = String.format(
                                        crashFinder.getString(R.string.text_found_crashes), crashFinder.foundErrors);
                                crashFinder.mNotificationBuilder.setContentText(message)
                                        .setTicker(message);
                                crashFinder.mNotificationManager.notify(ID_FOREGROUND_NOTIFICATION,
                                        crashFinder.mNotificationBuilder.build());
                                lastLineWasE = true;
                            }
                        }
                        else lastLineWasE = false;

                }
        }
    }

    private static class Handler extends android.os.Handler {

        private final WeakReference<CrashFinder> mService;

        public Handler(CrashFinder crashFinder) {
            mService = new WeakReference<>(crashFinder);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Logcat.CAT_LOGS:
                    @SuppressWarnings("unchecked")
                        List<Log> catLogs = (List<Log>) msg.obj;
                    CrashFinder crashFinder = mService.get();
                    if (crashFinder != null)
                        new Processor(crashFinder, catLogs).run();
                    break;
            }
        }

    }

}