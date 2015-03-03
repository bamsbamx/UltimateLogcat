package com.anrapps.ultimatelogcat.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.View;
import android.view.LayoutInflater;
import com.anrapps.ultimatelogcat.R;
import android.widget.TextView;
import java.util.List;
import com.anrapps.ultimatelogcat.logcat.Log;
import java.util.ArrayList;

public class AdapterLog extends RecyclerView.Adapter<AdapterLog.ViewHolder> {
	
    private final List<Log> mLogList;

    public AdapterLog() {
        this.mLogList = new ArrayList<Log>();
    }

    @Override
    public AdapterLog.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
			.inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(getItem(position).getMessage());
		holder.mTextView.setTextColor(getItem(position).getLevel().getColor());
    }

    @Override
    public int getItemCount() {
        return mLogList.size();
    }
	
	public Log getItem(int position) {
		return mLogList.get(position);
	}
	
	public void add(Log log) {
		mLogList.add(log);
		notifyDataSetChanged();
	}
	
	public void removeFirstItems(int count) {
		for (int i=0; i<count; i++) mLogList.remove(0);
		notifyDataSetChanged();
	}
	
	public void clear() {
		mLogList.clear();
		notifyDataSetChanged();
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }
}
