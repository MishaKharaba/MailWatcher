package com.eleks.mailwatcher.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.google.api.services.gmail.model.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "mail-watcher.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        doUpgrade(db, 0, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        doUpgrade(db, oldVersion, newVersion);
    }

    private void doUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion + 1; i <= newVersion; i++) {
            switch (i) {
                case 1:
                    upgrade1(db);
                    break;
                case 2:
                    upgrade2(db);
                    break;
            }
        }
    }

    private void upgrade1(SQLiteDatabase db) {
        String sqlCreateAlert = "CREATE TABLE " + AlertModel.TABLE_NAME + " (" +
                AlertModel._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                AlertModel.NAME + " TEXT," +
                AlertModel.TONE + " TEXT," +
                AlertModel.ENABLED + " BOOLEAN," +
                AlertModel.ACCOUNT_TYPE + " INTEGER," +
                AlertModel.USER_ACCOUNT + " TEXT," +
                AlertModel.LABEL_ID + " TEXT," +
                AlertModel.LABEL_NAME + " TEXT," +
                AlertModel.HISTORY_ID + " TEXT," +
                AlertModel.LAST_CHECK_TS + " INTEGER," +
                AlertModel.LAST_ALARM_TS + " INTEGER," +
                AlertModel.LAST_ERROR + " TEXT," +
                AlertModel.LAST_MESSAGE_ID + " INTEGER" +
                " )";
        db.execSQL(sqlCreateAlert);

        String sqlCreateMessage = "CREATE TABLE " + MessageModel.TABLE_NAME + " (" +
                MessageModel._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MessageModel.ALERT_ID + " INTEGER," +
                MessageModel.FROM + " TEXT," +
                MessageModel.TO + " TEXT," +
                MessageModel.SUBJECT + " TEXT" +
                " )";
        db.execSQL(sqlCreateMessage);
    }

    private void upgrade2(SQLiteDatabase db) {
    }

    //transaction
    public void beginTransaction() {
        getWritableDatabase().beginTransaction();
    }

    public void commitTransaction() {
        getWritableDatabase().setTransactionSuccessful();
    }

    public void endTransaction() {
        getWritableDatabase().endTransaction();
    }

    //Alert model
    public long createAlert(AlertModel model) {
        ContentValues values = model.toContentValues();
        long id = getWritableDatabase().insertOrThrow(AlertModel.TABLE_NAME, null, values);
        model.id = id;
        return id;
    }

    public void updateAlert(AlertModel model) {
        ContentValues values = model.toContentValues();
        getWritableDatabase().update(AlertModel.TABLE_NAME, values,
                AlertModel._ID + " = ?", new String[]{String.valueOf(model.id)});
    }

    public AlertModel getAlert(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM " + AlertModel.TABLE_NAME +
                " WHERE " + AlertModel._ID + " = " + id;
        AlertModel model = null;
        Cursor c = db.rawQuery(select, null);
        if (c.moveToNext()) {
            model = AlertModel.Create(c);
        }
        c.close();
        return model;
    }

    public List<AlertModel> getAlerts() {
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM " + AlertModel.TABLE_NAME;
        Cursor c = db.rawQuery(select, null);
        List<AlertModel> alertList = new ArrayList<>();
        while (c.moveToNext()) {
            alertList.add(AlertModel.Create(c));
        }
        if (alertList.isEmpty()) {
            alertList = null;
        }
        c.close();
        return alertList;
    }

    public void deleteAlert(long id) {
        deleteAlertMessages(id);
        getWritableDatabase().delete(AlertModel.TABLE_NAME,
                AlertModel._ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public boolean hasActiveAlerts() {
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT 1 FROM " + AlertModel.TABLE_NAME +
                " WHERE " + AlertModel.ENABLED + " <> 0";
        Cursor c = db.rawQuery(select, null);
        boolean b = c.moveToNext();
        c.close();
        return b;
    }

    //Message model
    public long createMessage(MessageModel model) {
        long id = getWritableDatabase().insertOrThrow(MessageModel.TABLE_NAME, null,
                model.toContentValues());
        model.id = id;
        return id;
    }

    public void updateMessage(MessageModel model) {
        ContentValues values = model.toContentValues();
        getWritableDatabase().update(MessageModel.TABLE_NAME, values,
                MessageModel._ID + " = ?",
                new String[]{String.valueOf(model.id)});
    }

    public int deleteMessage(long id) {
        return getWritableDatabase().delete(MessageModel.TABLE_NAME,
                MessageModel._ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public int deleteAlertMessages(long alertId) {
        return getWritableDatabase().delete(MessageModel.TABLE_NAME,
                MessageModel.ALERT_ID + " = ?",
                new String[]{String.valueOf(alertId)});
    }

    public MessageModel findMessage(Long id) {
        if (id != null) {
            MessageModel message = getMessage(id);
            return message;
        } else {
            return null;
        }
    }

    public MessageModel getMessage(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM " + MessageModel.TABLE_NAME + " WHERE " +
                MessageModel._ID + " = " + id;
        MessageModel model = null;
        Cursor c = db.rawQuery(select, null);
        if (c.moveToNext()) {
            model = MessageModel.Create(c);
        }
        c.close();
        return model;
    }
}
