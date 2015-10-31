package com.eleks.mailwatcher;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;

import java.util.Arrays;

public class GmailAccountSelector
{
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
    private String mAccountName;
    GoogleAccountCredential mCredential;

    public GmailAccountSelector(Activity ownedActivity)
    {
        this.mOwnedActivity = ownedActivity;
    }

    public String getmAccountName()
    {
        return mAccountName;
    }

    public void Select(String accountName)
    {
        this.mAccountName = accountName;
        if (mCredential == null)
        {
            mCredential = GoogleAccountCredential.usingOAuth2(mOwnedActivity, Arrays.asList(SCOPES))
                    .setBackOff(new ExponentialBackOff());
        }
        mCredential.setSelectedAccountName(accountName);
        chooseAccount();
        checkPermission(Manifest.permission.GET_ACCOUNTS);
    }

    private void chooseAccount()
    {
        mOwnedActivity.startActivityForResult(mCredential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER);
    }

    private void checkPermission(String permission)
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(mOwnedActivity, permission) != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(mOwnedActivity, permission))
            {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            }
            else
            {
                // No explanation needed, we can request the permission.
            }
            ActivityCompat.requestPermissions(mOwnedActivity, new String[]{permission}, 0);
        }
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK)
                {
                    //mMsg.setText("isGooglePlayServicesAvailable()");
                    //isGooglePlayServicesAvailable();
                    Toast toast = Toast.makeText(mOwnedActivity, "No Google Play Service", Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null)
                {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null)
                    {
                        mCredential.setSelectedAccountName(accountName);
                        mAccountName = accountName;
                        Toast toast = Toast.makeText(mOwnedActivity, "Account selected", Toast.LENGTH_LONG);
                        toast.show();
                    }
                    else
                    {
                        Toast toast = Toast.makeText(mOwnedActivity, "No account selected", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                else if (resultCode == RESULT_CANCELED)
                {
                    Toast toast = Toast.makeText(mOwnedActivity, "Selection cancelled", Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK)
                {
                    chooseAccount();
                }
                break;
            default:
                return false;
        }
        return true;
    }

}