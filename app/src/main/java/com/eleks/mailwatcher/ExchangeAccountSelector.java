package com.eleks.mailwatcher;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.eleks.mailwatcher.authentification.ExchangeAuthenticator;
import com.eleks.mailwatcher.model.LabelRec;

import java.util.ArrayList;
import java.util.List;

import ExchangeActiveSync.EasConnection;
import ExchangeActiveSync.EasFolder;
import ExchangeActiveSync.EasFolderType;

public class ExchangeAccountSelector implements IAccountSelector {
    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private final Activity ownedActivity;
    private final Result result;
    private final AccountManager accountManager;
    private String accountName;
    private List<LabelRec> folders;

    public ExchangeAccountSelector(Activity ownedActivity, IAccountSelector.Result result) {
        this.ownedActivity = ownedActivity;
        this.result = result;
        this.accountManager = AccountManager.get(ownedActivity);
    }

    public Activity getOwnedActivity() {
        return ownedActivity;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    @Override
    public void Select(String accountName) {
        showAccountPicker();
    }

    @Override
    public void setAccount(String accountName) {
        this.accountName = accountName;
        if (accountName != null)
            new GetFoldersTask().execute();
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ACCOUNT_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                this.accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                new GetFoldersTask().execute();
                showShortToast("Account selected");
                if (result != null) {
                    result.Selected(accountName);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public List<LabelRec> getFolders() {
        return folders;
    }

    private void showAccountPicker() {
        Account account = accountName != null
                ? new Account(accountName, ExchangeAuthenticator.ACCOUNT_TYPE) : null;

        @SuppressWarnings("deprecation")
        Intent intent = AccountManager.newChooseAccountIntent(account, null,
                new String[]{ExchangeAuthenticator.ACCOUNT_TYPE}, true,
                null, null, null, null);

        getOwnedActivity().startActivityForResult(intent, REQUEST_ACCOUNT_PICKER);
    }

    private void addNewAccount(String accountType, String authTokenType) {
        final AccountManagerFuture<Bundle> future = accountManager.addAccount(accountType, authTokenType,
                null, null, getOwnedActivity(),
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            if (!future.isCancelled()) {
                                Bundle bnd = future.getResult();
                                showShortToast("Account was created");
                                accountName = bnd.getString(AccountManager.KEY_ACCOUNT_NAME);
                                new GetFoldersTask().execute();
                                if (result != null) {
                                    result.Selected(accountName);
                                }
                            } else {
                                showShortToast("Operation canceled");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, null);
    }

    private void showShortToast(String text) {
        Toast toast = Toast.makeText(getOwnedActivity(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private class GetFoldersTask extends AsyncTask<Void, Void, List<LabelRec>> {
        private String error;

        @Override
        protected List<LabelRec> doInBackground(Void... params) {
            ArrayList<LabelRec> labelRecs = null;
            try {
                Account account = new Account(accountName, ExchangeAuthenticator.ACCOUNT_TYPE);
                String user = accountManager.getUserData(account, ExchangeAuthenticator.KEY_USER);
                Boolean ignoreCert = "1".equals(accountManager.getUserData(account,
                        ExchangeAuthenticator.KEY_IGNORE_CERT));

                EasConnection con = new EasConnection();
                con.setServer(accountManager.getUserData(account, ExchangeAuthenticator.KEY_SERVER));
                con.setCredential(
                        accountManager.getUserData(account, ExchangeAuthenticator.KEY_USER),
                        accountManager.getPassword(account));
                con.setIgnoreCertificate(ignoreCert);
                long policyKey = con.getPolicyKey();
                List<EasFolder> easFolders = con.getFolders(policyKey);

                labelRecs = new ArrayList<>();
                for (EasFolder label : easFolders) {
                    if (label.getType() == EasFolderType.UserCreatedMail ||
                            label.getType() == EasFolderType.DefaultInbox) {
                        labelRecs.add(new LabelRec(label.getId(), label.getName()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                error = EHelper.getMessage(e);
            }
            return labelRecs;
        }

        @Override
        protected void onPostExecute(List<LabelRec> labelRecs) {
            if (labelRecs != null) {
                folders = labelRecs;
                showShortToast("Folders loaded");
            } else if (error != null) {
                showShortToast(error);
            }
        }

    }

}
