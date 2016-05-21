package me.avelar.donee.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import me.avelar.donee.R;
import me.avelar.donee.controller.SessionManager;
import me.avelar.donee.dao.UserDAO;
import me.avelar.donee.model.Session;

public class FragmentSettings extends PreferenceFragment implements
             Preference.OnPreferenceClickListener, DialogInterface.OnClickListener {

    Preference pDeleteUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.fragment_settings);
        pDeleteUser = findPreference("delete_user");
        pDeleteUser.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.equals(pDeleteUser)) {
            new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.confirmation_title))
                .setMessage(getResources().getString(R.string.delete_confirmation_question))
                .setPositiveButton(R.string.yes, this)
                .setNegativeButton(R.string.no, null)
                .create().show();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        final Session session = SessionManager.getLastSession(getActivity());
        if (session != null) {
            SessionManager.logoutCurrentSession(getActivity(), true);
            String message = getString(R.string.user_deleted);
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UserDAO.delete(getActivity(), session.getUser());
                }
            }).start();
            if (getActivity() instanceof ActivityMain) {
                ((ActivityMain)getActivity()).toggleDrawer();
            }
        }
    }
}
