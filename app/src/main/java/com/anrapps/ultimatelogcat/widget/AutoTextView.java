package com.anrapps.ultimatelogcat.widget;
import android.widget.TextView;
import android.content.Context;
import android.os.Handler;

public class AutoTextView extends TextView {
	
	private String mAutoText;
	
	public AutoTextView(android.content.Context context) {
		super(context);
	}

    public AutoTextView(android.content.Context context, android.util.AttributeSet attrs) {
		super(context, attrs);
	}

    public AutoTextView(android.content.Context context, android.util.AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	public void setAutoText(int resId) {
		setText("");
		mAutoText = getContext().getString(resId);
		h.post(new Runnable() {
				@Override
				public void run() {
					setText(mAutoText.subSequence(0, getText().length() +1));
					if (getText().length() < mAutoText.length())
						h.postDelayed(this, 10);
				}
				
			
		});
	}
	
	Handler h = new Handler();
	
}
