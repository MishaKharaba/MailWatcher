package com.eleks.mailwatcher.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AlertModel {
    public enum AccountType {
        gmail, exchange;

        public static AccountType getValue(int i) {
            if (i >= 0 && i < values().length)
                return values()[i];
            return null;
        }
    }

    public long id;
    public String name;
    public Uri alarmTone;
    public boolean isEnabled;

    public AccountType accountType = AccountType.gmail;
    public String userAccount;
    public String labelId;
    public String labelName;

    public String historyId;
    public Date lastCheckDate;
    public Date lastAlarmDate;
    public String lastError;

    public AlertModel(long id) {
        this.id = id;
    }

    public static List<AlertModel> create() {
        List<AlertModel> result = new ArrayList<>();
        for (int i = 1; i < 20; i++) {
            AlertModel model = new AlertModel(i);
            model.name = "Name " + i;
            result.add(model);
        }
        return result;
    }
}
