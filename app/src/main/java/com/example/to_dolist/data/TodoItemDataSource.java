package com.example.to_dolist.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class TodoItemDataSource {

    private SQLiteDatabase database;
    private TodoItemDbHelper dbHelper;

    // 모든 열 정의
    private String[] allColumns = {
            TodoItemDbHelper.COLUMN_ID,
            TodoItemDbHelper.COLUMN_TITLE,
            TodoItemDbHelper.COLUMN_CATEGORY_ID,
            TodoItemDbHelper.COLUMN_DUE_TIME,
            TodoItemDbHelper.COLUMN_IS_COMPLETED
    };

    public TodoItemDataSource(Context context) {
        dbHelper = new TodoItemDbHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Cursor를 Task 객체로 변환
    private TodoItem cursorToTask(Cursor cursor) {
        TodoItem item = new TodoItem();
        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(TodoItemDbHelper.COLUMN_ID)));
        item.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(TodoItemDbHelper.COLUMN_TITLE)));
        item.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(TodoItemDbHelper.COLUMN_CATEGORY_ID)));
        item.setDueTime(cursor.getString(cursor.getColumnIndexOrThrow(TodoItemDbHelper.COLUMN_DUE_TIME)));
        item.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(TodoItemDbHelper.COLUMN_IS_COMPLETED)) == 1);
        return item;
    }

    // RQ-0001: 투두(To-Do) 추가
    public long createTask(TodoItem item) {
        ContentValues values = new ContentValues();
        values.put(TodoItemDbHelper.COLUMN_TITLE, item.getTitle());
        values.put(TodoItemDbHelper.COLUMN_CATEGORY_ID, item.getCategoryId());
        values.put(TodoItemDbHelper.COLUMN_DUE_TIME, item.getDueTime());
        values.put(TodoItemDbHelper.COLUMN_IS_COMPLETED, item.isCompleted() ? 1 : 0);

        long insertId = database.insert(TodoItemDbHelper.TABLE_NAME, null, values);
        return insertId; // 이 ID를 사용하여 알림 등록에 활용 가능
    }

    // RQ-0002: 투두 삭제
    public void deleteTask(long taskId) {
        database.delete(TodoItemDbHelper.TABLE_NAME,
                TodoItemDbHelper.COLUMN_ID + " = " + taskId, null);
    }

    // RQ-0003, 0005, 0006: 투두 수정 및 상태 변경
    public void updateTask(TodoItem item) {
        ContentValues values = new ContentValues();
        values.put(TodoItemDbHelper.COLUMN_TITLE, item.getTitle());
        values.put(TodoItemDbHelper.COLUMN_CATEGORY_ID, item.getCategoryId());
        values.put(TodoItemDbHelper.COLUMN_DUE_TIME, item.getDueTime());
        values.put(TodoItemDbHelper.COLUMN_IS_COMPLETED, item.isCompleted() ? 1 : 0);

        database.update(TodoItemDbHelper.TABLE_NAME, values,
                TodoItemDbHelper.COLUMN_ID + " = " + item.getId(), null);
    }

    // RQ-0008, 0009, 0010: 조회 및 필터링의 공통 로직
    public List<TodoItem> getTodoItems(String whereClause, String orderBy) {
        List<TodoItem> items = new ArrayList<>();
        Cursor cursor = database.query(TodoItemDbHelper.TABLE_NAME, allColumns,
                whereClause, null, null, null, orderBy);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TodoItem item = cursorToTask(cursor);
            items.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return items;
    }

    // RQ-0008: 전체 목록 조회 (마감시간순)
    public List<TodoItem> getAllTasksSortedByTime() {
        // "ASC"는 오름차순 (시간이 빠를수록 먼저)
        return getTodoItems(null, TodoItemDbHelper.COLUMN_DUE_TIME + " ASC");
    }

    // RQ-0009: 카테고리별 필터링 (마감 시간 임박 순)
    public List<TodoItem> getTasksByCategory(String category) {
        String where = TodoItemDbHelper.COLUMN_CATEGORY_ID + " = '" + category + "'";
        // "DESC"는 내림차순 (ID가 클수록 먼저 = 최신순)
        return getTodoItems(where, TodoItemDbHelper.COLUMN_DUE_TIME + " DESC");
    }

    // RQ-0010: 상태별 필터링
    public List<TodoItem> getTasksByCompletionStatus(boolean isCompleted) {
        String where = TodoItemDbHelper.COLUMN_IS_COMPLETED + " = " + (isCompleted ? 1 : 0);
        // 마감시간순으로 정렬
        return getTodoItems(where, TodoItemDbHelper.COLUMN_DUE_TIME + " ASC");
    }

    // RQ-0004: 알림 기능 지원을 위한 조회 (마감 시간이 있는 미완료 항목)
    public List<TodoItem> getUpcomingTasksForAlarm() {
        String where = TodoItemDbHelper.COLUMN_IS_COMPLETED + " = 0 AND " +
                TodoItemDbHelper.COLUMN_DUE_TIME + " IS NOT NULL";
        return getTodoItems(where, TodoItemDbHelper.COLUMN_DUE_TIME + " ASC");
    }
}
