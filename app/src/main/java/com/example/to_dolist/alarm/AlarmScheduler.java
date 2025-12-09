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

    /**
     * ⭐ 정확한 시간에 알람 울리도록 예약하는 코드 (핵심)
     */
    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleAlarm(Context context, TodoItem item) {

        if (item.getDueTime() == null) {
            Log.e(TAG, "알람 시간을 찾을 수 없습니다: " + item.getTitle());
            return;
        }

        try {
            // 입력된 시간 파싱
            SimpleDateFormat format = new SimpleDateFormat(TIME_FORMAT, Locale.KOREA);
            Date date = format.parse(item.getDueTime());
            if (date == null) return;

            // 오늘 날짜 + 입력한 시각으로 Calendar 생성
            Calendar target = Calendar.getInstance();
            target.setTime(date);

            Calendar now = Calendar.getInstance();
            target.set(Calendar.YEAR, now.get(Calendar.YEAR));
            target.set(Calendar.MONTH, now.get(Calendar.MONTH));
            target.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

            // 만약 이미 지난 시간이면 내일로 예약
            if (target.getTimeInMillis() <= System.currentTimeMillis()) {
                target.add(Calendar.DAY_OF_MONTH, 1);
            }

            long triggerTime = target.getTimeInMillis();

            Log.d(TAG, "알람 예약됨: " + item.getTitle() + " → " + target.getTime());

            // 브로드캐스트 Intent 생성
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("TODO_ID", item.getId());
            intent.putExtra("TODO_TITLE", item.getTitle());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) item.getId(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            // 정확한 시간에 울리도록 설정
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }

        } catch (ParseException e) {
            Log.e(TAG, "시간 파싱 실패: " + item.getDueTime(), e);
        }
    }

    /**
     * 알람 취소
     */
    public static void cancelAlarm(Context context, long itemId) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) itemId,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "알람 취소됨 ID = " + itemId);
        }
    }
}
