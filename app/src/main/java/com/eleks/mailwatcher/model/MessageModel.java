package com.eleks.mailwatcher.model;

import android.content.ContentValues;
import android.database.Cursor;

public class MessageModel {
    public static final String TABLE_NAME = "t_message";
    public static final String _ID = "c_id";
    public static final String ALERT_ID = "c_alert_id";
    public static final String TO = "c_to";
    public static final String FROM = "c_from";
    public static final String SUBJECT = "c_subject";

    public long id;
    public long alertId;
    public String to;
    public String from;
    public String subject;

    public MessageModel() {
        this.id = -1;
    }

    public MessageModel(long id) {
        this.id = id;
    }

    public static MessageModel Create(Cursor c) {
        long id = c.getLong(c.getColumnIndex(_ID));
        MessageModel m = new MessageModel(id);
        m.alertId = c.getLong(c.getColumnIndex(ALERT_ID));
        m.from = c.getString(c.getColumnIndex(FROM));
        m.to = c.getString(c.getColumnIndex(TO));
        m.subject = c.getString(c.getColumnIndex(SUBJECT));
        return m;
    }

    public void Update(MailMessageRec rec) {
        from = rec.getFrom();
        to = rec.getTo();
        subject = rec.getSubject();
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        //cv.put(_ID, id);
        cv.put(ALERT_ID, alertId);
        cv.put(FROM, from);
        cv.put(TO, to);
        cv.put(SUBJECT, subject);
        return cv;
    }
}
