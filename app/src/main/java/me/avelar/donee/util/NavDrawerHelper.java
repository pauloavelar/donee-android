package me.avelar.donee.util;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import me.avelar.donee.R;
import me.avelar.donee.controller.CollectionLogic;
import me.avelar.donee.model.NavDrawerItem;
import me.avelar.donee.view.FragmentCollections;
import me.avelar.donee.view.FragmentForms;
import me.avelar.donee.view.FragmentSettings;

@SuppressWarnings("WeakerAccess")
public class NavDrawerHelper {

    public static final int M_FORMS = 1, M_DRAFTS = 2, M_OUTBOX = 3, M_SETTINGS = 5, M_LOGOUT = 6;

    private static final int[] mTitleIds = {
        R.string.nav_category_collect,
        R.string.nav_forms,
        R.string.nav_drafts,
        R.string.nav_outbox,
        R.string.nav_category_more,
        R.string.nav_settings,
        R.string.nav_logout
    };

    public static final int[] iconNavigation = {
        0,  // category
        R.drawable.ic_forms,
        R.drawable.ic_drafts,
        R.drawable.ic_outbox,
        0, // category
        R.drawable.ic_settings,
        R.drawable.ic_logout
    };

    /**
     * Gets the label of a given navigation drawer item
     * @param context The activity in which the items are present
     * @param position The item whose title needs to be fetched
     * @return The title (label) corresponding to the provided item position
     */
    public static String getItemTitle(Context context, int position) {
        if (context == null) return null;
        if (position > mTitleIds.length) return null;
        return context.getResources().getString(mTitleIds[position]);
    }

    public static Fragment getItemFragment(int position) {
        switch (position) {
            case M_FORMS:
                return new FragmentForms();
            case M_DRAFTS: case M_OUTBOX:
                FragmentCollections fc = new FragmentCollections();
                Bundle args = new Bundle();
                if (position == M_DRAFTS) {
                    args.putInt(CollectionLogic.EXTRA_CONTENT, CollectionLogic.DRAFTS);
                } else {
                    args.putInt(CollectionLogic.EXTRA_CONTENT, CollectionLogic.OUTBOX);
                }
                fc.setArguments(args);
                return fc;
            case M_SETTINGS:
                return new FragmentSettings();
            default:
                return null;
        }
    }

    public static NavDrawerAdapter getNavigationAdapter(Context context) {
        NavDrawerAdapter navDrawerAdapter = new NavDrawerAdapter(context);
        String[] menuItems = createMenuItemsArray(context);

        for (int i = 0, len = menuItems.length; i < len; i++) {
            String title = menuItems[i];
            if (iconNavigation[i] == NavDrawerItem.NO_ICON) {
                navDrawerAdapter.addHeader(title.toUpperCase());
            } else {
                NavDrawerItem item = new NavDrawerItem(title, iconNavigation[i]);
                navDrawerAdapter.addItem(item);
            }
        }
        return navDrawerAdapter;
    }

    private static String[] createMenuItemsArray(Context context) {
        String[] menuItems = new String[mTitleIds.length];
        if (context != null) {
            for (int i = 0, len = mTitleIds.length; i < len; i++) {
                menuItems[i] = context.getResources().getString(mTitleIds[i]);
            }
        }
        return menuItems;
    }

}