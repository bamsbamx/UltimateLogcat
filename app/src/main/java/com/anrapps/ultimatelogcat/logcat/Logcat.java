package com.anrapps.ultimatelogcat.logcat;

import android.os.Handler;
import android.os.Message;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import android.os.AsyncTask;

/*
*	Usage: Must create an instance of Logcat via its constructor which requires a Handler. After
*	calling start method a new task will be started. This task will start a proccess using 'logcat' command.
*	Each some time (@see CAT_DELAY) the task will save output lines into a cache and sends the new lines to the 
*	provided handler with the message @see CAT_LOGS. Also it will remove some logs if there are too much of them
*	this way it will send empty message with @see CLEAR_LOGS.
*
*/
public class Logcat {
	
	//CAT: Add new log items
	//CLEAR: Remove first (oldest) log items to avoid memory problems
	//REMOVE: Delete all logs
    public static final int CAT_LOGS = 0;
    private static final long CAT_DELAY = 1;
    public static final int CLEAR_LOGS = 2;
	public static final int REMOVE_LOGS = 3;
	//in milliseconds
	public static final int CLEAR_PERIOD = 20000;

    private final Handler mHandler;
	private LogParser mLogParser;
	private Format mLogFormat;
	private Buffer mLogBuffer;
	private Level mLogLevel;
    private String mLogFilter;

    private long lastCat = -1;
	private long lastClear = - 1;

    private final ArrayList<Log> mLogCache = new ArrayList<Log>();
	private LogcatTask mLogcatTask;
    
    private Runnable catRunner = new Runnable() {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            if (now < lastCat + CAT_DELAY) return;
            lastCat = now;
            if (mLogCache.size() > 0) {
                synchronized (mLogCache) {
                    if (mLogCache.size() > 0) {
                        Message m = Message.obtain(mHandler, CAT_LOGS);
                        m.obj = mLogCache.clone();
                        mLogCache.clear();
                        mHandler.sendMessage(m);
                    }
                }
            }
			if (now - lastClear > CLEAR_PERIOD){
				mHandler.sendEmptyMessage(CLEAR_LOGS);
				lastClear = now;
			}
        }
    };

	/*
	*	Default constructor. Used to initialize a Logcat object
	*
	*	@param handler Handler which will receive log lines
	*	@param level The minimum log level the Logcat will output
	*	@param format The selected format for the logs
	*	@param buffer The selected buffer for the logs
	*/
    public Logcat(Handler handler, Level level, Format format, Buffer buffer) {
        mHandler = handler;
		this.mLogParser = new LogParser(format);
		this.mLogLevel = level;
		this.mLogFormat = format;
		this.mLogBuffer = buffer;
    }
	
	/*
	*	Starts the task which will receive the logs and will send them via the 
	*	provided Handler in the constructor. Also be sure to call stop when not needed (e.g.onPause())
	*
	*	[ I wont do nothing if already running ]
	*/
	public void start(){ 
		if (isRunning()) return;
		mHandler.sendEmptyMessage(REMOVE_LOGS);
		mLogcatTask = new LogcatTask();
		mLogcatTask.execute();
	}
	
	/*
	*	Stops the task started when called start()
	*
	*	[ I wont do nothing if not running ]
	*/
    public void stop() {
        if (!isRunning()) return;
		if (mLogcatTask != null && !mLogcatTask.isCancelled())
			mLogcatTask.cancel();
    }

	/*
	*	Sets the minimum log level for the output logs. This will restart the task so the old logs will be
	*	filtered as well
	*
	*	@param level The minimum level
	*/
	public void setLevel(Level level){
		this.mLogLevel = level;
		if (isRunning()){ 
			mLogcatTask.setOnTaskFinishedListener(new OnTaskFinishedListener(){
					@Override
					public void onTaskFinished() {
						start();
					}
			});
			stop();
		}else{
//			start();
		}
	}

	/*
	 *	Sets a search filter for the output logs. This will restart the task so the old logs will be 
	 *	filtered as well
	 *
	 *	@param searchFilter Filter which will be used to get logs
	 */
    public void setSearchFilter(String searchFilter){
        this.mLogFilter = searchFilter;
        if (isRunning()){
            mLogcatTask.setOnTaskFinishedListener(new OnTaskFinishedListener(){
                @Override
                public void onTaskFinished() {
                    start();
                }
            });
            stop();
        }else{
//            start();
        }
    }
	
	private class LogcatTask extends AsyncTask<Void, Void, Void> {

		Process aProcess;
		BufferedReader aReader;
		ScheduledExecutorService aExecutor;
		
		List<String> aLogCommands;
		OnTaskFinishedListener aOnTaskListener;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			// logcat -v brief -b main -b system *:V
			aLogCommands = new ArrayList<String>();
			aLogCommands.add("logcat");
			aLogCommands.add("-v");
			aLogCommands.add(mLogFormat.getTitle());
			if (mLogBuffer != Buffer.MAIN){
                aLogCommands.add("-b");
                aLogCommands.add(mLogBuffer.getTitle());
            }
            aLogCommands.add("*:" + mLogLevel);

            aExecutor = Executors.newScheduledThreadPool(1);
			aExecutor.scheduleAtFixedRate(catRunner, CAT_DELAY, CAT_DELAY, TimeUnit.SECONDS);
		}

		
		@Override
		protected Void doInBackground(Void[] p1) {
			try {
				aProcess = Runtime.getRuntime()
                    .exec(aLogCommands.toArray(new String[0]));
				aReader = new BufferedReader(new InputStreamReader(
												aProcess.getInputStream()), 1024);

				String line;
				while ((line = aReader.readLine()) != null) {
					if (isCancelled()) break;

					if (line.length() == 0) continue;

                    if (mLogFilter != null)
                        if (!line.contains(mLogFilter)) continue;

                    synchronized (mLogCache) {
						mLogCache.add(mLogParser.parseLine(line));
					}
				}
			} catch (IOException e) {
				android.util.Log.e("UltimateLogcat", "Error reading log => ", e);
			}		
			return null;
		}

		@Override
		protected void onCancelled() {
			if (aOnTaskListener != null)
				aOnTaskListener.onTaskFinished();
		}

		@Override
		protected void onPostExecute(Void result) {
			clearObjects();
		}
		
		public void cancel(){
			cancel(true);
			clearObjects();
		}
		
		public void setOnTaskFinishedListener(OnTaskFinishedListener listener){
			aOnTaskListener = listener;
		}
		
		private void clearObjects(){
			aExecutor.shutdown();
			if (aProcess != null) aProcess.destroy();
			if (aReader != null)
				try { aReader.close(); }
				catch (IOException e) {
					android.util.Log.e("UltimateLogcat", "Error closing stream => ", e);
				}
		}
		
	}
	
	/*
	 *	Show whether the task is running or not
	 *
	 *	@returns true if running, false otherwise
	 */
	public boolean isRunning(){
		return mLogcatTask == null ?
			false :
			mLogcatTask.getStatus().equals(AsyncTask.Status.RUNNING);
	}
	
	private interface OnTaskFinishedListener{
		public void onTaskFinished();
	}
}

