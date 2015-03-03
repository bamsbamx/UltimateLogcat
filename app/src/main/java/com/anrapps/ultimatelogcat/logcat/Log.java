package com.anrapps.ultimatelogcat.logcat;

public class Log{
	
	private String mMessage;
	private Level mLevel;
	
	public Log(String line){
		this.mMessage = line;
	}
	
	public String getMessage(){
		return mMessage;
	}
	
	public Level getLevel(){
		return mLevel;
	}
	
	public void setLevel(Level level){
		this.mLevel = level;
	}
	
}
