package com.anrapps.ultimatelogcat.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.View;
import android.view.LayoutInflater;
import com.anrapps.ultimatelogcat.R;
import android.widget.TextView;

public class AdapterLog extends RecyclerView.Adapter<AdapterLog.ViewHolder> {
	
    private String[] mDataset;

    public AdapterLog(String[] myDataset) {
        mDataset = myDataset;
    }

    @Override
    public AdapterLog.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
			.inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(mDataset[position]);

    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }
}
