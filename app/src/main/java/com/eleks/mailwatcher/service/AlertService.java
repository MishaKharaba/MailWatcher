package com.eleks.mailwatcher.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.eleks.mailwatcher.model.AlertDBHelper;

public class AlertService extends IntentService
{
    public final static String TAG = AlertService.class.getSimpleName();

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
            run(intent);
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

    }
}
