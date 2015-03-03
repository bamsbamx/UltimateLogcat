package com.anrapps.ultimatelogcat.logcat;

public enum Buffer{
	
	MAIN("main"),
	SYSTEM("system"),
	RADIO("radio"),
	EVENTS("events");
	
	private final String mTitle;
	
	private Buffer(String title){
		this.mTitle = title;
	}
	
	public String getTitle(){
		return mTitle;
	}
}
