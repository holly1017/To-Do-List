package com.example.to_dolist.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.media.Ringtone;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.app.PendingIntent;
import android.app.NotificationManager;

public class AlarmReceiver extends BroadcastReceiver {

    public static Ringtone ringtone;  // 🔥 알람음 멈추기 위해 static으로 저장

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        // -----------------------------
        // 🔴 1) STOP 버튼 눌렸을 때
        // -----------------------------
        if ("STOP_ALARM".equals(action)) {
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }

            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancelAll(); // 알림 제거
            return;
        }

        // -----------------------------
        // 🔔 2) 알람 울릴 때
        // -----------------------------
        String title = intent.getStringExtra("TODO_TITLE");
        long id = intent.getLongExtra("TODO_ID", 0L);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "todo_alarm_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "To-Do 알람",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        // -----------------------------
        // 🔘 STOP 버튼 PendingIntent 생성
        // -----------------------------
        Intent stopIntent = new Intent(context, AlarmReceiver.class);
        stopIntent.setAction("STOP_ALARM");
        PendingIntent stopPending = PendingIntent.getBroadcast(
                context,
                (int) id,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // -----------------------------
        // 🔔 Notification 만들기
        // -----------------------------
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setContentTitle("알람")
                        .setContentText(title + " 시간입니다!")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "알람 끄기", stopPending)
                        .setAutoCancel(true);

        manager.notify((int) id, builder.build());

        // 🔊 실제 소리 재생
        try {
            ringtone = RingtoneManager.getRingtone(context, alarmSound);
            ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
