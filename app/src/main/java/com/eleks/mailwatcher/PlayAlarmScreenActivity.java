package com.eleks.mailwatcher;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.eleks.mailwatcher.model.AlertDBHelper;
import com.eleks.mailwatcher.model.MailMessageRec;

public class PlayAlarmScreenActivity extends AppCompatActivity {
    public final String TAG = this.getClass().getSimpleName();

    private WakeLock mWakeLock;
    private MediaPlayer mPlayer;

    public static final String KEY_MAIL_MESSAGE = "MAIL_MESSAGE";
    public static final int WAKELOCK_TIMEOUT = 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_alarm_screen);

        String name = getIntent().getStringExtra(AlertDBHelper.Alert.COLUMN_NAME);
        String mail = getIntent().getStringExtra(AlertDBHelper.Alert.COLUMN_USER_ACCOUNT);
        String label = getIntent().getStringExtra(AlertDBHelper.Alert.COLUMN_LABEL_NAME);
        String tone = getIntent().getStringExtra(AlertDBHelper.Alert.COLUMN_ALARM_TONE);
        MailMessageRec msgRec = (MailMessageRec) getIntent().getParcelableExtra(KEY_MAIL_MESSAGE);

        TextView tvName = (TextView) findViewById(R.id.alarm_name);
        tvName.setText(name);
        TextView tvMail = (TextView) findViewById(R.id.alarm_mail);
        tvMail.setText(mail);
        TextView tvLabel = (TextView) findViewById(R.id.alarm_label);
        tvLabel.setText(label);
        TextView tvFrom = (TextView) findViewById(R.id.mail_from);
        tvFrom.setText(msgRec != null ? msgRec.getFrom() : "");
        TextView tvTo = (TextView) findViewById(R.id.mail_to);
        tvTo.setText(msgRec != null ? msgRec.getTo() : "");
        TextView tvSubject = (TextView) findViewById(R.id.mail_subject);
        tvSubject.setText(msgRec != null ? msgRec.getSubject() : "");
        startPlayer(tone);


        //Ensure wakelock release
        Runnable releaseWakelock = new Runnable() {
            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

                if (mWakeLock != null && mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
                stopPlayer();
            }
        };
        new Handler().postDelayed(releaseWakelock, WAKELOCK_TIMEOUT);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        // Set the window to keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        // Acquire wakelock
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (mWakeLock == null) {
            mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
        }
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
            Log.i(TAG, "Wakelock aquired!!");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            Log.i(TAG, "Wakelock released!!");
        }
        stopPlayer();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //if (mPlayer == null)
            outState.putCharSequence(AlertDBHelper.Alert.COLUMN_ALARM_TONE, null);
        super.onSaveInstanceState(outState);
    }

    private void startPlayer(String tone) {
        //Play alarm tone
        if (mPlayer != null)
            return;
        if (TextUtils.isEmpty(tone))
            return;
        Log.d(TAG, "Player start");
        mPlayer = new MediaPlayer();
        try {
            if (tone != null && !tone.equals("")) {
                Uri toneUri = Uri.parse(tone);
                if (toneUri != null) {
                    mPlayer.setDataSource(this, toneUri);
                    mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                    mPlayer.setLooping(true);
                    mPlayer.prepare();
                    mPlayer.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlayer() {
        if (mPlayer == null)
            return;

        Log.d(TAG, "Player stop");
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
    }

    public void onDismissClick(View view) {
        finish();
    }

}
