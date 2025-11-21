package com.example.healthtrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            FirebaseDatabase.getInstance().getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        DataSnapshot settings = userSnapshot.child("settings");
                        if (settings.exists()) {
                            // Hydration
                            if (settings.child("hydration_reminder_enabled").getValue(Boolean.class) == Boolean.TRUE) {
                                Integer interval = settings.child("hydration_interval_minutes").getValue(Integer.class);
                                if (interval != null) {
                                    scheduleReminder(context, 1, "Time for water!", "Stay hydrated to meet your goal.", interval, true);
                                }
                            }

                            // Sleep
                            if (settings.child("sleep_reminder_enabled").getValue(Boolean.class) == Boolean.TRUE) {
                                String time = settings.child("sleep_reminder_time").getValue(String.class);
                                if (time != null) {
                                    String[] timeParts = time.split(":");
                                    scheduleReminder(context, 2, "Bedtime approaching!", "Time to wind down for a good night\'s sleep.", Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
                                }
                            }

                            // Steps
                            if (settings.child("steps_reminder_enabled").getValue(Boolean.class) == Boolean.TRUE) {
                                String time = settings.child("steps_reminder_time").getValue(String.class);
                                if (time != null) {
                                    String[] timeParts = time.split(":");
                                    scheduleReminder(context, 3, "Daily Steps Reminder", "Don\'t forget to reach your step goal today!", Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });
        }
    }

    private void scheduleReminder(Context context, int notificationId, String title, String message, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("notification_id", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private void scheduleReminder(Context context, int notificationId, String title, String message, int intervalMinutes, boolean isRepeating) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("notification_id", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            long triggerAtMillis = System.currentTimeMillis() + (intervalMinutes * 60 * 1000);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMinutes * 60 * 1000, pendingIntent);
        }
    }
}
