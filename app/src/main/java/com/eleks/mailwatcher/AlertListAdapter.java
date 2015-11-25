package com.eleks.mailwatcher;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.eleks.mailwatcher.model.AlertModel;
import com.eleks.mailwatcher.model.DBHelper;
import com.eleks.mailwatcher.model.MessageModel;

import java.util.List;

public class AlertListAdapter extends BaseAdapter {
    private final Context mContext;
    private List<AlertModel> mAlerts;
    private final DBHelper dbHelper;

    public AlertListAdapter(Context context, List<AlertModel> alerts) {
        mContext = context;
        mAlerts = alerts;
        dbHelper = new DBHelper(context);
    }

    public void setAlerts(List<AlertModel> alerts) {
        mAlerts = alerts;
    }

    @Override
    public int getCount() {
        if (mAlerts != null)
            return mAlerts.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mAlerts != null)
            return mAlerts.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (mAlerts != null)
            return mAlerts.get(position).id;
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.alert_list_item, parent, false);
        }

        AlertModel alert = (AlertModel) getItem(position);

        TextView txtName = (TextView) view.findViewById(R.id.alert_item_name);
        txtName.setText(alert.name);

        TextView txtTone = (TextView) view.findViewById(R.id.alarm_tone);
        Ringtone ringtone = RingtoneManager.getRingtone(mContext, alert.alarmTone);
        txtTone.setText(ringtone.getTitle(mContext));

        TextView txtAccount = (TextView) view.findViewById(R.id.account_name);
        txtAccount.setText(alert.userAccount);

        TextView txtLabel = (TextView) view.findViewById(R.id.alert_label_name);
        txtLabel.setText(alert.labelName);

        TextView txtLastChecked = (TextView) view.findViewById(R.id.last_checked);
        java.text.DateFormat df = DateFormat.getDateFormat(view.getContext());
        java.text.DateFormat tf = DateFormat.getTimeFormat(view.getContext());
        if (alert.lastCheckDate != null && alert.historyId != null) {
            String msg = df.format(alert.lastCheckDate) + " " + tf.format(alert.lastCheckDate);
            txtLastChecked.setText(msg);
            txtLastChecked.setVisibility(View.VISIBLE);
        } else {
            txtLastChecked.setText("");
            txtLastChecked.setVisibility(View.GONE);
        }

        MessageModel msg = dbHelper.findMessage(alert.lastMessageId);
        view.findViewById(R.id.mail_group).setVisibility(msg != null ? View.VISIBLE : View.GONE);
        if (msg != null) {
            ((TextView) view.findViewById(R.id.mail_from)).setText(msg.from);
            ((TextView) view.findViewById(R.id.mail_to)).setText(msg.to);
            ((TextView) view.findViewById(R.id.mail_subject)).setText(msg.subject);
        }

        TextView txtError = (TextView) view.findViewById(R.id.error);
        if (alert.lastError != null && alert.isEnabled) {
            txtError.setText(alert.lastError);
            txtError.setVisibility(View.VISIBLE);
        } else {
            txtError.setText("");
            txtError.setVisibility(View.GONE);
        }

        ToggleButton btnEnabled = (ToggleButton) view.findViewById(R.id.alert_item_toggle);
        btnEnabled.setChecked(alert.isEnabled);
        btnEnabled.setTag(alert.id);
        btnEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((AlertListActivity) mContext).setAlertEnabled((Long) buttonView.getTag(), isChecked);
            }
        });

        final View leftInfo = view.findViewById(R.id.left_info);
        leftInfo.setTag(alert.id);
        leftInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((AlertListActivity) mContext).startAlertDetailsActivity((Long) leftInfo.getTag());
            }
        });

        leftInfo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ((AlertListActivity) mContext).deleteAlarm((Long) leftInfo.getTag());
                return true;
            }
        });

        return view;
    }
}
