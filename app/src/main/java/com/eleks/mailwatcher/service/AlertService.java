package com.eleks.mailwatcher.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.eleks.mailwatcher.AlertListActivity;
import com.eleks.mailwatcher.EHelper;
import com.eleks.mailwatcher.PlayAlarmScreenActivity;
import com.eleks.mailwatcher.R;
import com.eleks.mailwatcher.authentification.ExchangeAuthenticator;
import com.eleks.mailwatcher.model.AlertDBHelper;
import com.eleks.mailwatcher.model.AlertModel;
import com.eleks.mailwatcher.model.MailMessageRec;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.HistoryLabelAdded;
import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import ExchangeActiveSync.EasConnection;
import ExchangeActiveSync.EasSyncCommand;

public class AlertService extends IntentService {
    public final static String TAG = AlertService.class.getSimpleName();

    private AlertDBHelper dbHelper = new AlertDBHelper(this);

    public AlertService() {
        super("MailWatcher.AlertService");
    }

    public static void update(Context context) {
        AlertDBHelper dbHelper = new AlertDBHelper(context);
        boolean hasActiveAlerts = dbHelper.hasActiveAlerts();
        if (hasActiveAlerts)
            WakeupReceiver.activate(context);
        else
            WakeupReceiver.cancel(context);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Log.i(TAG, "Wakeup event received");
            try {
                run();
            } catch (Exception e) {
                Log.e(TAG, "run()", e);
            }
        } finally {
            WakeupReceiver.completeWakefulIntent(intent);
        }
    }

    private void run() {
        List<AlertModel> alerts = dbHelper.getAlerts();
        boolean checkCancel = false;
        for (AlertModel alert : alerts) {
            if (alert.isEnabled) {
                alert.lastCheckDate = Calendar.getInstance().getTime();
                alert.lastError = null;
                try {
                    checkDeviceOnline();
                    if (alert.accountType == AlertModel.AccountType.exchange) {
                        checkExchangeAlert(alert);
                    } else {
                        checkGmailAlert(alert);
                    }
                } catch (GooglePlayServicesAvailabilityIOException e) {
                    alert.lastError = EHelper.getMessage(e);
                    alert.isEnabled = false;
                    checkCancel = true;
                } catch (Exception e) {
                    alert.lastError = EHelper.getMessage(e);
                    Log.e(TAG, "Error in checkAlert() " + alert.name, e);
                }
                dbHelper.updateAlert(alert);

                Intent intent = new Intent();
                intent.setAction(AlertListActivity.REFRESH);
                sendBroadcast(intent);
            }
        }
        if (checkCancel) {
            if (!dbHelper.hasActiveAlerts())
                WakeupReceiver.cancel(getBaseContext());
        }
    }

    private void checkDeviceOnline() throws Exception {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null)
            throw new Exception(getString(R.string.err_no_active_network));
        if (!networkInfo.isConnected())
            throw new Exception("Not connected to network: " + networkInfo.getReason());
    }

    private Account findAccount(AlertModel alert, AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType(ExchangeAuthenticator.ACCOUNT_TYPE);
        for (Account account : accounts) {
            if (TextUtils.equals(account.name, alert.userAccount))
                return account;
        }
        return null;
    }

    private void checkGmailAlert(AlertModel alert) throws Exception {
        String[] SCOPES = {
                GmailScopes.MAIL_GOOGLE_COM,
                GmailScopes.GMAIL_LABELS,
                GmailScopes.GMAIL_READONLY};

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(alert.userAccount);

        if (credential.getSelectedAccount() == null)
            throw new Exception(String.format("Account '%s' not found", alert.userAccount));

        GmailReader reader = new GmailReader(credential);
        if (alert.historyId == null) {
            String historyId = getHistoryId(reader, null);
            Log.i(TAG, "Init gmail history ID " + historyId);
            alert.historyId = historyId;
            dbHelper.updateAlert(alert);
        }
        List<String> msgIdList = new ArrayList<>();
        boolean wasAlert = false;
        GmailReader.HistoryRec historyRec = reader.getHistory(alert.historyId, alert.labelId, 1024);
        for (History history : historyRec.list) {
            List<HistoryMessageAdded> addedMessaged = history.getMessagesAdded();
            if (addedMessaged != null && addedMessaged.size() > 0) {
                for (HistoryMessageAdded added : addedMessaged) {
                    msgIdList.add(added.getMessage().getId());
                    wasAlert = true;
                }
            }

            List<HistoryLabelAdded> addedLabels = history.getLabelsAdded();
            if (addedLabels != null && addedLabels.size() > 0) {
                for (HistoryLabelAdded added : addedLabels) {
                    msgIdList.add(added.getMessage().getId());
                    wasAlert = true;
                }
            }
        }

        if (wasAlert) {
            Message msg = reader.getMessage(msgIdList.get(0));
            MailMessageRec msgRec = new MailMessageRec(msg);
            startAlert(alert, msgRec);
        }

        alert.historyId = historyRec.historyId.toString();
        Log.i(TAG, "Last gmail history ID " + alert.historyId);
    }

    private String getHistoryId(GmailReader reader, String labelid) throws IOException {
        List<Message> messages = reader.getMessages(labelid, 1);
        if (messages.size() > 0) {
            Message msg = reader.getMessage(messages.get(0).getId());
            String historyId = msg.getHistoryId().toString();
            GmailReader.HistoryRec historyRec = reader.getHistory(historyId, null, 1024);
            return historyRec.historyId.toString();
        }
        return null;
    }

    private void checkExchangeAlert(AlertModel alert) throws Exception {
        AccountManager accountManager = AccountManager.get(getBaseContext());
        Account account = findAccount(alert, accountManager);
        if (account == null)
            throw new Exception(String.format("Account '%s' not found", alert.userAccount));
        String server = accountManager.getUserData(account, ExchangeAuthenticator.KEY_SERVER);
        String user = accountManager.getUserData(account, ExchangeAuthenticator.KEY_USER);
        String pwd = accountManager.getPassword(account);
        Boolean ignoreCert = "1".equals(accountManager.getUserData(account, ExchangeAuthenticator.KEY_IGNORE_CERT));
        Long policyKey = Long.parseLong(accountManager.getUserData(account, ExchangeAuthenticator.KEY_POLICY_KEY));


        EasConnection con = new EasConnection();
        con.setServer(server);
        con.setCredential(user, pwd);
        con.setIgnoreCertificate(ignoreCert);

        if (alert.historyId == null) {
            String syncKey = con.getFolderSyncKey(policyKey, alert.labelId);
            syncKey = con.getFolderLastSyncKey(policyKey, alert.labelId, syncKey);
            alert.historyId = syncKey;
            Log.i(TAG, "Init exchange sync key ID " + alert.historyId);
            dbHelper.updateAlert(alert);
        }

        EasSyncCommand syncCommands = new EasSyncCommand();
        syncCommands.setSyncKey(alert.historyId);
        List<MailMessageRec> msgRecs = new ArrayList<>();
        boolean wasAlert = false;
        do {
            syncCommands = con.getFolderSyncCommands(policyKey, alert.labelId, syncCommands.getSyncKey(), 512);
            if (syncCommands.getAdded().size() > 0) {
                for (EasSyncCommand.Command cmd : syncCommands.getAdded()) {
                    MailMessageRec msgRec = new MailMessageRec(cmd.getMessage());
                    msgRecs.add(msgRec);
                }
                wasAlert = true;
            }
        } while (syncCommands.allSize() > 0);

        if (wasAlert) {
            startAlert(alert, msgRecs.get(0));
        }

        alert.historyId = syncCommands.getSyncKey();
        Log.i(TAG, "Last exchange sync key ID " + alert.historyId);
    }

    private void startAlert(AlertModel alert, MailMessageRec msgRec) {
        Log.i(TAG, "Starting play alarm screen");
        alert.lastAlarmDate = alert.lastCheckDate;
        Intent intent = new Intent(this, PlayAlarmScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AlertDBHelper.Alert.COLUMN_NAME, alert.name);
        intent.putExtra(AlertDBHelper.Alert.COLUMN_USER_ACCOUNT, alert.userAccount);
        intent.putExtra(AlertDBHelper.Alert.COLUMN_LABEL_NAME, alert.labelName);
        intent.putExtra(AlertDBHelper.Alert.COLUMN_ALARM_TONE, alert.alarmTone.toString());
        startActivity(intent);
    }
}
