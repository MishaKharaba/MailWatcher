package com.eleks.mailwatcher;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.eleks.mailwatcher.model.AlertDBHelper;
import com.eleks.mailwatcher.model.AlertModel;

public class AlertDetailsActivity extends AppCompatActivity
{
    private AlertDBHelper dbHelper = new AlertDBHelper(this);
    private AlertModel alert;
    private GmailAccountSelector gmailAccountSelector = new GmailAccountSelector(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final View toneSelector = findViewById(R.id.alarm_tone);
        toneSelector.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                        Settings.System.DEFAULT_ALARM_ALERT_URI);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                        RingtoneManager.TYPE_ALL);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        (Parcelable) toneSelector.getTag());
                startActivityForResult(intent, 1);
            }
        });
        Button selectAccount = (Button) findViewById(R.id.account_selector);
        selectAccount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startAccountSelector();
            }
        });

        long id = getIntent().getExtras().getLong("id");

        if (id < 0)
        {
            alert = new AlertModel(id);
            alert.alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }
        else
        {
            alert = dbHelper.getAlert(id);
        }
        alertToView(alert);
    }

    private void alertToView(AlertModel alert)
    {
        EditText edtName = (EditText) findViewById(R.id.alert_details_name);
        edtName.setText(alert.name);
        View toneSelector = findViewById(R.id.alarm_tone);
        toneSelector.setTag(alert.alarmTone);
        TextView txtToneSelection = (TextView) findViewById(R.id.alarm_label_tone_selection);
        txtToneSelection.setText(RingtoneManager.getRingtone(this, alert.alarmTone).getTitle(this));
        EditText edtAccount = (EditText) findViewById(R.id.alert_account_name);
        edtAccount.setText(alert.userAccount);
        EditText edtLabelName = (EditText) findViewById(R.id.alert_label_name);
        edtLabelName.setText(alert.labelName);
    }

    private void viewToAlert(AlertModel alert)
    {
        EditText edtName = (EditText) findViewById(R.id.alert_details_name);
        alert.name = edtName.getText().toString();
        View toneSelector = findViewById(R.id.alarm_tone);
        alert.alarmTone = (Uri) toneSelector.getTag();
        EditText edtAccount = (EditText) findViewById(R.id.alert_account_name);
        alert.userAccount = edtAccount.getText().toString();
        EditText edtLabelName = (EditText) findViewById(R.id.alert_label_name);
        alert.labelName = edtLabelName.getText().toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_alert_details, menu);
        return true;
    }

    private void startAccountSelector()
    {
        EditText account = (EditText) findViewById(R.id.alert_account_name);
        gmailAccountSelector.Select(account.getText().toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (gmailAccountSelector.onActivityResult(requestCode, resultCode, data))
        {

            if (resultCode == RESULT_OK)
            {
                EditText account = (EditText) findViewById(R.id.alert_account_name);
                account.setText(gmailAccountSelector.getmAccountName());
            }
            return; //handled
        }

        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case 1:
                {
                    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    View toneSelector = findViewById(R.id.alarm_tone);
                    toneSelector.setTag(uri);
                    TextView txtToneSelection = (TextView) findViewById(R.id.alarm_label_tone_selection);
                    txtToneSelection.setText(RingtoneManager.getRingtone(this, uri).getTitle(this));
                    break;
                }
                default:
                {
                    break;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
            {
                finish();
                break;
            }
            case R.id.action_save_alert_details:
            {
                viewToAlert(alert);
                if (alert.id < 0)
                {
                    dbHelper.createAlert(alert);
                }
                else
                {
                    dbHelper.updateAlert(alert);
                }
                setResult(RESULT_OK);
                finish();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
