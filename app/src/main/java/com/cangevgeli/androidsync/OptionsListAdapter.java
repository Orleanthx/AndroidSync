package com.cangevgeli.androidsync;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Created by Can on 24.09.2017.
 */

public class OptionsListAdapter extends BaseAdapter {
    private Activity context;
    private LayoutInflater layoutInflater;
    private String[] optionsList = {"Assigned Ringtone","Unassigned Ringtones","Contact Ringtones"};
    private int size = optionsList.length;

    public OptionsListAdapter(Activity context){
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    };

    @Override
    public int getViewTypeCount() {

        if (getCount() != 0)
            return getCount();

        return 1;
    }

    @Override
    public int getCount() {
        return size;
    }

    @Override
    public Object getItem(int position) {
        return optionsList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        OptionsListHolder viewHolder;

        if (convertView == null){
            LayoutInflater li = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = li.inflate(R.layout.optionslist_item, parent, false);
            viewHolder = new OptionsListHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (OptionsListHolder) v.getTag();
        }

        viewHolder.optionText.setText(optionsList[position]);
        viewHolder.checkBox.setChecked(true);


        return v;
    }

    public class OptionsListHolder {
        public final CheckBox checkBox;
        public final TextView optionText;

        public OptionsListHolder(View base) {
            checkBox = (CheckBox) base.findViewById(R.id.checkBox);
            optionText = (TextView) base.findViewById(R.id.optionText);
        }
    }
}
