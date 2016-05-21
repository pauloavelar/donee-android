package me.avelar.donee.view;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;

import java.util.ArrayList;

import me.avelar.donee.R;
import me.avelar.donee.controller.CollectionLogic;
import me.avelar.donee.model.Collection;
import me.avelar.donee.util.CollectionsAdapter;
import me.avelar.donee.util.ConnectivityHelper;
import me.avelar.donee.util.IntentFactory;
import me.avelar.donee.web.SenderIntentService;

public class FragmentCollections extends Fragment implements Updatable, View.OnClickListener,
                                                             DialogInterface.OnClickListener {

    private enum ViewState { LOADING, EMPTY, LOADED }

    private static final String STATE_VIEW = "STATE_VIEW";
    private static final String STATE_COLL = "STATE_COLL";

    private View mPbLoading, mVwEmpty;
    private ListView mLvCollections;
    private CollectionsAdapter mCollectionsAdapter;

    private int mContentType;
    private ViewState mState;

    private BroadcastReceiver mBrCollections = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String  action  = intent.getStringExtra(IntentFactory.EXTRA_ACTION);
            if (action == null) return;

            switch (IntentFactory.Type.valueOf(action)) {
                case COLLECTION_LOADED:
                    boolean deleted = intent.getBooleanExtra(IntentFactory.EXTRA_DETAIL, false);
                    if (deleted) {
                        int messageResId = mContentType == CollectionLogic.DRAFTS ?
                                R.string.delete_draft_success : R.string.delete_outbox_success;
                        Toast.makeText(getActivity(), messageResId, Toast.LENGTH_SHORT).show();
                    }
                    ArrayList<Collection> c = intent.getParcelableArrayListExtra(IntentFactory.EXTRA_DATA);
                    mCollectionsAdapter.replace(context, c);
                    updateViewState(c == null || c.size() == 0 ? ViewState.EMPTY : ViewState.LOADED);
                    break;
                case COLLECTION_SENT:
                    String id = intent.getStringExtra(IntentFactory.EXTRA_DATA);
                    if (id != null) {
                        mCollectionsAdapter.removeByCollectionId(id);
                        if (mCollectionsAdapter.getCount() == 0) updateViewState(ViewState.EMPTY);
                    }
                    break;
            }
            getActivity().invalidateOptionsMenu();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_collections, container, false);

        // view references
        mLvCollections = (ListView) rootView.findViewById(R.id.collections_list_view);
        mPbLoading = rootView.findViewById(R.id.collections_loading);
        mVwEmpty   = rootView.findViewById(R.id.collections_empty);

        // ListView setup
        mCollectionsAdapter = new CollectionsAdapter(getActivity());
        AlphaInAnimationAdapter animationAdapter = new AlphaInAnimationAdapter(mCollectionsAdapter);
        animationAdapter.setAbsListView(mLvCollections);
        mLvCollections.setAdapter(animationAdapter);

        if (getArguments() != null) {
            mContentType = getArguments().getInt(CollectionLogic.EXTRA_CONTENT);
        }

        // registering broadcast receivers
        IntentFilter filter = IntentFactory.createFilter(IntentFactory.ViewReference.COLLECTIONS);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBrCollections, filter);

        setHasOptionsMenu(true);
        if (savedInstanceState == null) updateSessionData();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_VIEW, mState.toString());
        outState.putParcelableArrayList(STATE_COLL, mCollectionsAdapter.getItems());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String state   = savedInstanceState.getString(STATE_VIEW, ViewState.LOADING.toString());
            ArrayList<Collection> c = savedInstanceState.getParcelableArrayList(STATE_COLL);

            mCollectionsAdapter.replace(getActivity(), c);
            updateViewState(ViewState.valueOf(state));
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mState != ViewState.LOADING && mCollectionsAdapter.getCount() > 0) {
            inflater.inflate(R.menu.fragment_collections, menu);
            boolean visible = false;
            if (mContentType == CollectionLogic.OUTBOX) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                visible = !sp.getBoolean(SenderIntentService.ONGOING_SYNC, false);
            }
            menu.findItem(R.id.action_submit).setVisible(visible);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all:
                clickDeleteAll();
                return true;
            case R.id.action_submit:
                clickSubmit();
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBrCollections);
        super.onDestroyView();
    }

    @Override
    public void updateSessionData() {
        updateViewState(ViewState.LOADING);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null) return;
                CollectionLogic.getCollections(getActivity(), mContentType);
            }
        }).start();
    }

    private void clickDeleteAll() {
        new AlertDialog.Builder(getActivity())
            .setTitle(getResources().getString(R.string.confirmation_title))
            .setMessage(getResources().getString(R.string.delete_confirmation_question))
            .setPositiveButton(R.string.yes, this)
            .setNegativeButton(R.string.no, null)
            .create().show();
    }

    private void clickSubmit() {
        if (!ConnectivityHelper.isConnectedToInternet(getActivity())) {
            new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.submit_error))
                .setMessage(getResources().getString(R.string.submit_offline_hint))
                .setNeutralButton(R.string.yes, null)
                .create().show();
        } else if (mCollectionsAdapter.getCount() == 0) {
            Toast.makeText(getActivity(), R.string.sender_no_forms, Toast.LENGTH_LONG).show();
        } else {
            CollectionLogic.sendOutbox(getActivity());
        }
    }

    private void updateViewState(ViewState newState) {
        mState = newState;
        mLvCollections.setVisibility(mState == ViewState.LOADED  ? View.VISIBLE : View.GONE);
        mPbLoading.setVisibility    (mState == ViewState.LOADING ? View.VISIBLE : View.GONE);
        mVwEmpty.setVisibility      (mState == ViewState.EMPTY   ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        updateViewState(ViewState.LOADING);
        new Thread(new Runnable() {
            @Override
            public void run() {
                CollectionLogic.deleteAll(getActivity(), mContentType);
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.forms_retry:
                updateSessionData();
                break;
        }
    }

}
