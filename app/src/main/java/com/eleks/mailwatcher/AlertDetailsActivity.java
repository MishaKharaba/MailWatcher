package com.eleks.mailwatcher;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.eleks.mailwatcher.model.AlertDBHelper;
import com.eleks.mailwatcher.model.AlertModel;
import com.eleks.mailwatcher.model.LabelRec;

import java.util.List;

public class AlertDetailsActivity extends AppCompatActivity implements IAccountSelector.Result {
    private AlertDBHelper dbHelper = new AlertDBHelper(this);
    private AlertModel alert;
    private GmailAccountSelector gmailSelector;
    private ExchangeAccountSelector exchangeSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gmailSelector = new GmailAccountSelector(this, this);
        exchangeSelector = new ExchangeAccountSelector(this, this);

        setContentView(R.layout.activity_alert_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final View alarmTone = findViewById(R.id.alarm_tone);
        View alarmToneSelect = findViewById(R.id.alarm_tone_select);
        alarmToneSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                        Settings.System.DEFAULT_ALARM_ALERT_URI);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                        RingtoneManager.TYPE_ALL);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        (Parcelable) alarmTone.getTag());
                startActivityForResult(intent, 1);
            }
        });

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAccountAndFolder();
            }
        };
        findViewById(R.id.gmail).setOnClickListener(clickListener);
        findViewById(R.id.exchange).setOnClickListener(clickListener);

        Button accountSelect = (Button) findViewById(R.id.account_select);
        accountSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAccountSelector();
            }
        });

        Button folderSelect = (Button) findViewById(R.id.folder_select);
        folderSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFolderSelector();
            }
        });

        long id = getIntent().getExtras().getLong("id");

        if (id < 0) {
            alert = new AlertModel(id);
            alert.alarmTone = Settings.System.DEFAULT_ALARM_ALERT_URI;
        } else {
            alert = dbHelper.getAlert(id);
        }
        alertToView(alert);
        getAccountSelector().setAccount(alert.userAccount);
    }

    private void resetAccountAndFolder() {
        TextView accountName = (TextView) findViewById(R.id.account_name);
        accountName.setText(null);
        TextView folderName = (TextView) findViewById(R.id.folder_name);
        folderName.setText(null);
        folderName.setTag(null);
    }

    private void alertToView(AlertModel alert) {
        EditText edtName = (EditText) findViewById(R.id.alert_details_name);
        edtName.setText(alert.name);
        TextView alarmTone = (TextView) findViewById(R.id.alarm_tone);
        alarmTone.setTag(alert.alarmTone);
        alarmTone.setText(RingtoneManager.getRingtone(this, alert.alarmTone).getTitle(this));
        TextView accountName = (TextView) findViewById(R.id.account_name);
        accountName.setText(alert.userAccount);
        TextView folderName = (TextView) findViewById(R.id.folder_name);
        folderName.setText(alert.labelName);
        folderName.setTag(alert.labelId);
        RadioButton accType = (RadioButton) findViewById(
                alert.accountType == AlertModel.AccountType.gmail
                        ? R.id.gmail : R.id.exchange);
        accType.setChecked(true);
    }

    private void viewToAlert(AlertModel alert) {
        EditText edtName = (EditText) findViewById(R.id.alert_details_name);
        alert.name = edtName.getText().toString();
        View alarmTone = findViewById(R.id.alarm_tone);
        alert.alarmTone = (Uri) alarmTone.getTag();
        TextView accountName = (TextView) findViewById(R.id.account_name);
        alert.userAccount = accountName.getText().toString();
        TextView folderName = (TextView) findViewById(R.id.folder_name);
        alert.labelId = (String) folderName.getTag();
        alert.labelName = folderName.getText().toString();
        RadioButton accType = (RadioButton) findViewById(R.id.gmail);
        alert.accountType = getAccountType();
    }

    private AlertModel.AccountType getAccountType() {
        RadioButton accType = (RadioButton) findViewById(R.id.gmail);
        return accType.isChecked() ? AlertModel.AccountType.gmail : AlertModel.AccountType.exchange;
    }

    protected IAccountSelector getAccountSelector() {
        switch (getAccountType()) {
            case gmail:
                return gmailSelector;
            case exchange:
                return exchangeSelector;
            default:
                return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_alert_details, menu);
        return true;
    }

    private void startAccountSelector() {
        TextView account = (TextView) findViewById(R.id.account_name);
        getAccountSelector().Select(account.getText().toString());
    }

    @Override
    public void Selected(String accountName) {
        TextView account = (TextView) findViewById(R.id.account_name);
        account.setText(getAccountSelector().getAccountName());
    }

    private void startFolderSelector() {
        final List<LabelRec> folders = getAccountSelector().getFolders();

        if (folders == null || folders.size() == 0) {
            Toast.makeText(this, "No folders loaded", Toast.LENGTH_SHORT).show();
        } else {
            String names[] = new String[folders.size()];
            for (int i = 0; i < folders.size(); i++) {
                names[i] = folders.get(i).name;
            }

            AlertDialog selectFolderDialog = new AlertDialog.Builder(this)
                    .setTitle("Pick Account")
                    .setItems(
                            names,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    LabelRec folder = folders.get(which);
                                    TextView folderName = (TextView) findViewById(R.id.folder_name);
                                    folderName.setText(folder.name);
                                    folderName.setTag(folder.id);
                                }
                            }).create();
            selectFolderDialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (getAccountSelector().onActivityResult(requestCode, resultCode, data)) {
            return; //handled
        }

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1: {
                    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    View toneSelector = findViewById(R.id.alarm_tone);
                    toneSelector.setTag(uri);
                    TextView txtToneSelection = (TextView) findViewById(R.id.alarm_tone);
                    txtToneSelection.setText(RingtoneManager.getRingtone(this, uri).getTitle(this));
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            }
            case R.id.action_save_alert_details: {
                viewToAlert(alert);
                if (alert.id < 0) {
                    dbHelper.createAlert(alert);
                } else {
                    dbHelper.updateAlert(alert);
                }
                setResult(RESULT_OK);
                finish();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
