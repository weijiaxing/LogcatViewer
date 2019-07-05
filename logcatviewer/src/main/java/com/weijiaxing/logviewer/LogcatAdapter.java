package com.weijiaxing.logviewer;


import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class LogcatAdapter extends BaseAdapter implements Filterable {

    private ArrayList<LogItem> mData;
    @Nullable private ArrayList<LogItem> mFilteredData = null;
    @Nullable private String mFilter = null;

    LogcatAdapter() {
        mData = new ArrayList<>();
    }

    void append(LogItem item) {
        synchronized (LogcatAdapter.class) {
            mData.add(item);
            if (mFilter != null && mFilteredData != null) {
                if (!item.isFiltered(mFilter)) {
                    mFilteredData.add(item);
                }
            }
            notifyDataSetChanged();
        }
    }

    void clear() {
        synchronized (LogcatAdapter.class) {
            mData.clear();
            mFilteredData = null;
            notifyDataSetChanged();
        }
    }

    public LogItem[] getData() {
        synchronized (LogcatAdapter.class) {
            return mData.toArray(new LogItem[mData.size()]);
        }
    }

    @Override
    public int getCount() {
        return mFilteredData != null ? mFilteredData.size() : mData.size();
    }

    @Override
    public LogItem getItem(int position) {
        return mFilteredData != null ? mFilteredData.get(position) : mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        Holder holder;
        if (item == null) {
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_logcat, parent, false);
            holder = new Holder(item);
        } else {
            holder = (Holder) item.getTag();
        }
        holder.parse(getItem(position));
        return item;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                synchronized (LogcatAdapter.class) {
                    FilterResults results = new FilterResults();

                    if (constraint == null) {
                        mFilter = null;
                        results.count = mData.size();
                        results.values = null;
                        return results;
                    } else {
                        mFilter = String.valueOf(constraint.charAt(0));
                    }

                    ArrayList<LogItem> filtered = new ArrayList<>();
                    for (LogItem item : mData) {
                        if (!item.isFiltered(mFilter)) {
                            filtered.add(item);
                        }
                    }

                    results.values = filtered;
                    results.count = filtered.size();
                    return results;
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values == null) {
                    mFilteredData = null;
                } else {
                    //noinspection unchecked
                    mFilteredData = (ArrayList<LogItem>) results.values;
                }
                notifyDataSetChanged();
            }
        };
    }

    public static class Holder {

        private static final SimpleDateFormat sDateFormat = new SimpleDateFormat(
                "MM-dd hh:mm:ss.SSS", Locale.getDefault());

        TextView tag;
        TextView time;
        TextView content;

        Holder(View item) {
            tag = item.findViewById(R.id.tag);
            time = item.findViewById(R.id.time);
            content = item.findViewById(R.id.content);
            item.setTag(this);
        }

        void parse(LogItem data) {
            time.setText(String.format(Locale.getDefault(),"%s %d-%d/%s",
                    sDateFormat.format(data.time), data.processId, data.threadId, data.tag));
            content.setText(data.content);
            tag.setText(data.priority);
            tag.setBackgroundResource(data.getColorRes());
        }
    }
}
