package com.example.mapper.activities.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.mapper.R;
import com.example.mapper.activities.LoginActivity;
import com.example.mapper.apis.Mapper;
import com.example.mapper.data.Messages;

import org.json.JSONObject;

/**
 * TODO: document your custom view class.
 */
public class ProcessingDialog {
    Dialog dialog;

    public ProcessingDialog(Context context){
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.processing_dialog);

    }
    public void setCancelable(boolean isCancelable){
        dialog.setCancelable(isCancelable);
    }
    public void setTitle(String title){
        dialog.setTitle(title);
        ((TextView)dialog.findViewById(R.id.processing_model_title)).setText(title);
    }

    public void show(){
        dialog.show();
    }
    public void hide(){
        dialog.hide();
    }

}