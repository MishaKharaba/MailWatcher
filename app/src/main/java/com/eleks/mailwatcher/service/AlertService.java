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
import com.eleks.mailwatcher.model.AlertModel;
import com.eleks.mailwatcher.model.DBHelper;
import com.eleks.mailwatcher.model.MailMessageRec;
import com.eleks.mailwatcher.model.Utils;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.HistoryLabelAdded;
import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ExchangeActiveSync.EasConnection;
import ExchangeActiveSync.EasSyncCommand;

public class AlertService extends IntentService {
    public final static String TAG = AlertService.class.getSimpleName();

    private DBHelper dbHelper = new DBHelper(this);

    public AlertService() {
        super("MailWatcher.AlertService");
    }

    public static void update(Context context) {
        DBHelper dbHelper = new DBHelper(context);
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
            Log.i(TAG, "Wakeup event completed");
        }
    }

    private void run() {
        List<AlertModel> alerts = dbHelper.getAlerts();
        boolean checkCancel = false;
        for (AlertModel alert : alerts) {
            if (alert.isEnabled) {
                try {
                    checkDeviceOnline();
                    if (alert.accountType == AlertModel.AccountType.exchange) {
                        processExchangeAlert(alert);
                    } else {
                        processGmailAlert(alert);
                    }
                } catch (GooglePlayServicesAvailabilityIOException e) {
                    Log.e(TAG, "Error in checkAlert() " + alert.name, e);
                    dbHelper.setError(alert.id, EHelper.getMessage(e), true);
                    checkCancel = true;
                } catch (Exception e) {
                    Log.e(TAG, "Error in checkAlert() " + alert.name, e);
                    dbHelper.setError(alert.id, EHelper.getMessage(e), false);
                }

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

    private Account findAccount(AlertModel alert, AccountManager accountManager, String accountType) {
        Account[] accounts = accountManager.getAccountsByType(accountType);
        for (Account account : accounts) {
            if (TextUtils.equals(account.name, alert.userAccount))
                return account;
        }
        return null;
    }

    private void processGmailAlert(AlertModel alert) throws Exception {
        Log.d(TAG, "processGmailAlert");
        AccountManager accountManager = AccountManager.get(getBaseContext());
        Account account = findAccount(alert, accountManager, GoogleAccountManager.ACCOUNT_TYPE);
        if (account == null)
            throw new Exception(String.format("Account '%s' not found", alert.userAccount));

        GmailReader reader = new GmailReader(getApplicationContext(), alert.userAccount);
        if (alert.historyId == null) {
            initGmailAlert(alert, reader);
        }

        List<String> msgIdList = checkGmailAlert(alert, reader);
        if (msgIdList.size() > 0) {
            MailMessageRec lastMsg = null;
            String[] fromList = Utils.splitMalList(alert.filterFrom);
            String[] toList = Utils.splitMalList(alert.filterTo);
            Pattern pSubject = Utils.makePattern(alert.filterSubject);
            for (String msgId : msgIdList) {
                MailMessageRec msgRec = new MailMessageRec(reader.getMessage(msgId));
                if (msgRec.checkFrom(fromList) && msgRec.checkTo(toList)
                        && msgRec.checkSubject(pSubject)) {
                    lastMsg = msgRec;
                }
            }
            if (lastMsg != null) {
                startAlert(alert, lastMsg);
            }
            dbHelper.updateAlertHistory(alert.id, alert.historyId, lastMsg);
        } else {
            dbHelper.updateAlertHistory(alert.id, alert.historyId, null);
        }
    }

    private void initGmailAlert(AlertModel alert, GmailReader reader) throws IOException {
        Message msg = reader.getLastMessage(alert.labelId);
        MailMessageRec msgRec = null;
        if (msg != null) {
            msg = reader.getMessage(msg.getId());
            msgRec = new MailMessageRec(msg);
        } else {
            msg = reader.getLastMessage(null);
        }
        alert.historyId = getHistoryId(reader, msg);
        Log.i(TAG, "Init gmail history ID " + alert.historyId);
        dbHelper.updateAlertHistory(alert.id, alert.historyId, msgRec);
    }

    private String getHistoryId(GmailReader reader, Message msg) throws IOException {
        if (msg != null) {
            String historyId = msg.getHistoryId().toString();
            GmailReader.HistoryRec historyRec = reader.getHistory(historyId, null, 1024);
            return historyRec.historyId.toString();
        }
        return null;
    }

    private List<String> checkGmailAlert(AlertModel alert, GmailReader reader) throws IOException {
        List<String> msgIdList = new ArrayList<>();
        GmailReader.HistoryRec historyRec = reader.getHistory(alert.historyId, alert.labelId, 1024);
        for (History history : historyRec.list) {
            List<HistoryMessageAdded> addedMessaged = history.getMessagesAdded();
            if (addedMessaged != null && addedMessaged.size() > 0) {
                for (HistoryMessageAdded added : addedMessaged) {
                    msgIdList.add(added.getMessage().getId());
                }
            }

            List<HistoryLabelAdded> addedLabels = history.getLabelsAdded();
            if (addedLabels != null && addedLabels.size() > 0) {
                for (HistoryLabelAdded added : addedLabels) {
                    msgIdList.add(added.getMessage().getId());
                }
            }
        }

        alert.historyId = historyRec.historyId.toString();
        Log.i(TAG, "Last gmail history ID " + alert.historyId);
        return msgIdList;
    }

    private void processExchangeAlert(AlertModel alert) throws Exception {
        Log.d(TAG, "processExchangeAlert");
        AccountManager accountManager = AccountManager.get(getBaseContext());
        Account account = findAccount(alert, accountManager, ExchangeAuthenticator.ACCOUNT_TYPE);
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

        if (alert.historyId == null)
            initExchangeAlert(alert, con, policyKey);

        List<MailMessageRec> msgRecs = checkExchangeAlert(alert, con, policyKey);
        if (msgRecs.size() > 0) {
            MailMessageRec lastMsg = null;
            String[] fromList = Utils.splitMalList(alert.filterFrom);
            String[] toList = Utils.splitMalList(alert.filterTo);
            Pattern pSubject = Utils.makePattern(alert.filterSubject);
            for (MailMessageRec msgRec : msgRecs) {
                if (msgRec.checkFrom(fromList) && msgRec.checkTo(toList)
                        && msgRec.checkSubject(pSubject)) {
                    lastMsg = msgRec;
                }
            }
            if (lastMsg != null) {
                startAlert(alert, lastMsg);
            }
            dbHelper.updateAlertHistory(alert.id, alert.historyId, lastMsg);
        } else {
            dbHelper.updateAlertHistory(alert.id, alert.historyId, null);
        }
    }

    private void initExchangeAlert(AlertModel alert, EasConnection con, Long policyKey) throws Exception {
        String syncKey = con.getFolderSyncKey(policyKey, alert.labelId);
        EasSyncCommand syncCmd = con.getFolderLastSyncKey(policyKey, alert.labelId, syncKey);
        alert.historyId = syncCmd.getSyncKey();
        Log.i(TAG, "Init exchange sync key ID " + alert.historyId);
        MailMessageRec msgRec = null;
        if (syncCmd.getLastAdded() != null) {
            msgRec = new MailMessageRec(syncCmd.getLastAdded().getMessage());
        }
        dbHelper.updateAlertHistory(alert.id, alert.historyId, msgRec);
    }

    private List<MailMessageRec> checkExchangeAlert(AlertModel alert, EasConnection con, Long policyKey) throws Exception {
        EasSyncCommand syncCommands = new EasSyncCommand();
        syncCommands.setSyncKey(alert.historyId);
        List<MailMessageRec> msgRecs = new ArrayList<>();
        do {
            syncCommands = con.getFolderSyncCommands(policyKey, alert.labelId, syncCommands.getSyncKey(), 512);
            if (syncCommands.getAdded().size() > 0) {
                for (EasSyncCommand.Command cmd : syncCommands.getAdded()) {
                    MailMessageRec msgRec = new MailMessageRec(cmd.getMessage());
                    msgRecs.add(msgRec);
                }
            }
        } while (syncCommands.allSize() > 0);

        alert.historyId = syncCommands.getSyncKey();
        Log.i(TAG, "Last exchange sync key ID " + alert.historyId);
        return msgRecs;
    }

    private void startAlert(AlertModel alert, MailMessageRec msgRec) {
        Log.i(TAG, "Starting play alarm screen");
        alert.lastAlarmDate = alert.lastCheckDate;
        Intent intent = new Intent(this, PlayAlarmScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AlertModel.NAME, alert.name);
        intent.putExtra(AlertModel.USER_ACCOUNT, alert.userAccount);
        intent.putExtra(AlertModel.LABEL_NAME, alert.labelName);
        intent.putExtra(AlertModel.TONE, alert.alarmTone.toString());
        intent.putExtra(PlayAlarmScreenActivity.KEY_MAIL_MESSAGE, msgRec);
        startActivity(intent);
    }
}
