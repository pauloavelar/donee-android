package me.avelar.donee.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.animation.Animation.AnimationListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import me.avelar.donee.R;
import me.avelar.donee.controller.SessionManager;
import me.avelar.donee.controller.WelcomeLogic;
import me.avelar.donee.dao.UserDao;
import me.avelar.donee.model.User;
import me.avelar.donee.util.IntentFactory;
import me.avelar.donee.util.NavDrawerAdapter;
import me.avelar.donee.util.NavDrawerHelper;
import me.avelar.donee.util.NotificationFactory;
import me.avelar.donee.util.PhotoCacheLoader;
import me.avelar.donee.util.UserAdapter;
import me.avelar.donee.web.SenderIntentService;
import me.avelar.donee.web.UrlRepository;

public class ActivityMain extends AppCompatActivity implements View.OnClickListener,
                        AdapterView.OnItemClickListener, DialogInterface.OnClickListener {

    private static final String STATE_FORM = "FORM";

    private ActionBarDrawerToggle mDrawerToggle;
    private Fragment mFragment;

    private DrawerLayout mDrawerLayout;

    private ActionBar mActionBar;
    private ImageView mIvPhoto;
    private TextView  mTvName, mTvAccount;
    private View      mNavDrawer, mUserListLayout;

    private NavDrawerAdapter mMenuAdapter;
    private UserAdapter      mUserAdapter;

    private String mMenuTitle;
    private String mAppTitle;
    private int    mPreviousPosition;

    private AlertDialog mLogoutConfirmation;

    private BroadcastReceiver brMain = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(IntentFactory.EXTRA_ACTION);
            switch (IntentFactory.Type.valueOf(action)) {
                case MAIN_GO_TO_LOGIN:
                    boolean clearTask = intent.getBooleanExtra(IntentFactory.EXTRA_DETAIL, false);
                    showLoginActivity(clearTask);
                    break;
                case MAIN_REFRESH_USER:
                    loadCurrentUser();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAppTitle = getResources().getString(R.string.app_name);
        checkLastSyncIssue();
        createLogoutDialog();
        SessionManager.validateCurrent(this);

        // getting references for the ListViews
        ListView mUserListView = (ListView) findViewById(R.id.nav_users);
        ListView mMenuListView = (ListView) findViewById(R.id.nav_menus);

        // getting references for important Views
        FrameLayout mCurrentUser = (FrameLayout) findViewById(R.id.current_user);
        mUserListLayout = findViewById(R.id.nav_users_layout);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavDrawer = findViewById(R.id.nav_drawer);
        mActionBar = getSupportActionBar();

        // setting up Navigation Drawer hamburger button
        if (mActionBar != null) {
            mActionBar.setHomeButtonEnabled(true);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // setting up the Navigation Drawer layout and behavior
        mDrawerToggle = new DrawerToggle(this, mDrawerLayout,
                                         R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        // creating current user layout card
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.user_card, mCurrentUser);
        mTvName    = (TextView)  mCurrentUser.findViewById(R.id.user_name);
        mTvAccount = (TextView)  mCurrentUser.findViewById(R.id.user_account);
        mIvPhoto   = (ImageView) mCurrentUser.findViewById(R.id.user_photo);
        mCurrentUser.findViewById(R.id.user_expand).setVisibility(View.VISIBLE);

        // setting up Navigation Drawer menu
        mMenuAdapter = NavDrawerHelper.getNavigationAdapter(this);
        mMenuListView.setAdapter(mMenuAdapter);
        mMenuListView.setOnItemClickListener(this);

        // setting up Navigation Drawer user list
        mCurrentUser.setOnClickListener(this);
        mUserAdapter = new UserAdapter(this, R.layout.user_card);
        mUserListView.setAdapter(mUserAdapter);
        mUserListView.setOnItemClickListener(this);
        mUserListView.setEmptyView(findViewById(R.id.user_list_empty));
        loadCurrentUser();
        findViewById(R.id.user_add).setOnClickListener(this);

        if (getIntent().getBooleanExtra(NotificationFactory.EXTRA_OUTBOX, false)) {
            setFragment(NavDrawerHelper.M_OUTBOX);
        } else if (savedInstanceState != null) {
            setFragment(savedInstanceState.getInt(STATE_FORM, NavDrawerHelper.M_FORMS));
        } else {
            setFragment(NavDrawerHelper.M_FORMS);
        }

        // registering the BroadcastReceiver
        IntentFilter mainFilter = IntentFactory.createFilter(IntentFactory.ViewReference.MAIN);
        LocalBroadcastManager.getInstance(this).registerReceiver(brMain, mainFilter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    private void checkLastSyncIssue() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean ongoingSync = sp.getBoolean(SenderIntentService.ONGOING_SYNC, false);
        long msFromLastSync = new Date().getTime() - sp.getLong(SenderIntentService.LAST_SYNC, 0);
        if (ongoingSync && msFromLastSync > SenderIntentService.TWO_MINUTES) {
            sp.edit().putBoolean(SenderIntentService.ONGOING_SYNC, false).apply();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_FORM, mPreviousPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggleDrawer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(brMain);
        super.onDestroy();
    }

    public void toggleDrawer() {
        if (mDrawerLayout.isDrawerOpen(mNavDrawer)) {
            mDrawerLayout.closeDrawer(mNavDrawer);
        } else {
            mDrawerLayout.openDrawer(mNavDrawer);
        }
    }

    private void setFragment(int fragmentId) {
        mPreviousPosition = fragmentId;
        // adjusting navigation drawer
        mMenuAdapter.resetCheck();
        mFragment = NavDrawerHelper.getItemFragment(mPreviousPosition);
        mMenuTitle = NavDrawerHelper.getItemTitle(this, mPreviousPosition);
        mMenuAdapter.setCheckedItem(mPreviousPosition);
        setActionBarTitle(mMenuTitle);
        // loading the fragment into view
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.main_container, mFragment).commit();

    }

    private void loadCurrentUser() {
        User user = SessionManager.getLastSession(this).getUser();
        mTvName.setText(user.getName());
        mTvAccount.setText(user.getAccount());
        PhotoCacheLoader.loadUserPhoto(this, UrlRepository.getUserPhotoUrl(user.getId()), mIvPhoto);
        mUserAdapter.clear();
        mUserAdapter.addAll(UserDao.getOthers(this));
        mUserAdapter.notifyDataSetChanged();
        if (mFragment instanceof Updatable) {
            ((Updatable) mFragment).updateSessionData();
        }
    }

    private void setActionBarTitle(String title) {
        if (mActionBar != null) mActionBar.setTitle(title);
    }

    private void toggleUserListVisibility() {
        int v = mUserListLayout.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE;
        setUserListVisibility(v);
    }

    private void setUserListVisibility(int visibility) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Animation anim;
            switch (visibility) {
                case View.VISIBLE:
                    anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
                    mUserListLayout.setVisibility(View.VISIBLE);
                    mUserListLayout.startAnimation(anim);
                    break;
                case View.GONE:
                case View.INVISIBLE:
                    anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
                    anim.setAnimationListener(new AnimationListener() {
                        @Override public void onAnimationStart(Animation animation) {}
                        @Override public void onAnimationRepeat(Animation animation) {}
                        @Override public void onAnimationEnd(Animation animation) {
                            mUserListLayout.setVisibility(View.INVISIBLE);
                        }
                    });
                    mUserListLayout.startAnimation(anim);
                    break;
            }

        } else setUserListVisibilityV21(visibility);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setUserListVisibilityV21(int visibility) {
        if (mUserListLayout.getVisibility() == visibility) return;

        Animator anim;
        int cx     = mUserListLayout.getWidth() / 2;
        int radius = Math.max(cx, mUserListLayout.getHeight());

        switch (visibility) {
            case View.VISIBLE:
                anim = ViewAnimationUtils
                        .createCircularReveal(mUserListLayout, cx, -cx, cx, radius + cx);
                mUserListLayout.setVisibility(View.VISIBLE);
                anim.start();
                break;
            case View.INVISIBLE: case View.GONE:
                anim = ViewAnimationUtils
                        .createCircularReveal(mUserListLayout, cx, -cx, radius + cx, cx);
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mUserListLayout.setVisibility(View.INVISIBLE);
                    }
                });
                anim.start();
                break;
        }
    }

    private void showLoginActivity(User user, boolean clearTask) {
        Intent intent = new Intent(this, ActivityWelcome.class);
        if (user != null) {
            intent.putExtra(WelcomeLogic.INTENT_EMAIL, user.getEmail());
        }
        if (clearTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        startActivity(intent);
    }

    private void showLoginActivity() {
        showLoginActivity(null, false);
    }

    private void showLoginActivity(boolean clearTask) {
        showLoginActivity(null, clearTask);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.current_user:
                toggleUserListVisibility();
                break;
            case R.id.user_add:
                showLoginActivity();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.nav_menus: // switching to a different fragment
                if (position == mPreviousPosition) return;
                mMenuTitle = NavDrawerHelper.getItemTitle(this, position);
                mMenuAdapter.setCheckedItem(position);
                mDrawerLayout.closeDrawer(mNavDrawer);
                clickNavDrawerMenu(position);
                break;
            case R.id.nav_users: // switching to a different user
                Object obj = parent.getItemAtPosition(position);
                if (obj instanceof User) {
                    if (SessionManager.switchTo(this, (User) obj)) {
                        loadCurrentUser();
                        if (mFragment instanceof Updatable) {
                            SessionManager.updateFragmentState((Updatable)mFragment);
                        }
                    } else showLoginActivity((User) obj, false);
                }
                setUserListVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int whichButton) {
        SessionManager.logoutCurrentSession(ActivityMain.this, true);
        String userName = SessionManager.getCurrentUserName(ActivityMain.this);
        if (userName == null) return;

        SessionManager.getLastSession(ActivityMain.this);
        String message = getResources().getString(R.string.switched_to) + " " + userName;
        Toast.makeText(ActivityMain.this, message, Toast.LENGTH_LONG).show();
    }

    private void createLogoutDialog() {
        mLogoutConfirmation = new AlertDialog.Builder(this)
            .setTitle(getResources().getString(R.string.confirmation_title))
            .setMessage(getResources().getString(R.string.logout_confirmation_question))
            .setPositiveButton(R.string.yes, this)
            .setNegativeButton(R.string.no, null)
            .create();
    }

    private void clickNavDrawerMenu(int position) {
        if (position == NavDrawerHelper.M_LOGOUT) {
            mLogoutConfirmation.show();
            mMenuTitle = NavDrawerHelper.getItemTitle(this, mPreviousPosition);
            mMenuAdapter.setCheckedItem(mPreviousPosition);
        } else updateFragment(position);
    }

    private void updateFragment(int position) {
        if (position == mPreviousPosition) return;

        mPreviousPosition = position;
        mFragment = NavDrawerHelper.getItemFragment(position);
        if (mFragment == null) return;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.main_container, mFragment);
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        ft.commit();
    }

    private class DrawerToggle extends ActionBarDrawerToggle {
        DrawerToggle(Activity activity, DrawerLayout drawerLayout,
                            int openDrawerRes, int closeDrawerRes) {
            super(activity, drawerLayout, openDrawerRes, closeDrawerRes);
        }
        @Override
        public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
            if (mUserListLayout.getVisibility() == View.VISIBLE) {
                mUserListLayout.setVisibility(View.INVISIBLE);
            }
            setActionBarTitle(mMenuTitle);
            invalidateOptionsMenu();
        }
        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            setActionBarTitle(mAppTitle);
            invalidateOptionsMenu();
        }
    }

}