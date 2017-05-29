package me.avelar.donee.util;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import me.avelar.donee.R;
import me.avelar.donee.controller.CollectionLogic;
import me.avelar.donee.controller.FormsLogic;
import me.avelar.donee.dao.CollectionDao;
import me.avelar.donee.model.Collection;
import me.avelar.donee.view.ActivityCollect;

public class CollectionsAdapter extends BaseAdapter implements ListAdapter, View.OnClickListener {

    private static final int TYPE_CATEGORY   = 0;
    private static final int TYPE_COLLECTION = 1;

    private static final String DATE_PATTERN = "yyyyMMdd HHmm";

    private class ListItem {
        boolean    isCategory;
        String     category;
        Collection collection;

        public ListItem(String category) {
            this.isCategory = true;
            this.category   = category;
        }

        public ListItem(Collection collection) {
            this.isCategory = false;
            this.collection = collection;
        }

    }

    static class ViewHolder {
        TextView textView;
        Button btnEdit, btnDelete;
    }

    private Context mContext;
    private ArrayList<ListItem> mItems;
    private ArrayList<Collection> mCollections;

    public CollectionsAdapter(Context context) {
        this(context, null);
    }

    public CollectionsAdapter(Context context, ArrayList<Collection> items) {
        this.mContext = context;
        this.mCollections = items;
        this.mItems = new ArrayList<>();
        replace(context, items);
    }

    public void replace(Context context, ArrayList<Collection> items) {
        if (mItems.size() > 0) mItems.clear();
        if (items == null || items.size() == 0) return;

        // sorts the forms by category then name, puts no category forms in the end
        Collections.sort(items, new CollectionComparator());
        mCollections = items;

        // control variables -- check for no category and when category changed
        boolean noCategoryAdded = false;
        String previousCategory = "";

        // iterate through each form
        for (Collection collection : items) {
            // if it's a collection without form (comparator puts them in the end)
            if (collection.getRelatedForm() == null) {
                if (!noCategoryAdded) {
                    addCategory(context.getResources().getString(R.string.unpublished_forms));
                    noCategoryAdded = true;
                }
            }
            // if category name changed -- adds a new separator
            else if (!previousCategory.equalsIgnoreCase(collection.getRelatedForm().getName())) {
                previousCategory = collection.getRelatedForm().getName();
                addCategory(previousCategory);
            }
            addCollection(collection);
        }
        notifyDataSetChanged();
    }

    public void addCategory(String categoryName) {
        if (categoryName == null) return;
        mItems.add(new ListItem(categoryName.toUpperCase()));
    }

    public void addCollection(Collection collection) {
        if (collection == null) return;
        mItems.add(new ListItem(collection));
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isCategory ? TYPE_CATEGORY : TYPE_COLLECTION;
    }

    @Override
    public boolean isEnabled(int position) {
        return !getItem(position).isCategory;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public ListItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        ListItem item = mItems.get(position);
        if (item.isCategory) return 0;
        return Long.parseLong(item.collection.getLocalId());
    }

    public void removeByCollectionId(String idAsString) {
        if (idAsString == null) return;

        for (int i = 0, len = mItems.size(); i < len; i++) {
            ListItem item = mItems.get(i);
            if (item.isCategory || item.collection == null) continue;
            if (idAsString.equals(item.collection.getLocalId())) {
                removeByPosition(i);
                break;
            }
        }
    }

    private void removeByPosition(int position) {
        if (position >= mItems.size()) return;
        mItems.remove(position--);
        // removes empty category
        if (mItems.get(position).isCategory &&
           (position+1 >= mItems.size() || mItems.get(position+1).isCategory)) {
            mItems.remove(position);
        }
        notifyDataSetChanged();
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
                holder.textView = (TextView) rowView.findViewById(R.id.category_name);
            } else {
                rowView = inflater.inflate(R.layout.collection_list_row, parent, false);
                holder.textView  = (TextView) rowView.findViewById(R.id.collection_date);
                holder.btnEdit   = (Button)   rowView.findViewById(R.id.collection_action_edit);
                holder.btnDelete = (Button)   rowView.findViewById(R.id.collection_action_delete);
                holder.btnEdit.setOnClickListener(this);
                holder.btnDelete.setOnClickListener(this);
            }
            rowView.setTag(holder);
        }
        holder = (ViewHolder) rowView.getTag();

        if (item.isCategory) {
            holder.textView.setText(item.category);
        } else if (item.collection != null) {
            String localized;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                localized = DateFormat.getBestDateTimePattern(Locale.getDefault(), DATE_PATTERN);
            } else {
                localized = "yyyy-MM-dd HH:mm";
            }
            CharSequence dateText = DateFormat.format(localized, item.collection.getSubmittedTime());
            holder.textView.setText(dateText);
            holder.btnEdit.setTag(position);
            holder.btnDelete.setTag(position);
        }

        return rowView;
    }

    public Collection getItemCollection(int position) {
        ListItem item = mItems.get(position);
        return item == null ? null : item.collection;
    }

    public ArrayList<Collection> getItems() {
        return mCollections;
    }

    @Override
    public void onClick(View v) {
        int position = 0;
        try {
            position = (int) v.getTag();
        } catch (Exception ignore) { }
        if (position == 0) return;

        Collection c;
        switch (v.getId()) {
            case R.id.collection_action_edit:
                c = CollectionDao.findComplete(mContext, getItemCollection(position));
                if (c == null) return;
                Intent next = new Intent(mContext, ActivityCollect.class);
                next.putExtra(FormsLogic.EXTRA_FORM, c.getRelatedForm());
                next.putExtra(CollectionLogic.EXTRA_COLLECTION, c);
                mContext.startActivity(next);
                break;
            case R.id.collection_action_delete:
                c = getItemCollection(position);
                if (c != null) {
                    removeByCollectionId(c.getLocalId());
                    CollectionDao.delete(mContext, c);
                }
                break;
        }
    }

}