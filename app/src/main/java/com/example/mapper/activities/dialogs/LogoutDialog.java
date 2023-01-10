package com.example.mapper.activities.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.mapper.R;
import com.example.mapper.activities.LoginActivity;
import com.example.mapper.activities.MainActivity;
import com.example.mapper.apis.Mapper;
import com.example.mapper.data.Messages;

import org.json.JSONObject;

/**
 * TODO: document your custom view class.
 */
public class LogoutDialog  {


    public void show(Activity activity){

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.logout_dialog);

        LinearLayout promptLayout = dialog.findViewById(R.id.prompt_layout);
        LinearLayout loggingOutLayout = dialog.findViewById(R.id.logging_out_layout);

        dialog.findViewById(R.id.logout_dialog_cancel_btn).setOnClickListener(view -> {
            dialog.cancel();
        });

        dialog.findViewById(R.id.logout_dialog_logout_btn).setOnClickListener(view -> {
            Mapper.SignOut(activity.getApplicationContext(), new Mapper.TaskTransition() {
                @Override
                public void onBefore() {
                    setBusyStatus(true, promptLayout, loggingOutLayout);
                }
            }, new Mapper.ResponseCallBack() {
                @Override
                public void onSuccess(JSONObject response) {
                    resetAuth(activity);
                }

                @Override
                public void onError(String message, @Nullable JSONObject error) {
                    setBusyStatus(false, promptLayout, loggingOutLayout);

                    Toast.makeText(activity, message, Toast.LENGTH_SHORT)
                            .show();
                }

                @Override
                public void NotAuthenticated() {
                    Toast.makeText(activity, Messages.AUTH_FAILURE_ERROR_MESSAGE, Toast.LENGTH_SHORT)
                            .show();

                    resetAuth(activity);
                }
            });
        });
        dialog.show();


    }

    private void setBusyStatus(boolean isBusy, LinearLayout promptLayout, LinearLayout loggingOutLayout){
        if(isBusy){
            promptLayout.setVisibility(View.GONE);
            loggingOutLayout.setVisibility(View.VISIBLE);
        }else{
            promptLayout.setVisibility(View.VISIBLE);
            loggingOutLayout.setVisibility(View.GONE);
        }
    }

    public static void resetAuth(Activity activity){
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}