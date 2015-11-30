package com.eleks.mailwatcher.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

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

    public static final String TABLE_NAME = "t_alert";
    public static final String _ID = "c_id";
    public static final String NAME = "c_name";
    public static final String TONE = "c_tone";
    public static final String ENABLED = "c_enabled";
    public static final String ACCOUNT_TYPE = "c_account_type";
    public static final String USER_ACCOUNT = "c_user_account";
    public static final String LABEL_ID = "c_label_id";
    public static final String LABEL_NAME = "c_label_name";
    public static final String HISTORY_ID = "c_history_id";
    public static final String LAST_CHECK_TS = "c_last_check_ts";
    public static final String LAST_ALARM_TS = "c_last_alarm_ts";
    public static final String LAST_ERROR = "c_last_error";
    public static final String LAST_MESSAGE_ID = "c_last_message_id";
    public static final String FILTER_FROM = "c_filter_from";
    public static final String FILTER_TO = "c_filter_to";
    public static final String FILTER_SUBJECT = "c_filter_subject";


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

    public Long lastMessageId;

    public String filterFrom;
    public String filterTo;
    public String filterSubject;

    public AlertModel(long id) {
        this.id = id;
    }

    public static AlertModel Create(Cursor c) {
        long id = c.getLong(c.getColumnIndex(_ID));
        AlertModel m = new AlertModel(id);
        m.name = c.getString(c.getColumnIndex(NAME));
        m.alarmTone = !"".equals(c.getString(c.getColumnIndex(TONE)))
                ? Uri.parse(c.getString(c.getColumnIndex(TONE))) : null;
        m.isEnabled = c.getInt(c.getColumnIndex(ENABLED)) != 0;
        m.accountType = AccountType.getValue(
                c.getInt(c.getColumnIndex(ACCOUNT_TYPE)));
        m.userAccount = c.getString(c.getColumnIndex(USER_ACCOUNT));
        m.labelId = c.getString(c.getColumnIndex(LABEL_ID));
        m.labelName = c.getString(c.getColumnIndex(LABEL_NAME));
        m.historyId = c.getString(c.getColumnIndex(HISTORY_ID));
        m.historyId = c.getString(c.getColumnIndex(HISTORY_ID));
        long aLong = c.getLong(c.getColumnIndex(LAST_CHECK_TS));
        m.lastCheckDate = aLong > 0 ? new Date(aLong) : null;
        aLong = c.getLong(c.getColumnIndex(LAST_ALARM_TS));
        m.lastAlarmDate = aLong > 0 ? new Date(aLong) : null;
        m.lastError = c.getString(c.getColumnIndex(LAST_ERROR));
        m.lastMessageId = c.isNull(c.getColumnIndex(LAST_MESSAGE_ID))
                ? null : c.getLong(c.getColumnIndex(LAST_MESSAGE_ID));
        m.filterFrom = c.getString(c.getColumnIndex(FILTER_FROM));
        m.filterTo = c.getString(c.getColumnIndex(FILTER_TO));
        m.filterSubject = c.getString(c.getColumnIndex(FILTER_SUBJECT));

        return m;
    }

    public void fromContentValues(ContentValues cv) {
        id = cv.getAsLong(_ID);
        name = cv.getAsString(NAME);
        String tone = cv.getAsString(TONE);
        alarmTone = TextUtils.isEmpty(tone) ? null : Uri.parse(tone);
        isEnabled = cv.getAsBoolean(ENABLED);
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(NAME, name);
        values.put(TONE, (alarmTone != null) ? alarmTone.toString() : "");
        values.put(ENABLED, isEnabled);
        values.put(ACCOUNT_TYPE, accountType.ordinal());
        values.put(USER_ACCOUNT, userAccount);
        values.put(LABEL_ID, labelId);
        values.put(LABEL_NAME, labelName);
        values.put(HISTORY_ID, historyId);
        values.put(LAST_CHECK_TS,
                lastCheckDate != null ? lastCheckDate.getTime() : 0);
        values.put(LAST_ALARM_TS,
                lastAlarmDate != null ? lastAlarmDate.getTime() : 0);
        values.put(LAST_ERROR, lastError);
        values.put(LAST_MESSAGE_ID, lastMessageId);
        values.put(FILTER_FROM, filterFrom);
        values.put(FILTER_TO, filterTo);
        values.put(FILTER_SUBJECT, filterSubject);
        return values;
    }

}
