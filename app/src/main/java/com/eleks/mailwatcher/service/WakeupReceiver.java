package com.eleks.mailwatcher.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.eleks.mailwatcher.SettingsActivity;

public class WakeupReceiver extends WakefulBroadcastReceiver {
    public final static String TAG = WakeupReceiver.class.getSimpleName();

    public static void activate(Context context) {
        Log.d(TAG, "activate wakeup");
        Intent intent = new Intent(context, WakeupReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int intervalMin = getCheckInterval(context);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 60000, intervalMin * 60000, pendingIntent);

        //boot receiver
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private static int getCheckInterval(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String interval = sharedPref.getString(SettingsActivity.KEY_CHECK_MAIL_INTERVAL, "1");
        int intervalMin = getIntValue(interval, 1);
        if (intervalMin < 1) {
            intervalMin = 1;
        }
        Log.d(TAG, "Settings: " + SettingsActivity.KEY_CHECK_MAIL_INTERVAL + "=" + intervalMin);
        return intervalMin;
    }

    private static int getIntValue(String strValue, int defValue) {
        try {
            return Integer.parseInt(strValue);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static void cancel(Context context) {
        Log.d(TAG, "cancel wakeup");
        Intent intent = new Intent(context, WakeupReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmMgr.cancel(pendingIntent);
        }

        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, AlertService.class);
        startWakefulService(context, service);
    }
}
