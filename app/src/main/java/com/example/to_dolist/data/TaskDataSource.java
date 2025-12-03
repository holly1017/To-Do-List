package com.example.to_dolist.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class TaskDataSource {

    private SQLiteDatabase database;
    private TaskDbHelper dbHelper;

    // 모든 열 정의
    private String[] allColumns = {
            TaskDbHelper.COLUMN_ID,
            TaskDbHelper.COLUMN_TITLE,
            TaskDbHelper.COLUMN_DESCRIPTION,
            TaskDbHelper.COLUMN_CATEGORY,
            TaskDbHelper.COLUMN_DUE_TIME,
            TaskDbHelper.COLUMN_IS_COMPLETED
    };

    public TaskDataSource(Context context) {
        dbHelper = new TaskDbHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Cursor를 Task 객체로 변환
    private Task cursorToTask(Cursor cursor) {
        Task task = new Task();
        task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(TaskDbHelper.COLUMN_ID)));
        task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(TaskDbHelper.COLUMN_TITLE)));
        task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(TaskDbHelper.COLUMN_DESCRIPTION)));
        task.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(TaskDbHelper.COLUMN_CATEGORY)));
        task.setDueTime(cursor.getString(cursor.getColumnIndexOrThrow(TaskDbHelper.COLUMN_DUE_TIME)));
        task.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(TaskDbHelper.COLUMN_IS_COMPLETED)) == 1);
        return task;
    }

    // RQ-0001: 투두(To-Do) 추가
    public long createTask(Task task) {
        ContentValues values = new ContentValues();
        values.put(TaskDbHelper.COLUMN_TITLE, task.getTitle());
        values.put(TaskDbHelper.COLUMN_DESCRIPTION, task.getDescription());
        values.put(TaskDbHelper.COLUMN_CATEGORY, task.getCategory());
        values.put(TaskDbHelper.COLUMN_DUE_TIME, task.getDueTime());
        values.put(TaskDbHelper.COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);

        long insertId = database.insert(TaskDbHelper.TABLE_NAME, null, values);
        return insertId; // 이 ID를 사용하여 알림 등록에 활용 가능
    }

    // RQ-0002: 투두 삭제
    public void deleteTask(long taskId) {
        database.delete(TaskDbHelper.TABLE_NAME,
                TaskDbHelper.COLUMN_ID + " = " + taskId, null);
    }

    // RQ-0003, 0005, 0006: 투두 수정 및 상태 변경
    public void updateTask(Task task) {
        ContentValues values = new ContentValues();
        values.put(TaskDbHelper.COLUMN_TITLE, task.getTitle());
        values.put(TaskDbHelper.COLUMN_DESCRIPTION, task.getDescription());
        values.put(TaskDbHelper.COLUMN_CATEGORY, task.getCategory());
        values.put(TaskDbHelper.COLUMN_DUE_TIME, task.getDueTime());
        values.put(TaskDbHelper.COLUMN_IS_COMPLETED, task.isCompleted() ? 1 : 0);

        database.update(TaskDbHelper.TABLE_NAME, values,
                TaskDbHelper.COLUMN_ID + " = " + task.getId(), null);
    }

    // RQ-0008, 0009, 0010: 조회 및 필터링의 공통 로직
    public List<Task> getTasks(String whereClause, String orderBy) {
        List<Task> tasks = new ArrayList<>();
        Cursor cursor = database.query(TaskDbHelper.TABLE_NAME, allColumns,
                whereClause, null, null, null, orderBy);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Task task = cursorToTask(cursor);
            tasks.add(task);
            cursor.moveToNext();
        }
        cursor.close();
        return tasks;
    }

    // RQ-0008: 전체 목록 조회 (마감시간순)
    public List<Task> getAllTasksSortedByTime() {
        // "ASC"는 오름차순 (시간이 빠를수록 먼저)
        return getTasks(null, TaskDbHelper.COLUMN_DUE_TIME + " ASC");
    }

    // RQ-0009: 카테고리별 필터링 (최신순)
    public List<Task> getTasksByCategory(String category) {
        String where = TaskDbHelper.COLUMN_CATEGORY + " = '" + category + "'";
        // "DESC"는 내림차순 (ID가 클수록 먼저 = 최신순)
        return getTasks(where, TaskDbHelper.COLUMN_ID + " DESC");
    }

    // RQ-0010: 상태별 필터링
    public List<Task> getTasksByCompletionStatus(boolean isCompleted) {
        String where = TaskDbHelper.COLUMN_IS_COMPLETED + " = " + (isCompleted ? 1 : 0);
        // 마감시간순으로 정렬
        return getTasks(where, TaskDbHelper.COLUMN_DUE_TIME + " ASC");
    }

    // RQ-0004: 알림 기능 지원을 위한 조회 (마감 시간이 있는 미완료 항목)
    public List<Task> getUpcomingTasksForAlarm() {
        String where = TaskDbHelper.COLUMN_IS_COMPLETED + " = 0 AND " +
                TaskDbHelper.COLUMN_DUE_TIME + " IS NOT NULL";
        return getTasks(where, TaskDbHelper.COLUMN_DUE_TIME + " ASC");
    }
}
