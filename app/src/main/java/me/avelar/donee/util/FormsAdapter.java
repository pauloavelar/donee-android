package me.avelar.donee.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import me.avelar.donee.R;
import me.avelar.donee.model.Form;

public class FormsAdapter extends BaseAdapter implements ListAdapter {

    private static final int TYPE_CATEGORY = 0;
    private static final int TYPE_FORM     = 1;

    private class ListItem {
        boolean isCategory;
        String  category;
        Form    form;

        ListItem(String category) {
            this.isCategory = true;
            this.category   = category;
        }

        ListItem(Form form) {
            this.isCategory = false;
            this.form       = form;
        }

    }

    private static class ViewHolder {
        ImageView icon;
        TextView  name;
        TextView  description;
    }

    private Context mContext;
    private ArrayList<ListItem> mItems;
    private ArrayList<Form> mOriginalItems;

    public FormsAdapter(Context context) {
        this(context, null);
    }

    private FormsAdapter(Context context, ArrayList<Form> items) {
        this.mContext = context;
        this.mOriginalItems = items;
        this.mItems = new ArrayList<>();
        replaceForms(context, items);
    }

    public void replaceForms(Context context, ArrayList<Form> items) {
        if (mItems.size() > 0) mItems.clear();
        if (items == null || items.size() == 0) return;

        // sorts the forms by category then name, puts no category forms in the end
        Collections.sort(items, new FormComparator());
        mOriginalItems = items;

        // control variables -- check for no category and when category changed
        boolean noCategoryAdded = false;
        String previousCategory = "";

        // iterate through each form
        for (Form form : items) {
            // if it's a no category form (comparator puts them in the end)
            if ((form.getCategory() == null || form.getCategory().equals("")) && !noCategoryAdded) {
                addCategory(context.getResources().getString(R.string.no_category));
                noCategoryAdded = true;
            }
            // if category name changed -- adds a new separator
            else if (!previousCategory.equalsIgnoreCase(form.getCategory())) {
                addCategory(form.getCategory());
            }
            // stores the previous category for comparisons and adds the form
            previousCategory = form.getCategory();
            addForm(form);
        }
        notifyDataSetChanged();
    }

    private void addCategory(String categoryName) {
        if (categoryName == null) return;
        mItems.add(new ListItem(categoryName.toUpperCase()));
    }

    private void addForm(Form form) {
        if (form == null) return;
        mItems.add(new ListItem(form));
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isCategory ? TYPE_CATEGORY : TYPE_FORM;
    }

    @Override
    public boolean isEnabled(int position) {
        return !getItem(position).isCategory;
    }

    @SuppressWarnings("unused")
    public int getFormCount() {
        int count = 0;
        for (ListItem item : mItems) {
            if (!item.isCategory) count++;
        }
        return count;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public ListItem getItem(int position) {
        return mItems.get(position);
    }

    public Form getItemForm(int position) {
        ListItem item = mItems.get(position);
        if (item.isCategory) return null;
        return item.form;
    }

    @Override
    public long getItemId(int position) {
        ListItem item = mItems.get(position);
        if (item.isCategory) return 0;
        return Long.parseLong(item.form.getId());
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View  rowView = convertView;
        ListItem item = getItem(position);

        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            holder = new ViewHolder();
            if (item.isCategory) {
                rowView = inflater.inflate(R.layout.list_category, parent, false);
                holder.name = (TextView) rowView.findViewById(R.id.category_name);
            } else {
                rowView = inflater.inflate(R.layout.form_list_row, parent, false);
                holder.icon        = (ImageView) rowView.findViewById(R.id.form_row_icon);
                holder.name        = (TextView)  rowView.findViewById(R.id.form_row_name);
                holder.description = (TextView)  rowView.findViewById(R.id.form_row_description);
            }
            rowView.setTag(holder);
        }
        holder = (ViewHolder) rowView.getTag();

        if (item.isCategory) {
            holder.name.setText(item.category);
        } else if (item.form != null) {
            if (item.form.getIconUrl() != null) {
                PhotoCacheLoader.loadFormIcon(mContext, item.form.getIconUrl(), holder.icon);
            } else {
                Picasso.with(mContext).load(R.drawable.form_placeholder).into(holder.icon);
            }
            holder.name.setText(item.form.getName());
            String description = item.form.getDescription();
            if (description == null || description.length() == 0) {
                holder.description.setVisibility(View.GONE);
            } else {
                holder.description.setVisibility(View.VISIBLE);
                holder.description.setText(item.form.getDescription());
            }
        }

        return rowView;
    }

    public ArrayList<Form> getItems() {
        return mOriginalItems;
    }

}