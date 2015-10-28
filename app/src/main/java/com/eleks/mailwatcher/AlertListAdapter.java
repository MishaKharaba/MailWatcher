package com.eleks.mailwatcher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.eleks.mailwatcher.model.AlertModel;

import java.util.List;

public class AlertListAdapter extends BaseAdapter
{
    private final Context mContext;
    private final List<AlertModel> mAlerts;

    public AlertListAdapter(Context context, List<AlertModel> alerts)
    {
        mContext = context;
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

        AlertModel model = (AlertModel) getItem(position);
        TextView txtName = (TextView) view.findViewById(R.id.alert_item_name);
        txtName.setText(model.name);

        view.setTag(Long.valueOf(model.id));
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
