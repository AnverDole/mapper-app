package com.example.mapper.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mapper.R;
import com.example.mapper.apis.Mapper;
import com.example.mapper.databinding.ActivityMainBinding;
import com.example.mapper.notifications.AlarmService;
import com.example.mapper.synchronization.Authenticator;
import com.example.mapper.synchronization.AuthenticatorService;
import com.example.mapper.synchronization.SyncAccount;
import com.example.mapper.synchronization.SyncService;

import org.json.JSONObject;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private LinearLayout loadingLayout;

    private LinearLayout errorRetryLayout;
    private TextView errorMessageText;
    private Button retryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingLayout = binding.loadingLayout;

        errorRetryLayout = binding.errorRetryLayout;
        errorMessageText = binding.errorMessageText;
        retryBtn = binding.retryBtn;

        Mapper.setBaseUrl(getApplicationContext().getString(R.string.mapperBaseUrl));
        Mapper.initiateRequestQueueInstance(getApplicationContext());

        TryAuthenticate();

        retryBtn.setOnClickListener(view -> {
            TryAuthenticate();
        });
    }

    private void GotoLogin(){
        new Handler().postDelayed((Runnable) () -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            MainActivity.this.finish();
        }, 500);
    }

    private void GotoHome(){
        new Handler().postDelayed((Runnable) () -> {
            Intent intent = new Intent(MainActivity.this, IndexActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            MainActivity.this.finish();
        }, 500);

//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.SECOND, 10);
//        AlarmService alarmService = new AlarmService(getApplicationContext());
//        alarmService.addAlarm(alarmService.getAlarmIntent(1), calendar);

//        Bundle settingsBundle = new Bundle();
//        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
//        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
//
//        /*
//         * Request the sync for the default account, authority, and
//         * manual sync settings
//         */
//        Account account = SyncAccount.Create(getApplicationContext());
//        getContentResolver().requestSync(account, getApplicationContext().getString(R.string.authority), settingsBundle);

    }


    private void TryAuthenticate(){

        if(Mapper.IsAuthenticated(getApplicationContext())){

            Mapper.TryAuthenticate(getApplicationContext(), new Mapper.TaskTransition() {
                @Override
                public void onBefore() {
                    showLoadingLayout(true);
                    showErrorLayout(false, null);
                }
            }, new Mapper.ResponseCallBack() {
                @Override
                public void onSuccess(JSONObject response) {
                    GotoHome();
                }

                @Override
                public void onError(String message, JSONObject error) {
                    showLoadingLayout(false);
                    showErrorLayout(true, message);
                }

                @Override
                public void NotAuthenticated() {
                    Mapper.RemoveAuthenticationToken(getApplicationContext());
                    GotoLogin();
                }
            });

        }else{
            GotoLogin();
        }
    }

    private void showLoadingLayout(boolean show){
        if(show){
            loadingLayout.setVisibility(View.VISIBLE);
        }else{
            loadingLayout.setVisibility(View.GONE);
        }
    }
    private void showErrorLayout(boolean show, String message){
        if(show){
            errorRetryLayout.setVisibility(View.VISIBLE);
        }else{
            errorRetryLayout.setVisibility(View.GONE);
        }
        errorMessageText.setText(message);
    }
}