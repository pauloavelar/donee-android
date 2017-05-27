package me.avelar.donee.view;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Date;

import me.avelar.donee.R;
import me.avelar.donee.controller.CollectionLogic;
import me.avelar.donee.controller.FormsLogic;
import me.avelar.donee.controller.Validator;
import me.avelar.donee.model.Collection;
import me.avelar.donee.model.Field;
import me.avelar.donee.model.Form;
import me.avelar.donee.util.ConnectivityHelper;
import me.avelar.donee.util.FieldFactory;
import me.avelar.donee.util.IntentFactory;
import me.avelar.donee.util.PermissionHelper;
import me.avelar.donee.view.fields.FieldGroup;
import me.avelar.donee.view.fields.ImageGroup;

public class ActivityCollect extends AppCompatActivity implements View.OnClickListener,
        ConnectionCallbacks, OnConnectionFailedListener, DialogInterface.OnClickListener {

    private int mLocationRetries;

    private enum ViewState {FORM_ERROR, FORM_EMPTY, FORM_LOADED}

    private enum DialogType {LOCATING, SAVING, SAVED, LOCATION_DISABLED, ERROR}

    private enum AnimationDirection {UP, DOWN}

    private static final int REQUEST_PICTURE_FROM_GALLERY = 12151;
    private static final int REQUEST_PICTURE_FROM_CAMERA = 12152;

    private boolean mCanceled;
    private GoogleApiClient mGoogleApiClient;
    private Dialog mDialog;

    private Form mForm;
    private Collection mDraft;

    private int mScrollPosition;

    private View mVwError, mVwEmpty;
    private TextView mTvErrorMessage;
    private ScrollView mSvForm;
    private ImageGroup mImageReference;
    private ArrayList<FieldGroup> mFields;

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
                    mTvErrorMessage.setOnClickListener(ActivityCollect.this);
                    mTvErrorMessage.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setErrorMessage(null);
                        }
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

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra(IntentFactory.EXTRA_ACTION);
            if (result == null) result = IntentFactory.Type.COLLECTION_STORED_ERROR.toString();
            int destination = intent.getIntExtra(IntentFactory.EXTRA_DETAIL, -1);
            switch (IntentFactory.Type.valueOf(result)) {
                case COLLECTION_STORED:
                    showDialog(DialogType.SAVED, destination);
                    break;
                case COLLECTION_STORED_ERROR:
                    showDialog(DialogType.ERROR);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);

        // getting view references
        mSvForm = (ScrollView) findViewById(R.id.collect_scroll);
        mTvErrorMessage = (TextView) findViewById(R.id.collections_non_intrusive_error);
        mVwError = findViewById(R.id.form_error);
        mVwEmpty = findViewById(R.id.form_empty);

        // enabling back navigation and setting title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // getting and validating the received form
        mForm = getIntent().getParcelableExtra(FormsLogic.EXTRA_FORM);
        mDraft = getIntent().getParcelableExtra(CollectionLogic.EXTRA_COLLECTION);

        // syncing activity state
        if (savedInstanceState != null) {
            // get state and update it
            mScrollPosition = savedInstanceState.getInt(CollectionLogic.EXTRA_FOCUSED);
            mDraft = savedInstanceState.getParcelable(CollectionLogic.EXTRA_COLLECTION);
        } else if (mDraft == null) {
            mDraft = new Collection();
            mDraft.setRelatedForm(mForm);
            mDraft.setSubmittedTime(new Date());
        }

        configureForm();

        // registering BroadcastReceiver for async responses
        IntentFilter loginFilter = IntentFactory.createFilter(IntentFactory.ViewReference.COLLECT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, loginFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_collect, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!saveAsDraft()) NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_validate:
                clickValidate(true);
                return true;
            case R.id.action_submit:
                clickSubmit();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (!saveAsDraft()) super.onBackPressed();
    }

    private boolean saveAsDraft() {
        if (mFields == null || mFields.size() == 0) return false;
        for (FieldGroup fg : mFields) fg.commit();
        if (mDraft.getValues() == null || mDraft.getValues().size() == 0) return false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int mode = mDraft.isSubmitted() ? CollectionLogic.OUTBOX : CollectionLogic.DRAFTS;
                CollectionLogic.storeCollection(ActivityCollect.this, mDraft, mode);
            }
        }).start();
        return true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // saving state and variables for activity recreation
        if (mFields != null) {
            for (FieldGroup field : mFields) {
                field.commit();
            }
        }
        outState.putParcelable(CollectionLogic.EXTRA_COLLECTION, mDraft);
        outState.putInt(CollectionLogic.EXTRA_FOCUSED, mSvForm.getScrollY());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void clickSubmit() {
        if (clickValidate(false)) {
            for (FieldGroup fg : mFields) fg.commit();
            if (mForm.usesLocation()) {
                mCanceled = false;
                showDialog(DialogType.LOCATING);
                buildGoogleApiClient();
                mGoogleApiClient.connect();
                mLocationRetries = 0;
            } else {
                showDialog(DialogType.SAVING);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CollectionLogic.storeCollection(ActivityCollect.this, mDraft, CollectionLogic.OUTBOX);
                    }
                }).start();
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mCanceled) return;

        if (!PermissionHelper.checkForLocationPermission(this)) {
            if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
            PermissionHelper.requestLocationPermission(this);
        } else {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location != null) {
                showDialog(DialogType.SAVING);
                mDraft.addLocation(location);
                CollectionLogic.storeCollection(this, mDraft, CollectionLogic.OUTBOX);
            } else if (mLocationRetries < 3) {
                mLocationRetries++;
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() { onConnected(null); }
                }, 1000);
            } else {
                // show dialog enable locations
                if (!ConnectivityHelper.isLocationEnabled(this)) {
                    showDialog(DialogType.LOCATION_DISABLED);
                } else {
                    showDialog(DialogType.ERROR);
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showDialog(DialogType.ERROR);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] results) {
        switch (requestCode) {
            case PermissionHelper.REQUEST_PERMISSION_GPS:
                if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted, continue getting the location
                    mGoogleApiClient.reconnect();
                    mLocationRetries = 0;
                } else {
                    // permission denied, just ask the user to allow it
                    showDialog(DialogType.ERROR);
                }
        }
    }

    private void showDialog(DialogType type) {
        showDialog(type, CollectionLogic.OUTBOX);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void showDialogV21(AlertDialog.Builder builder, DialogType type, int destination) {
        switch(type) {
            case LOCATING:
                builder.setView(R.layout.dialog_locating);
                break;
            case LOCATION_DISABLED:
                builder.setView(R.layout.dialog_location_disabled);
                break;
            case SAVING:
                builder.setView(R.layout.dialog_saving);
                break;
        }
    }

    private void showDialog(DialogType type, int destination) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            showDialogV21(builder, type, destination);
        } else {
            switch (type) {
                case LOCATING:
                    builder.setMessage(R.string.getting_location);
                    break;
                case LOCATION_DISABLED:
                    builder.setMessage(R.string.enable_location);
                    break;
                case SAVING:
                    builder.setMessage(R.string.saving_form);
                    break;
            }
        }

        switch (type) {
            case LOCATING:
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mCanceled = true;
                    }
                });
                break;
            case LOCATION_DISABLED:
                builder.setNegativeButton(R.string.cancel, this);
                builder.setNeutralButton(R.string.enable, this);
                break;
            case SAVED:
                builder.setTitle(R.string.title_saved);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.ok, this);
                if (destination == CollectionLogic.OUTBOX) {
                    builder.setMessage(R.string.message_saved_outbox);
                } else {
                    builder.setMessage(R.string.message_saved_draft);
                }
                break;
            case ERROR:
                builder.setTitle(R.string.title_error)
                    .setMessage(R.string.message_error)
                    .setPositiveButton(R.string.ok, null);
                break;
        }
        if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
        mDialog = builder.create();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDialog.show();
            }
        });
    }

    private void configureForm() {
        if (mForm == null || mForm.getFields() == null) {
            syncState(ViewState.FORM_ERROR);
            return;
        }

        // setting title and related stuff
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mForm.getCategory());
            actionBar.setSubtitle(mForm.getName());
        }

        // getting the field container and the fields
        LinearLayout container = (LinearLayout) findViewById(R.id.collect_container);
        ArrayList<Field> fields = mForm.getFields();
        mFields = new ArrayList<>();

        // iterating to create the fields
        for (Field field : fields) {
            FieldGroup fg = FieldFactory.create(this, field);
            if (fg != null) {
                fg.setCollection(mDraft);
                container.addView(fg);
                mFields.add(fg);
            }
        }
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mSvForm.scrollTo(0, mScrollPosition);
            }
        });
        syncState(ViewState.FORM_LOADED);
    }

    private boolean clickValidate(boolean showToast) {
        resetFields();
        setErrorMessage(null);
        int result = Validator.test(mFields);
        if (result != Validator.NO_ERROR) {
            FieldGroup fg = mFields.get(result);
            setErrorMessage(fg.getErrorMessage());
            fg.showError(true);
            focusOnView(fg);
            return false;
        }
        if (showToast) {
            Toast.makeText(this, R.string.collect_no_error, Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private void setErrorMessage(String errorMessage) {
        int animationResource = 0;

        if (errorMessage == null && mTvErrorMessage.getVisibility() == View.VISIBLE) {
            animationResource = R.anim.slide_down;
        } else if (errorMessage != null && mTvErrorMessage.getVisibility() == View.INVISIBLE) {
            mTvErrorMessage.setText(errorMessage);
            animationResource = R.anim.slide_up;
        }

        if (animationResource != 0) {
            Animation anim = AnimationUtils.loadAnimation(this, animationResource);
            anim.setAnimationListener(mErrorListener);
            mTvErrorMessage.startAnimation(anim);
        }
    }

    private void focusOnView(final View v) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mSvForm.scrollTo(0, v.getTop());
            }
        });
    }

    private void syncState(final ViewState state) {
            mSvForm.setVisibility(state == ViewState.FORM_LOADED ? View.VISIBLE : View.GONE);
            mVwError.setVisibility(state == ViewState.FORM_ERROR ? View.VISIBLE : View.GONE);
            mVwEmpty.setVisibility(state == ViewState.FORM_EMPTY ? View.VISIBLE : View.GONE);
    }

    private void resetFields() {
        if (mFields == null) return;
        for (FieldGroup field : mFields) {
            field.showError(false);
        }
    }

    public void snapPhoto(ImageGroup reference) {
        mImageReference = reference;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_PICTURE_FROM_CAMERA);
        }
    }

    public void pickPhoto(ImageGroup reference) {
        mImageReference = reference;
        Intent intent = new Intent(Intent.ACTION_PICK,
                                   android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_PICTURE_FROM_GALLERY);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.collections_non_intrusive_error) {
            setErrorMessage(null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICTURE_FROM_CAMERA && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap image  = (Bitmap) extras.get("data");
            mImageReference.setImage(image);
        } else if (requestCode == REQUEST_PICTURE_FROM_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            String picturePath = null;
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                picturePath = cursor.getString(columnIndex);
                cursor.close();
            }
            if (picturePath != null) {
                mImageReference.setImage(picturePath);
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_NEGATIVE:
                // Cancel button in Location disabled dialog
                // Does the same as the OK button (closes the form)
            case Dialog.BUTTON_POSITIVE:
                // OK button in "Saved" dialog
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        NavUtils.navigateUpFromSameTask(ActivityCollect.this);
                    }
                });
                break;
            case Dialog.BUTTON_NEUTRAL:
                // Enable button in Location disabled dialog
                if (!PermissionHelper.checkForLocationPermission(this)) {
                    PermissionHelper.requestLocationPermission(this);
                } else {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
                break;
        }
    }

}