package com.library.common.widget.draggrid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class DragAdapter<T> extends BaseAdapter {

    protected Context context;

    private List<T> dataList;

    private LayoutInflater inflater;

    private int dragPosition = AdapterView.INVALID_POSITION;

    private OnDataChangedListener listener;

    public DragAdapter(Context context, List<T> list) {
        this.context = context;
        this.dataList = list;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return getItemCount();
    }

    @Override
    public T getItem(int position) {
        if (isPositionValid(position)) {
            return dataList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = createView(position, convertView, parent);
        if (dragPosition == position) {
            view.setVisibility(View.INVISIBLE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
        return view;
    }

    public abstract View createView(int position, View convertView, ViewGroup parent);

    public void addDataItem(T item) {
        if (dataList == null) {
            dataList = new ArrayList<>();
        }
        dataList.add(item);

        notifyDataSetChanged();
        if (listener != null) {
            listener.onDataChanged();
        }
    }

    public void removeDataItem(int position) {
        if (!isPositionValid(position)) {
            return;
        }
        dataList.remove(position);

        notifyDataSetChanged();
        if (listener != null) {
            listener.onDataChanged();
        }
    }

    public void exchange(int dragPostion, int dropPostion) {
        if (!isPositionValid(dragPostion) || !isPositionValid(dropPostion)) {
            return;
        }

        T dragItem = getItem(dragPostion);
        if (dragPostion < dropPostion) {
            dataList.add(dropPostion + 1, dragItem);
            dataList.remove(dragPostion);
        } else {
            dataList.add(dropPostion, dragItem);
            dataList.remove(dragPostion + 1);
        }
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return dataList == null? 0 : dataList.size();
    }

    public boolean isPositionValid(int postion) {
        if (postion >= 0 && postion < getItemCount()) {
            return true;
        }
        return false;
    }

    public LayoutInflater getInflater() {
        return this.inflater;
    }

    public void setDragPosition(int position) {
        this.dragPosition = position;
    }

    class DragHolder {

        public View itemView;

        public DragHolder (View view) {
            this.itemView = view;
        }
    }

    public void setOnDataChangedListener(OnDataChangedListener listener) {
        this.listener = listener;
    }

    public interface OnDataChangedListener {

        void onDataChanged();
    }

}
