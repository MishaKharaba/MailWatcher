package com.eleks.mailwatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.eleks.mailwatcher.model.AlertDBHelper;
import com.eleks.mailwatcher.model.AlertModel;
import com.eleks.mailwatcher.service.AlertService;

public class AlertListActivity extends AppCompatActivity {
    public final static String REFRESH = "REFRESH";

    private ListView mListView;
    private AlertListAdapter mAdapter;
    private AlertDBHelper dbHelper = new AlertDBHelper(this);
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mListView = (ListView) findViewById(android.R.id.list);
        mAdapter = new AlertListAdapter(this, dbHelper.getAlerts());
        mListView.setAdapter(mAdapter);
        AlertService.update(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(REFRESH);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(REFRESH)) {
                    updateAlertList();
                }
            }
        };
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_alert_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_new_alert: {
                startAlertDetailsActivity(-1);
                return true;
            }
            case R.id.action_settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            updateAlertList();
        }
    }

    public void startAlertDetailsActivity(long id) {
        Intent intent = new Intent(this, AlertDetailsActivity.class);
        intent.putExtra("id", id);
        startActivityForResult(intent, 0);
    }

    public void setAlertEnabled(long id, boolean isEnabled) {
        AlertModel alert = dbHelper.getAlert(id);
        alert.isEnabled = isEnabled;
        if (!isEnabled) {
            alert.historyId = null;
        }
        dbHelper.updateAlert(alert);

        updateAlertList();
    }

    private void updateAlertList() {
        mAdapter.setAlerts(dbHelper.getAlerts());
        mAdapter.notifyDataSetChanged();
        AlertService.update(this);
    }

    public void deleteAlarm(long id) {
        final long alertId = id;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please confirm")
                .setTitle("Delete set?")
                .setCancelable(true)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteAlert(alertId);
                        mAdapter.setAlerts(dbHelper.getAlerts());
                        mAdapter.notifyDataSetChanged();
                        AlertService.update(builder.getContext());
                    }
                }).show();
    }
}
