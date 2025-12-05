package com.example.to_dolist.alarm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.to_dolist.data.TodoItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";
    private static final String TIME_FORMAT = "HH:mm";
    private static final long ONE_HOUR_IN_MILLIS = 60 * 60 * 1000;

    /**
     * TodoItem에 설정된 시간보다 1시간 일찍 알람을 스케줄링합니다.
     */
    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleAlarm(Context context, TodoItem item) {
        if (item.getDueTime() == null) {
            Log.d(TAG, "Due time is null for item: " + item.getTitle());
            return;
        }

        try {
            // 1. Due Time (HH:mm) 파싱
            SimpleDateFormat format = new SimpleDateFormat(TIME_FORMAT, Locale.KOREA);
            Date dueTimeDate = format.parse(item.getDueTime());

            if (dueTimeDate == null) return;

            // 2. 오늘 날짜와 결합하여 마감 시간 Calendar 생성
            Calendar dueCalendar = Calendar.getInstance();
            dueCalendar.setTime(dueTimeDate);

            // 오늘 날짜로 년/월/일을 설정합니다.
            Calendar now = Calendar.getInstance();
            dueCalendar.set(Calendar.YEAR, now.get(Calendar.YEAR));
            dueCalendar.set(Calendar.MONTH, now.get(Calendar.MONTH));
            dueCalendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

            // 3. 1시간 전 알람 트리거 시간 계산
            long triggerTimeOneHourBefore = dueCalendar.getTimeInMillis() - ONE_HOUR_IN_MILLIS;

            // 4. 현재 시간보다 이미 지난 시간이라면, 다음 날로 설정합니다.
            if (triggerTimeOneHourBefore <= System.currentTimeMillis()) {
                // 다음 날로 설정
                triggerTimeOneHourBefore += 24 * ONE_HOUR_IN_MILLIS;
                Log.d(TAG, "Scheduled time passed, rescheduling for tomorrow: " + new Date(triggerTimeOneHourBefore));
            }

            // 5. 알람 매니저 및 PendingIntent 준비
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("TODO_ID", item.getId());
            intent.putExtra("TODO_TITLE", item.getTitle());

            // PendingIntent 생성 시 ID를 사용하여 알람이 덮어쓰여지지 않도록 합니다.
            int requestCode = (int) item.getId();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // 6. 알람 스케줄링 (setExact 사용)
            Log.d(TAG, "Alarm Scheduled for: " + item.getTitle() + " at " + new Date(triggerTimeOneHourBefore));

            // RQ-0004: 알림 발송 로직 (AlarmManager 사용)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // API 23 이상 (Doze 모드 대응 필요)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeOneHourBefore, pendingIntent);
            } else {
                // setExact 사용
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeOneHourBefore, pendingIntent);
            }
            // 참고: Android 12(S) 이상의 권한 검사는 MainActivity에서 이미 처리했습니다.


        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse due time: " + item.getDueTime(), e);
        }
    }

    /**
     * 기존에 설정된 알람을 취소합니다. (항목 완료 또는 삭제 시 사용)
     */
    public static void cancelAlarm(Context context, long itemId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);

        // 스케줄링 시 사용했던 동일한 requestCode(itemId)와 플래그를 사용해야 취소할 수 있습니다.
        int requestCode = (int) itemId;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE // 알람이 없으면 새로 만들지 않음
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Alarm cancelled for ID: " + itemId);
        }
    }
}