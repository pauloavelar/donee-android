package me.avelar.monride.view.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import me.avelar.monride.R;

public class EnhancedAdapter extends ArrayAdapter<SpinnerItem> {

    Activity context;
    int layoutId, textViewId;
    SpinnerItem data[] = null;

    public EnhancedAdapter(Activity context, int layoutId, int textViewId, SpinnerItem[] data) {
        super(context, layoutId, textViewId, data);
        this.context    = context;
        this.layoutId   = layoutId;
        this.textViewId = textViewId;
        this.data       = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View       rowView = convertView;
        ViewHolder holder  = null;

        if (rowView == null) {
            rowView = context.getLayoutInflater().inflate(layoutId, parent, false);
            holder  = new ViewHolder();
            holder.id    = (TextView) rowView.findViewById(R.id.spinner_item_id);
            holder.label = (TextView) rowView.findViewById(R.id.spinner_item_label);
            rowView.setTag(holder);
        }
        // fill data
        holder = (ViewHolder) rowView.getTag();
        holder.id.setText(data[position].getStringId());
        holder.label.setText(data[position].toString());
        return rowView;
    }

    public static class ViewHolder { TextView id, label; }

}