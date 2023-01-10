package com.example.mapper.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class AlarmService
{
    private Context context;
    public AlarmService(Context context){
        this.context=context;
    }

    public PendingIntent getAlarmIntent(Integer slotId){
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, slotId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
    public void addAlarm(PendingIntent intent, Calendar calendar){
        AlarmManager alarmMgr = (AlarmManager)context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        alarmMgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent);
    }
    public void cancelAlarm(PendingIntent intent){
        AlarmManager alarmMgr = (AlarmManager)context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        alarmMgr.cancel(intent);
    }
}