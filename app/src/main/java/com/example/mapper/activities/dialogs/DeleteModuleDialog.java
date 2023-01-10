package com.example.mapper.activities.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.mapper.R;
import com.example.mapper.apis.Mapper;
import com.example.mapper.data.Messages;
import com.example.mapper.models.Module;
import com.example.mapper.models.Subject;

import org.json.JSONObject;

/**
 * TODO: document your custom view class.
 */
public class DeleteModuleDialog {

    private EventListeners eventListeners;

    public void show(Activity activity, Module module, int no){


        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.delete_module_dialog);

        ((TextView)dialog.findViewById(R.id.delete_module_title))
                .setText(activity.getResources().getString(R.string.delete_module_title) + " #" + String.valueOf(no));


        LinearLayout promptLayout = dialog.findViewById(R.id.prompt_layout);
        LinearLayout deletingLayout = dialog.findViewById(R.id.delete_module_layout);

        dialog.findViewById(R.id.delete_module_cancel_btn).setOnClickListener(view -> {
            dialog.cancel();
            dialog.hide();
        });

        dialog.findViewById(R.id.delete_module_delete_btn).setOnClickListener(view -> {
            Mapper.deleteModule(activity.getApplicationContext(), module.subjectId, module.id, new Mapper.TaskTransition() {
                @Override
                public void onBefore() {
                    setBusyStatus(true, promptLayout, deletingLayout);
                }
            }, new Mapper.ResponseCallBack() {
                @Override
                public void onSuccess(JSONObject response) {
                    eventListeners.onSuccess();
                    dialog.hide();
                }

                @Override
                public void onError(String message, @Nullable JSONObject error) {
                    setBusyStatus(false, promptLayout, deletingLayout);

                    Toast.makeText(activity, message, Toast.LENGTH_SHORT)
                            .show();
                }

                @Override
                public void NotAuthenticated() {
                    Toast.makeText(activity, Messages.AUTH_FAILURE_ERROR_MESSAGE, Toast.LENGTH_SHORT)
                            .show();

                    LogoutDialog.resetAuth(activity);
                }
            });
        });
        dialog.show();


    }

    public void setEventListeners(EventListeners eventListeners) {
        this.eventListeners = eventListeners;
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

    public interface EventListeners{
        void onSuccess();
    }
}