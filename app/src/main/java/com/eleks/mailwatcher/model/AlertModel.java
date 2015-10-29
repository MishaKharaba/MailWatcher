package com.eleks.mailwatcher.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class AlertModel
{
    public long id;
    public String name;
    public Uri alarmTone;
    public boolean isEnabled;

    public AlertModel(long id)
    {
        this.id = id;
    }

    public static List<AlertModel> create()
    {
        List<AlertModel> result = new ArrayList<>();
        for (int i = 1; i < 20; i++)
        {
            AlertModel model = new AlertModel(i);
            model.name = "Name " + i;
            result.add(model);
        }
        return result;
    }
}
