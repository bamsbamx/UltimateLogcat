package com.anrapps.ultimatelogcat.logcat;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class LogParser {
	
	private Pattern mLevelPattern;

	public LogParser(Format format){
		this.mLevelPattern = format.getPattern();
	}
	
	public Log parseLine(String line){
		Log log = new Log(line);
		log.setLevel(getLevel(line));
		return log;
	}
	
	private Level getLevel(String line) {
		if (mLevelPattern == null) return Level.U;
		
		Matcher m = mLevelPattern.matcher(line);
		if (m.find()) return Level.valueOf(m.group(1));
					
		//UNKNOWN LEVEL
		return Level.U;
	}

    public static String getTimeStamp(Log log){
        String line = log.getMessage();
        return line.substring(0, 17);

    }
}
