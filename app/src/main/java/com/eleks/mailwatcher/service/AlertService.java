package com.eleks.mailwatcher.service;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.eleks.mailwatcher.PlayAlarmScreenActivity;
import com.eleks.mailwatcher.authentification.ExchangeAuthenticator;
import com.eleks.mailwatcher.model.AlertDBHelper;
import com.eleks.mailwatcher.model.AlertModel;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.HistoryLabelAdded;
import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import ExchangeActiveSync.EasConnection;
import ExchangeActiveSync.EasFolder;
import ExchangeActiveSync.EasSyncCommand;

public class AlertService extends IntentService {
    public final static String TAG = AlertService.class.getSimpleName();

    private AlertDBHelper dbHelper = new AlertDBHelper(this);

    public AlertService() {
        super("AlertService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Log.i(TAG, "Wakeup event received");
            try {
                run(intent);
            } catch (Exception e) {
                Log.e(TAG, "run()", e);
            }
        } finally {
            WakeupReceiver.completeWakefulIntent(intent);
        }
    }

    public static void update(Context context) {
        AlertDBHelper dbHelper = new AlertDBHelper(context);
        boolean hasActiveAlerts = dbHelper.hasActiveAlerts();
        if (hasActiveAlerts)
            WakeupReceiver.activate(context);
        else
            WakeupReceiver.cancel(context);
    }

    private void run(Intent intent) {
        List<AlertModel> alerts = dbHelper.getAlerts();
        for (AlertModel alert : alerts) {
            if (alert.isEnabled) {
                alert.lastCheckDate = Calendar.getInstance().getTime();
                alert.lastError = null;
                try {
                    if (alert.accountType == AlertModel.AccountType.exchange) {
                        checkExchangeAlert(alert);
                    } else {
                        checkGmailAlert(alert);
                    }
                } catch (Exception e) {
                    alert.lastError = e.getMessage();
                    Log.e(TAG, "checkAlert() " + alert.name, e);
                }
                dbHelper.updateAlert(alert);
            }
        }
    }

    private void checkGmailAlert(AlertModel alert) throws Exception {
        String[] SCOPES = {
                GmailScopes.MAIL_GOOGLE_COM,
                GmailScopes.GMAIL_LABELS,
                GmailScopes.GMAIL_READONLY};

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "No permission: " + Manifest.permission.GET_ACCOUNTS);
            return;
        }
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(),
                Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName("misha.kharaba@gmail.com");

        GmailReader reader = new GmailReader(credential);
        BigInteger historyId;
        if (alert.historyId == null || alert.historyId.isEmpty()) {
            historyId = getHistoryId(reader);
            if (historyId == null)
                return;
            alert.historyId = historyId.toString();
            dbHelper.updateAlert(alert);
        } else {
            historyId = new BigInteger(alert.historyId);
        }
        GmailReader.HistoryRec historyRec = reader.getHistory(historyId, alert.labelId, 100);
        for (History history : historyRec.list) {
            List<HistoryMessageAdded> addedMessaged = history.getMessagesAdded();
            if (addedMessaged != null && addedMessaged.size() > 0) {
                startAlert(alert);
                break;
            }

            List<HistoryLabelAdded> addedLabels = history.getLabelsAdded();
            if (addedLabels != null && addedLabels.size() > 0) {
                startAlert(alert);
                break;
            }
        }
        alert.historyId = historyRec.historyId.toString();
        Log.i(TAG, "Last history ID " + alert.historyId);
    }

    private BigInteger getHistoryId(GmailReader reader) throws IOException {
        List<Message> messages = reader.getMessages(1);
        if (messages.size() > 0) {
            Message msg = reader.getMessage(messages.get(0).getId());
            return msg.getHistoryId();
        }
        return null;
    }

    private void checkExchangeAlert(AlertModel alert) throws Exception {
        AccountManager accountManager = AccountManager.get(getBaseContext());
        Account account = new Account(alert.userAccount, ExchangeAuthenticator.ACCOUNT_TYPE);
        String user = accountManager.getUserData(account, ExchangeAuthenticator.KEY_USER);
        Boolean ignoreCert = "1".equals(accountManager.getUserData(account,
                ExchangeAuthenticator.KEY_IGNORE_CERT));
        Long policyKey = Long.parseLong(accountManager.getUserData(account,
                ExchangeAuthenticator.KEY_POLICY_KEY));

        EasConnection con = new EasConnection();
        con.setServer(accountManager.getUserData(account, ExchangeAuthenticator.KEY_SERVER));
        con.setCredential(
                accountManager.getUserData(account, ExchangeAuthenticator.KEY_USER),
                accountManager.getPassword(account));
        con.setIgnoreCertificate(ignoreCert);

        if (alert.historyId == null || alert.historyId.isEmpty()) {
            String syncKey = con.getFolderSyncKey(policyKey, alert.labelId);
            syncKey = con.getFolderLastSyncKey(policyKey, alert.labelId, syncKey);
            alert.historyId = syncKey;
            dbHelper.updateAlert(alert);
        }

        EasSyncCommand syncCommands = new EasSyncCommand();
        syncCommands.setSyncKey(alert.historyId);
        boolean wasAlert = false;
        do {
            syncCommands = con.getFolderSyncCommands(policyKey, alert.labelId, syncCommands.getSyncKey(), 512);
            if (syncCommands.getAdded().size() > 0) {
                wasAlert = true;
            }
        } while (syncCommands.allSize() > 0);

        if (wasAlert) {
            startAlert(alert);
        }

        alert.historyId = syncCommands.getSyncKey();
        Log.i(TAG, "Last history ID " + alert.historyId);
    }

    private void startAlert(AlertModel alert) {
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
