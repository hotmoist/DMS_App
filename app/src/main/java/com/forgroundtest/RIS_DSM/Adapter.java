package com.forgroundtest.RIS_DSM;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

// universal Adapter in this project.
public class Adapter extends BaseAdapter {
    Context mContext = null;
    LayoutInflater inflater = null;
    ArrayList<SettingData> setting;

    public Adapter(Context context, ArrayList<SettingData> data) {
        mContext = context;
        setting = data;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return setting.size();
    }

    @Override
    public Object getItem(int position) {
        return setting.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.setting, null);

        TextView icon = view.findViewById(R.id.setting_icon);
        TextView menu = view.findViewById(R.id.setting_menu);

        icon.setBackgroundResource(setting.get(position).getIcon());
        menu.setText(setting.get(position).getMenu());

        return view;
    }
}