package com.eleks.mailwatcher.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

public class AlertDBHelper extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "mailwatcher.db";

    public static abstract class Alert implements BaseColumns
    {
        public static final String TABLE_NAME = "alert";
        public static final String _ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ALARM_TONE = "tone";
        public static final String COLUMN_ENABLED = "enabled";
        public static final String COLUMN_USER_ACCOUNT = "userAccount";
        public static final String COLUMN_LABEL_ID = "labelId";
        public static final String COLUMN_LABEL_NAME = "labelName";
        public static final String COLUMN_HISTORY_ID = "historyId";
    }

    private static final String SQL_CREATE_ALERT = "CREATE TABLE " + Alert.TABLE_NAME + " (" +
            Alert._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            Alert.COLUMN_NAME + " TEXT," +
            Alert.COLUMN_ALARM_TONE + " TEXT," +
            Alert.COLUMN_ENABLED + " BOOLEAN," +
            Alert.COLUMN_USER_ACCOUNT + " TEXT," +
            Alert.COLUMN_LABEL_ID + " TEXT," +
            Alert.COLUMN_LABEL_NAME + " TEXT," +
            Alert.COLUMN_HISTORY_ID + " TEXT" +
            " )";

    private static final String SQL_DELETE_ALERT = "DROP TABLE IF EXISTS " + Alert.TABLE_NAME;

    public AlertDBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(SQL_CREATE_ALERT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(SQL_DELETE_ALERT);
        onCreate(db);
    }

    private AlertModel loadModel(Cursor c)
    {
        long id = c.getLong(c.getColumnIndex(Alert._ID));
        AlertModel model = new AlertModel(id);
        model.name = c.getString(c.getColumnIndex(Alert.COLUMN_NAME));
        model.alarmTone = !"".equals(c.getString(c.getColumnIndex(Alert.COLUMN_ALARM_TONE)))
                ? Uri.parse(c.getString(c.getColumnIndex(Alert.COLUMN_ALARM_TONE))) : null;
        model.isEnabled = c.getInt(c.getColumnIndex(Alert.COLUMN_ENABLED)) != 0;
        model.userAccount = c.getString(c.getColumnIndex(Alert.COLUMN_USER_ACCOUNT));
        model.labelId = c.getString(c.getColumnIndex(Alert.COLUMN_LABEL_ID));
        model.labelName = c.getString(c.getColumnIndex(Alert.COLUMN_LABEL_NAME));
        model.historyId = c.getString(c.getColumnIndex(Alert.COLUMN_HISTORY_ID));
        return model;
    }

    private ContentValues loadContent(AlertModel model)
    {
        ContentValues values = new ContentValues();
        values.put(Alert.COLUMN_NAME, model.name);
        values.put(Alert.COLUMN_ALARM_TONE, (model.alarmTone != null) ? model.alarmTone.toString() : "");
        values.put(Alert.COLUMN_ENABLED, model.isEnabled);
        values.put(Alert.COLUMN_USER_ACCOUNT, model.userAccount);
        values.put(Alert.COLUMN_LABEL_ID, model.labelId);
        values.put(Alert.COLUMN_LABEL_NAME, model.labelName);
        values.put(Alert.COLUMN_HISTORY_ID, model.historyId);
        return values;
    }

    public long createAlert(AlertModel model)
    {
        ContentValues values = loadContent(model);
        long id = getWritableDatabase().insertOrThrow(Alert.TABLE_NAME, null, values);
        model.id = id;
        return id;
    }

    public int updateAlert(AlertModel model)
    {
        ContentValues values = loadContent(model);
        int nRows = getWritableDatabase().update(Alert.TABLE_NAME, values,
                Alert._ID + " = ?", new String[]{String.valueOf(model.id)});
        return nRows;
    }

    public AlertModel getAlert(long id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM " + Alert.TABLE_NAME + " WHERE " + Alert._ID + " = " + id;
        Cursor c = db.rawQuery(select, null);
        if (c.moveToNext())
        {
            return loadModel(c);
        }
        return null;
    }

    public List<AlertModel> getAlerts()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM " + Alert.TABLE_NAME;
        Cursor c = db.rawQuery(select, null);
        List<AlertModel> alertList = new ArrayList<AlertModel>();
        while (c.moveToNext())
        {
            alertList.add(loadModel(c));
        }
        if (!alertList.isEmpty())
        {
            return alertList;
        }
        return null;
    }

    public int deleteAlert(long id)
    {
        return getWritableDatabase().delete(Alert.TABLE_NAME, Alert._ID + " = ?", new String[]{String.valueOf(id)});
    }

    public boolean hasActiveAlerts()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT 1 FROM " + Alert.TABLE_NAME + " WHERE " + Alert.COLUMN_ENABLED + " <> 0";
        Cursor c = db.rawQuery(select, null);
        if (c.moveToNext())
        {
            return true;
        }
        return false;
    }

}
