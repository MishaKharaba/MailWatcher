package com.eleks.mailwatcher;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.eleks.mailwatcher.model.AlertDBHelper;
import com.eleks.mailwatcher.model.AlertModel;
import com.eleks.mailwatcher.service.AlertService;

import java.util.ArrayList;

public class AlertListActivity extends AppCompatActivity
{
    private ListView mListView;
    private AlertListAdapter mAdapter;
    private AlertDBHelper dbHelper = new AlertDBHelper(this);

    protected void setListAdapter(ListAdapter adapter)
    {
        mListView.setAdapter(adapter);
    }

    protected ListAdapter getListAdapter()
    {
        ListAdapter adapter = mListView.getAdapter();
        if (adapter instanceof HeaderViewListAdapter)
        {
            return ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }
        else
        {
            return adapter;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mListView = (ListView) findViewById(android.R.id.list);
        mAdapter = new AlertListAdapter(this, dbHelper.getAlerts());
        setListAdapter(mAdapter);
        AlertService.update(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_alert_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_add_new_alert:
            {
                startAlertDetailsActivity(-1);
                return true;
            }
            case R.id.action_settings:
            {
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            mAdapter.setAlerts(dbHelper.getAlerts());
            mAdapter.notifyDataSetChanged();
            AlertService.update(this);
        }
    }

    public void startAlertDetailsActivity(long id)
    {
        Intent intent = new Intent(this, AlertDetailsActivity.class);
        intent.putExtra("id", id);
        startActivityForResult(intent, 0);
    }

    public void setAlertEnabled(long id, boolean isEnabled)
    {
        AlertModel alert = dbHelper.getAlert(id);
        alert.isEnabled = isEnabled;
        dbHelper.updateAlert(alert);

        mAdapter.setAlerts(dbHelper.getAlerts());
        mAdapter.notifyDataSetChanged();
        AlertService.update(this);
    }

    public void deleteAlarm(long id)
    {
        final long alertId = id;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please confirm")
                .setTitle("Delete set?")
                .setCancelable(true)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dbHelper.deleteAlert(alertId);
                        mAdapter.setAlerts(dbHelper.getAlerts());
                        mAdapter.notifyDataSetChanged();
                        AlertService.update(builder.getContext());
                    }
                }).show();
    }
}
