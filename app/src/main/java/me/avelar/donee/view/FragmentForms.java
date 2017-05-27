package me.avelar.donee.view;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import me.avelar.donee.R;
import me.avelar.donee.controller.FormsLogic;
import me.avelar.donee.model.Form;
import me.avelar.donee.util.FormsAdapter;
import me.avelar.donee.util.IntentFactory;
import me.avelar.donee.web.DoneeService.RequestStatus;

public class FragmentForms extends Fragment implements Updatable,
                                            View.OnClickListener, ListView.OnItemClickListener {

    private enum ViewState { LOADING, LOADED_EMPTY, LOADED_OK, LOADED_ERROR }
    private enum AnimationDirection { UP, DOWN }

    private static final String STATE_VIEW    = "STATE_VIEW";
    private static final String STATE_FORMS   = "STATE_FORMS";
    private static final String STATE_ERROR   = "STATE_ERROR";
    private static final String STATE_MESSAGE = "STATE_MESSAGE";

    private View mPbLoading, mVwEmpty, mVwError;
    private ListView mLvForms;
    private TextView mTvMessage, mTvErrorMessage;

    private FormsAdapter mFormsAdapter;

    private ViewState mViewState;
    private String mErrorMessage;
    private boolean mRefreshing;

    private final Animation.AnimationListener mErrorListener = new Animation.AnimationListener() {
        AnimationDirection direction;

        @Override
        public void onAnimationStart(Animation animation) {
            if (mTvErrorMessage.getVisibility() == View.INVISIBLE) {
                // set direction and make the view visible
                direction = AnimationDirection.UP;
                mTvErrorMessage.setVisibility(View.VISIBLE);
            } else {
                // set direction and make view unclickable
                direction = AnimationDirection.DOWN;
                mTvErrorMessage.setOnClickListener(null);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            switch (direction) {
                case UP:
                    mTvErrorMessage.setOnClickListener(FragmentForms.this);
                    mTvErrorMessage.postDelayed(new Runnable() {
                        @Override public void run() { setErrorMessage(null); }
                    }, 2000);
                    break;
                case DOWN:
                    mTvErrorMessage.setVisibility(View.INVISIBLE);
                    break;
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) { }
    };

    private BroadcastReceiver mBrForms = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(IntentFactory.EXTRA_ACTION);
            mRefreshing = false;
            if (getActivity() != null) getActivity().invalidateOptionsMenu();
            if (action == null) return;
            switch (IntentFactory.Type.valueOf(action)) {
                case FORMS_LOAD_FINISHED:
                    ArrayList<Form> forms = intent.getParcelableArrayListExtra(IntentFactory.EXTRA_DATA);
                    mFormsAdapter.replaceForms(context, forms);
                    if (forms == null || forms.isEmpty()) {
                        updateViewState(ViewState.LOADED_EMPTY);
                    } else {
                        updateViewState(ViewState.LOADED_OK);
                    }
                    break;
                case FORMS_LOAD_ERROR:
                    String detail = intent.getStringExtra(IntentFactory.EXTRA_DETAIL);
                    String message;
                    if (detail == null) detail = RequestStatus.UNKNOWN_ERROR.toString();
                    switch (RequestStatus.valueOf(detail)) {
                        case NO_CONNECTION:
                            message = getString(R.string.forms_error_internet);
                            break;
                        default: // case SERVER_ERROR: case UNKNOWN_ERROR:
                            message = getString(R.string.forms_error);
                            break;
                    }
                    if (mViewState == ViewState.LOADED_OK) {
                        setErrorMessage(message);
                    } else {
                        updateViewState(ViewState.LOADED_ERROR, message);
                    }
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forms, container, false);

        // view references
        mLvForms   = (ListView) rootView.findViewById(R.id.forms_list_view);
        mPbLoading = rootView.findViewById(R.id.forms_loading);
        mVwEmpty   = rootView.findViewById(R.id.forms_empty);
        mVwError   = rootView.findViewById(R.id.forms_error);
        mTvMessage = (TextView) rootView.findViewById(R.id.forms_error_message);
        mTvErrorMessage = (TextView) rootView.findViewById(R.id.forms_non_intrusive_error);
        rootView.findViewById(R.id.forms_retry).setOnClickListener(this);

        // ListView setup
        mFormsAdapter = new FormsAdapter(getActivity());
        mLvForms.setAdapter(mFormsAdapter);
        mLvForms.setOnItemClickListener(this);

        // registering broadcast receivers
        IntentFilter filter = IntentFactory.createFilter(IntentFactory.ViewReference.FORMS);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBrForms, filter);

        setHasOptionsMenu(true);
        if (savedInstanceState == null) updateSessionData();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_VIEW, mViewState.toString());
        outState.putParcelableArrayList(STATE_FORMS, mFormsAdapter.getItems());
        outState.putString(STATE_MESSAGE, mTvMessage.getText().toString());
        outState.putString(STATE_ERROR, mErrorMessage);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!mRefreshing) {
            inflater.inflate(R.menu.fragment_forms, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mRefreshing = true;
                clickRefreshFromServer();
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String state   = savedInstanceState.getString(STATE_VIEW, ViewState.LOADING.toString());
            String message = savedInstanceState.getString(STATE_MESSAGE);
            setErrorMessage(savedInstanceState.getString(STATE_ERROR, null));
            ArrayList<Form> forms = savedInstanceState.getParcelableArrayList(STATE_FORMS);

            mFormsAdapter.replaceForms(getActivity(), forms);
            updateViewState(ViewState.valueOf(state), message);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBrForms);
        super.onDestroyView();
    }

    @Override
    public void updateSessionData() {
        updateViewState(ViewState.LOADING);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    // this may be triggered with a null activity when the screen is rotated
                    FormsLogic.loadCurrentUserForms(getActivity());
                }
            }
        }).start();
    }

    private void setErrorMessage(String errorMessage) {
        int animationResource = 0;
        mErrorMessage = errorMessage;

        if (errorMessage == null && mTvErrorMessage.getVisibility() == View.VISIBLE) {
            animationResource = R.anim.slide_down;
        } else if (errorMessage != null && mTvErrorMessage.getVisibility() == View.INVISIBLE) {
            mTvErrorMessage.setText(errorMessage);
            animationResource = R.anim.slide_up;
        }

        if (animationResource != 0) {
            Animation anim = AnimationUtils.loadAnimation(getActivity(), animationResource);
            anim.setAnimationListener(mErrorListener);
            mTvErrorMessage.startAnimation(anim);
        }
    }

    private void clickRefreshFromServer() {
        Toast.makeText(getActivity(), R.string.forms_refreshing_list, Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                FormsLogic.loadCurrentUserForms(getActivity(), true);
            }
        }).start();
    }

    private void updateViewState(ViewState newState) {
        updateViewState(newState, null);
    }

    private void updateViewState(ViewState newState, String message) {
        mViewState = newState;
        if (message != null) mTvMessage.setText(message);

        mPbLoading.setVisibility(newState == ViewState.LOADING      ? View.VISIBLE : View.INVISIBLE);
        //mLvForms.setVisibility  (newState == ViewState.LOADED_OK    ? View.VISIBLE : View.INVISIBLE);
        mVwEmpty.setVisibility  (newState == ViewState.LOADED_EMPTY ? View.VISIBLE : View.INVISIBLE);
        mVwError.setVisibility  (newState == ViewState.LOADED_ERROR ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.forms_retry:
                updateSessionData();
                break;
            case R.id.forms_non_intrusive_error:
                setErrorMessage(null);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // check if the clicked element is really a form
        Form clickedForm = mFormsAdapter.getItemForm(position);
        Intent next = new Intent(getActivity(), ActivityCollect.class);
        next.putExtra(FormsLogic.EXTRA_FORM, clickedForm);
        startActivity(next);
    }

}