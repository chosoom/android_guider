package com.example.whtna.usingnaver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MyListAdapter extends BaseAdapter{
    Context context;
    ArrayList<list_item> list_itemArrayList;
    TextView addrTextView;
    TextView infoTextView;

    public MyListAdapter(Context context, ArrayList<list_item> list_itemArrayList) {
        this.context = context;
        this.list_itemArrayList = list_itemArrayList;
    }

    @Override
    public int getCount() {
        return this.list_itemArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return list_itemArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.listiem,null);
            addrTextView = (TextView)view.findViewById(R.id.addressText);
            infoTextView = (TextView)view.findViewById(R.id.infoText);
            addrTextView.setText(list_itemArrayList.get(i).getAddress());
            infoTextView.setText(list_itemArrayList.get(i).getInfo());
        }
        return view;
    }
}
