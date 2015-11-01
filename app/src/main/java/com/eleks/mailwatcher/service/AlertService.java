package com.eleks.mailwatcher.service;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.eleks.mailwatcher.AlarmClockScreenActivity;
import com.eleks.mailwatcher.model.AlertDBHelper;
import com.eleks.mailwatcher.model.AlertModel;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.HistoryLabelAdded;
import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.Message;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class AlertService extends IntentService
{
    public final static String TAG = AlertService.class.getSimpleName();

    private AlertDBHelper dbHelper = new AlertDBHelper(this);

    public AlertService()
    {
        super("AlertService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        try
        {
            Log.i(TAG, "Wakeup event received");
            try
            {
                run(intent);
            }
            catch (Exception e)
            {
                Log.e(TAG, "run()", e);
            }
        }
        finally
        {
            WakeupReceiver.completeWakefulIntent(intent);
        }
    }

    public static void update(Context context)
    {
        AlertDBHelper dbHelper = new AlertDBHelper(context);
        boolean hasActiveAlerts = dbHelper.hasActiveAlerts();
        if (hasActiveAlerts)
            WakeupReceiver.activate(context);
        else
            WakeupReceiver.cancel(context);
    }

    private void run(Intent intent)
    {
        List<AlertModel> alerts = dbHelper.getAlerts();
        for (AlertModel alert : alerts)
        {
            if (alert.isEnabled)
                checkAlert(alert);
        }
    }

    private void checkAlert(AlertModel alert)
    {
        String[] SCOPES = {
                GmailScopes.MAIL_GOOGLE_COM,
                GmailScopes.GMAIL_LABELS,
                GmailScopes.GMAIL_READONLY};

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED)
        {
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
        if (alert.historyId == null)
        {
            historyId = getHistoryId(reader);
            if (historyId == null)
                return;
            alert.historyId = historyId.toString();
            dbHelper.updateAlert(alert);
        }
        else
        {
            historyId = new BigInteger(alert.historyId);
        }
        GmailReader.HistoryRec historyRec = reader.getHistory(historyId, alert.labelId, 100);
        for (History history : historyRec.list)
        {
            List<HistoryMessageAdded> addedMessaged = history.getMessagesAdded();
            if (addedMessaged != null && addedMessaged.size() > 0)
            {
                startAlert(alert);
                break;
            }

            List<HistoryLabelAdded> addedLabels = history.getLabelsAdded();
            if (addedLabels != null && addedLabels.size() > 0)
            {
                startAlert(alert);
                break;
            }
        }
        alert.historyId = historyRec.historyId.toString();
        dbHelper.updateAlert(alert);
    }

    private BigInteger getHistoryId(GmailReader reader)
    {
        List<Message> messages = reader.getMessages(1);
        if (messages.size() > 0)
        {
            Message msg = reader.getMessage(messages.get(0).getId());
            return msg.getHistoryId();
        }
        return null;
    }

    private void startAlert(AlertModel alert)
    {
        Intent intent = new Intent(this, AlarmClockScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AlertDBHelper.Alert.COLUMN_NAME, alert.name);
        intent.putExtra(AlertDBHelper.Alert.COLUMN_ALARM_TONE, alert.alarmTone.toString());
        startActivity(intent);
    }
}
