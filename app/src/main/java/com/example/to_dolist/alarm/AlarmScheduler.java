package com.example.to_dolist.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.to_dolist.data.TodoItem;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale; // 시간 파싱 및 로깅을 위해 임포트

// 시간 파싱 관련 임포트 (SimpleDateFormat 대신 Calendar 객체 활용)

public class AlarmScheduler {
    private static final String TAG = "AlarmScheduler";
    private static final long ONE_HOUR_IN_MILLIS = 60 * 60 * 1000;

    public static void scheduleAlarm(Context context, TodoItem item) {
        // dueTime이 null이거나 비어있으면 알림 등록을 건너뜁니다.
        if (item.getDueTime() == null || item.getDueTime().isEmpty()) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);

        // Task ID를 PendingIntent 고유 ID로 사용
        long alarmId = item.getId();

        // long 타입인 alarmId를 int로 변환하여 PendingIntent에 사용 (int 범위 내여야 함)
        int requestCode = (int) alarmId;

        // 알림에 필요한 데이터 추가
        intent.putExtra("TASK_TITLE", item.getTitle());
        intent.putExtra("TASK_ID", alarmId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode, // int requestCode 사용
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 마감 시간 1시간 전 시간 (밀리초) 계산
        long triggerTimeMillis = getTriggerTimeMillis(item.getDueTime());
        long triggerTimeOneHourBefore = triggerTimeMillis - ONE_HOUR_IN_MILLIS;

        // 현재 시간보다 늦게 트리거되도록 확인
        if (triggerTimeOneHourBefore > System.currentTimeMillis()) {
            Log.d(TAG, "Alarm Scheduled for: " + item.getTitle() + " at " + new Date(triggerTimeOneHourBefore));

            // RQ-0004: 알림 발송 로직 (AlarmManager 사용)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeOneHourBefore, pendingIntent);
        } else {
            Log.w(TAG, "Alarm time already passed for: " + item.getTitle());
            // 이미 시간이 지났다면 알림 등록하지 않음
            cancelAlarm(context, requestCode);
        }
    }

    /**
     * HH:MM 문자열을 오늘 날짜와 결합하여 long 타입의 밀리초로 변환합니다.
     */
    private static long getTriggerTimeMillis(String dueTime) {
        try {
            // dueTime 형식: "HH:MM" (예: "15:30")
            String[] parts = dueTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            // 현재 날짜를 포함하는 Calendar 객체 생성
            Calendar calendar = Calendar.getInstance(Locale.KOREA);

            // 오늘 날짜의 해당 시간으로 설정
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // 만약 계산된 시간이 이미 지났다면, 다음 날로 설정 (선택 사항)
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DATE, 1);
            }

            return calendar.getTimeInMillis();

        } catch (Exception e) {
            // 파싱 오류 발생 시 (dueTime 형식이 "HH:MM"이 아닐 경우)
            Log.e(TAG, "Error parsing due time: " + dueTime, e);
            return 0;
        }
    }

    public static void cancelAlarm(Context context, int taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId, // int requestCode 사용
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Alarm cancelled for Task ID: " + taskId);
        }
    }
}