package com.example.to_dolist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TodoItemDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "todolist.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "todoitem";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CATEGORY_ID = "category_id "; // RQ-0007
    public static final String COLUMN_DUE_TIME = "due_time"; // HH:MM 형식의 시간 (TEXT)
    public static final String COLUMN_IS_COMPLETED = "is_completed"; // RQ-0005 (0: false, 1: true)

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_TITLE + " TEXT NOT NULL," +
                    COLUMN_CATEGORY_ID + " TEXT NOT NULL," +
                    COLUMN_DUE_TIME + " TEXT," +
                    COLUMN_IS_COMPLETED + " INTEGER DEFAULT 0)";

    public TodoItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 데이터 마이그레이션이 복잡하므로, 여기서는 간단하게 삭제 후 재생성 처리
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}