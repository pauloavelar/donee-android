package me.avelar.donee.util;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.avelar.donee.R;
import me.avelar.donee.model.NavDrawerItem;

public class NavDrawerAdapter extends ArrayAdapter<NavDrawerItem> {

    static class ViewHolder {
        RelativeLayout layout;
        ImageView icon;
        TextView  title;
        TextView  counter;
        View      navigation;
    }

    private int checkedItem;

    public NavDrawerAdapter(Context context) {
        super(context, 0);
    }

    public void addHeader(String title) {
        add(new NavDrawerItem(title, 0, true));
    }

    public void addItem(NavDrawerItem item) {
        add(item);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isHeader ? 0 : 1;
    }

    @Override
    public boolean isEnabled(int position) {
        return !getItem(position).isHeader;
    }

    public void setCheckedItem(int pos) {
        checkedItem = pos;
        this.notifyDataSetChanged();
    }

    public void resetCheck() {
        setCheckedItem(-1);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder     holder;
        View           rowView  = convertView;
        NavDrawerItem  item     = getItem(position);

        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            holder = new ViewHolder();
            if (item.isHeader) {
                rowView = inflater.inflate(R.layout.nav_drawer_header, parent, false);
            } else {
                rowView = inflater.inflate(R.layout.nav_drawer_item, parent, false);
                holder.counter = (TextView)       rowView.findViewById(R.id.counter);
                holder.icon    = (ImageView)      rowView.findViewById(R.id.icon);
                holder.layout  = (RelativeLayout) rowView.findViewById(R.id.ns_menu_row);
                holder.navigation = rowView.findViewById(R.id.view_selected);
            }
            holder.title = (TextView) rowView.findViewById(R.id.title);
            rowView.setTag(holder);
        }
        holder = (ViewHolder) rowView.getTag();

        if (item != null && holder != null) {
            // icon
            if (item.icon != NavDrawerItem.NO_ICON && holder.icon != null) {
                holder.icon.setVisibility(View.VISIBLE);
                holder.icon.setImageResource(item.icon);
            } else if (holder.icon != null) {
                holder.icon.setVisibility(View.GONE);
            }
            // title
            holder.title.setText(item.title);
            // counter
            if (item.counter > 0 && holder.counter != null) {
                holder.counter.setVisibility(View.VISIBLE);
                holder.counter.setText("" + item.counter);
            } else if (holder.counter != null) {
                holder.counter.setVisibility(View.INVISIBLE);
            }
            // checked mark
            if (!item.isHeader) {
                int visibility = (checkedItem == position ? View.VISIBLE : View.INVISIBLE);
                holder.navigation.setVisibility(visibility);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    holder.navigation.setBackgroundResource(R.drawable.menu_item_bg);
                }
            }
        }
        return rowView;
    }

}