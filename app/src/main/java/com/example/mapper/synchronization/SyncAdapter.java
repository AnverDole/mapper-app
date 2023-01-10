package com.example.mapper.synchronization;

import android.accounts.Account;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.mapper.R;
import com.example.mapper.apis.Mapper;
import com.example.mapper.database.LocalScheduleSlotData;
import com.example.mapper.database.ScheduleSlot;
import com.example.mapper.notifications.AlarmBroadcastReceiver;
import com.example.mapper.notifications.AlarmService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    // Global variables
    // Define a variable to contain a content resolver instance
    ContentResolver contentResolver;

    LocalScheduleSlotData localScheduleSlotData;

    AlarmService alarmService;

    int failedAttempts = 0;
    final int maxFailedAttempts = 10;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        contentResolver = context.getContentResolver();

        alarmService = new AlarmService(context);

        localScheduleSlotData = new LocalScheduleSlotData(getContext().getApplicationContext());

        Log.e("SSD", "Sync Init A");
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.e("SSD", "Sync Running");
        if(failedAttempts > maxFailedAttempts) return;

        for (ScheduleSlot scheduleSlot: localScheduleSlotData.scheduleSlots()) {
            delete(scheduleSlot);
        }

        AsyncTask.execute(() -> loadSchedule(getContext().getApplicationContext(), 100, 1));
    }


    private void loadSchedule(Context context, int perPage, int page){
        Mapper.setBaseUrl(context.getApplicationContext().getString(R.string.mapperBaseUrl));
        Mapper.initiateRequestQueueInstance(context.getApplicationContext());

        Mapper.fetchTodayActivities(context,1, perPage, page, new Mapper.TaskTransition() {},
                new Mapper.ResponseCallBack() {
                    @Override
                    public void onSuccess(JSONObject response) {

                        AsyncTask.execute(() -> {
                            if(parseSchedule(response)){
                                loadSchedule(context, perPage, page + 1);
                            }
                        });

                    }

                    @Override
                    public void onError(String message, @Nullable JSONObject error) {
                        failedAttempts++;
                    }

                    @Override
                    public void NotAuthenticated() {
                        failedAttempts++;
                    }
                });
    }
    private boolean parseSchedule(JSONObject fetchResponse){
        try{
            JSONArray jsonScheduleSlots =  fetchResponse.getJSONArray("slots");


            JSONObject jsonPagination = fetchResponse.getJSONObject("pagination");
            boolean hasMoreSlots = jsonPagination.getBoolean("has_next");

            for(int i = 0; i < jsonScheduleSlots.length(); i++){

                JSONObject jsonScheduleSlot = jsonScheduleSlots.getJSONObject(i);
                JSONObject jsonModule = jsonScheduleSlot.getJSONObject("module");

                ScheduleSlot scheduleSlot = new ScheduleSlot();
                scheduleSlot.id = jsonScheduleSlot.getInt("id");
                scheduleSlot.moduleId = jsonModule.getInt("id");
                scheduleSlot.date = jsonScheduleSlot.getString("date");
                scheduleSlot.startAt = jsonScheduleSlot.getString("start_at_time");
                scheduleSlot.endAt = jsonScheduleSlot.getString("end_at_time");

//                Log.e("SSD", scheduleSlot.date + " " + scheduleSlot.startAt);

                if(jsonScheduleSlot.getInt("is_finished") != 1){
                    insert(scheduleSlot);
                }
            }

            return hasMoreSlots;
        }catch (JSONException e){
            Log.e("SSD", e.getMessage());
            e.printStackTrace();

            failedAttempts++;
            if(failedAttempts > maxFailedAttempts){
                return false;
            }
            return true;
        }
    }

    private void insert(ScheduleSlot scheduleSlot){
        localScheduleSlotData.insert(scheduleSlot);


        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm a", Locale.ENGLISH);
            sdf.parse(scheduleSlot.date + " " + scheduleSlot.startAt);


            PendingIntent alarmIntent = alarmService.getAlarmIntent(scheduleSlot.id);
            alarmService.addAlarm(alarmIntent, sdf.getCalendar());
            Log.e("SSD", sdf.getCalendar().getTime().toString());
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("SSD", e.getMessage());

        }


    }
    private void delete(ScheduleSlot scheduleSlot){
        localScheduleSlotData.delete(scheduleSlot);

        PendingIntent alarmIntent = alarmService.getAlarmIntent(scheduleSlot.id);
        alarmService.cancelAlarm(alarmIntent);
    }


}