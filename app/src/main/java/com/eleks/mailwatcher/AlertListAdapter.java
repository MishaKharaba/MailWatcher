package com.eleks.mailwatcher;

import android.content.Context;
import android.media.RingtoneManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.eleks.mailwatcher.model.AlertModel;

import java.util.List;

public class AlertListAdapter extends BaseAdapter
{
    private final Context mContext;
    private List<AlertModel> mAlerts;

    public AlertListAdapter(Context context, List<AlertModel> alerts)
    {
        mContext = context;
        mAlerts = alerts;
    }

    public void setAlerts(List<AlertModel> alerts)
    {
        mAlerts = alerts;
    }

    @Override
    public int getCount()
    {
        if (mAlerts != null)
            return mAlerts.size();
        return 0;
    }

    @Override
    public Object getItem(int position)
    {
        if (mAlerts != null)
            return mAlerts.get(position);
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        if (mAlerts != null)
            return mAlerts.get(position).id;
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        if (view == null)
        {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.alert_list_item, parent, false);
        }

        AlertModel alert = (AlertModel) getItem(position);

        TextView txtName = (TextView) view.findViewById(R.id.alert_item_name);
        txtName.setText(alert.name);

        TextView txtTone = (TextView) view.findViewById(R.id.alarm_label_tone_selection);
        txtTone.setText(RingtoneManager.getRingtone(mContext, alert.alarmTone).getTitle(mContext));

        ToggleButton btnEnabled = (ToggleButton) view.findViewById(R.id.alert_item_toggle);
        btnEnabled.setChecked(alert.isEnabled);
        btnEnabled.setTag(alert.id);
        btnEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                ((AlertListActivity) mContext).setAlertEnabled(((Long) buttonView.getTag()).longValue(), isChecked);
            }
        });

        view.setTag(Long.valueOf(alert.id));
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ((AlertListActivity) mContext).startAlertDetailsActivity(((Long) view.getTag()).longValue());
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                ((AlertListActivity) mContext).deleteAlarm(((Long) view.getTag()).longValue());
                return true;
            }
        });

        return view;
    }
}
