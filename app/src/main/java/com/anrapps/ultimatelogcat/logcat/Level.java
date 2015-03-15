package com.anrapps.ultimatelogcat.logcat;

import android.content.Context;
import android.graphics.Color;
import com.anrapps.ultimatelogcat.R;

public enum Level {

    V(0, "#263238", R.string.verbose_title),
    D(1, "#0D47A1", R.string.debug_title),
    I(2, "#1B5E20", R.string.info_title),
    W(3, "#F57F17", R.string.warn_title),
    E(4, "#B71C1C", R.string.error_title),
    F(5, "#4A148C", R.string.assert_title),
	U(6, "#000000", R.string.unknown_title);

    private static Level[] allLevels = new Level[6];

    static {
        allLevels[0] = V;
        allLevels[1] = D;
        allLevels[2] = I;
        allLevels[3] = W;
        allLevels[4] = E;
        allLevels[5] = F;
    }

	private int mId;
    private int mColor;
    private int mTitleId;

    private Level(int id, String hexColor, int titleId) {
        mId = id;
        mColor = Color.parseColor(hexColor);
        mTitleId = titleId;
    }

	public int getValue() {
        return mId;
    }
	
    public int getColor() {
        return mColor;
    }

	public String getTitle(Context context) {
        return context.getString(mTitleId);
    }
    
    public static Level get(int id) {
        return allLevels[id];
    }

    public static Level[] getLevelArray(){
        return allLevels;
    }

    
}
