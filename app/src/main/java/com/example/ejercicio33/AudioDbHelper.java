package com.example.ejercicio33;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class AudioDbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "audio_recorder.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_RECORDINGS = "recordings";
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "audio_name";
    public static final String COL_DATA = "audio_data";
    public static final String COL_DURATION = "duration";
    public static final String COL_DATE = "record_date";

    public AudioDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_RECORDINGS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_DATA + " BLOB NOT NULL, "
                + COL_DURATION + " INTEGER, "
                + COL_DATE + " TEXT"
                + ");";
        db.execSQL(sql);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDINGS);
        onCreate(db);
    }
    // Métodos para insertar, consultar y borrar están en respuestas previas (copia y adapta):
    public long insertRecording(String name, byte[] audioBytes, int durationSeconds, String recordDate) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_DATA, audioBytes);
        values.put(COL_DURATION, durationSeconds);
        values.put(COL_DATE, recordDate);
        return getWritableDatabase().insert(TABLE_RECORDINGS, null, values);
    }
    public List<AudioRecording> getAllRecordings() {
        List<AudioRecording> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(
                TABLE_RECORDINGS,
                new String[]{COL_ID, COL_NAME, COL_DURATION, COL_DATE},
                null, null, null, null,
                COL_ID + " DESC"
        );
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    long id = c.getLong(c.getColumnIndexOrThrow(COL_ID));
                    String name = c.getString(c.getColumnIndexOrThrow(COL_NAME));
                    int duration = c.getInt(c.getColumnIndexOrThrow(COL_DURATION));
                    String date = c.getString(c.getColumnIndexOrThrow(COL_DATE));
                    list.add(new AudioRecording(id, name, duration, date));
                }
            } finally {
                c.close();
            }
        }
        return list;
    }
    public byte[] getAudioDataById(long id) {
        Cursor c = getReadableDatabase().query(
                TABLE_RECORDINGS,
                new String[]{COL_DATA},
                COL_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null
        );
        byte[] data = null;
        if (c != null) {
            try {
                if (c.moveToFirst()) data = c.getBlob(c.getColumnIndexOrThrow(COL_DATA));
            } finally { c.close(); }
        }
        return data;
    }
    public int deleteRecording(long id) {
        return getWritableDatabase().delete(TABLE_RECORDINGS, COL_ID + "=?", new String[]{String.valueOf(id)});
    }
}
