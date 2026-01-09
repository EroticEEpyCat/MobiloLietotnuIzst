package com.example.crazyapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "ai_prompts.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "prompts";
    private static final String COL_ID = "id";
    private static final String COL_PROMPT = "prompt";
    private static final String COL_ANSWER = "answer";

    public DBHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_PROMPT + " TEXT," +
                COL_ANSWER + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertPrompt(String prompt, String answer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PROMPT, prompt);
        cv.put(COL_ANSWER, answer);
        return db.insert(TABLE_NAME, null, cv) != -1;
    }

    public Cursor getAllPrompts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_ID + " DESC", null);
    }
}
