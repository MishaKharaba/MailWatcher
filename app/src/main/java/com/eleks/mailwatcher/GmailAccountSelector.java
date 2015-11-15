package com.eleks.mailwatcher;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.eleks.mailwatcher.model.LabelRec;
import com.eleks.mailwatcher.service.GmailReader;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GmailAccountSelector implements IAccountSelector {
    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String[] SCOPES = {
            GmailScopes.MAIL_GOOGLE_COM,
            GmailScopes.GMAIL_LABELS,
            GmailScopes.GMAIL_READONLY};

    private static final int RESULT_OK = Activity.RESULT_OK;
    private static final int RESULT_CANCELED = Activity.RESULT_CANCELED;

    private final Activity mOwnedActivity;
    private Result result;
    private String mAccountName;
    private GoogleAccountCredential mCredential;
    private List<LabelRec> labelRecs;

    public GmailAccountSelector(Activity ownedActivity, IAccountSelector.Result result) {
        this.mOwnedActivity = ownedActivity;
        this.result = result;
    }

    @Override
    public String getAccountName() {
        return mAccountName;
    }

    @Override
    public void Select(String accountName) {
        this.mAccountName = accountName;
        if (mCredential == null) {
            mCredential = GoogleAccountCredential.usingOAuth2(mOwnedActivity.getApplicationContext(),
                    Arrays.asList(SCOPES))
                    .setBackOff(new ExponentialBackOff());
        }
        mCredential.setSelectedAccountName(accountName);
        chooseAccount();
        checkPermission(Manifest.permission.GET_ACCOUNTS);
    }

    @Override
    public void setAccount(String accountName) {
        this.mAccountName = accountName;
        if (mCredential == null) {
            mCredential = GoogleAccountCredential.usingOAuth2(mOwnedActivity.getApplicationContext(),
                    Arrays.asList(SCOPES))
                    .setBackOff(new ExponentialBackOff());
        }
        mCredential.setSelectedAccountName(accountName);
        if (accountName != null)
            new GetLabelsTask().execute();
    }

    private void chooseAccount() {
        mOwnedActivity.startActivityForResult(mCredential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER);
    }

    private void checkPermission(String permission) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(mOwnedActivity, permission) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(mOwnedActivity, permission)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
            }
            ActivityCompat.requestPermissions(mOwnedActivity, new String[]{permission}, 0);
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    //mMsg.setText("isGooglePlayServicesAvailable()");
                    //isGooglePlayServicesAvailable();
                    showShortToast("No Google Play Service");
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        mAccountName = accountName;
                        if (result != null) {
                            result.Selected(accountName);
                        }
                        new GetLabelsTask().execute();
                        showShortToast("Account selected");
                    } else {
                        showShortToast("No account selected");
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    showShortToast("Selection cancelled");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public List<LabelRec> getFolders() {
        return labelRecs;
    }

    private void showShortToast(String text) {
        Toast toast = Toast.makeText(mOwnedActivity, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void setLabels(List<LabelRec> labelRecs) {
        this.labelRecs = labelRecs;
    }

    private class GetLabelsTask extends AsyncTask<Void, Void, List<LabelRec>> {
        @Override
        protected List<LabelRec> doInBackground(Void... params) {
            GmailReader reader = new GmailReader(mCredential);
            List<Label> labels = reader.getLabelListSafe();
            if (reader.getLastError() instanceof UserRecoverableAuthIOException) {
                Intent intent = ((UserRecoverableAuthIOException) reader.getLastError()).getIntent();
                mOwnedActivity.startActivityForResult(intent, REQUEST_AUTHORIZATION);
            }
            ArrayList<LabelRec> labelRecs = null;
            if (labels != null) {
                labelRecs = new ArrayList<>();
                for (Label label : labels) {
                    if (!"system".equalsIgnoreCase(label.getType()) ||
                            "INBOX".equalsIgnoreCase(label.getId())) {
                        labelRecs.add(new LabelRec(label.getId(), label.getName()));
                    }
                }
            }
            return labelRecs;
        }

        @Override
        protected void onPostExecute(List<LabelRec> labelRecs) {
            if (labelRecs != null) {
                setLabels(labelRecs);

                showShortToast("Labels loaded");
            }
        }

    }
}
