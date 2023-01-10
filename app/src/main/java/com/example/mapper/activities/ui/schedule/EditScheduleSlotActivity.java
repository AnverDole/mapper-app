package com.example.mapper.activities.ui.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapper.R;
import com.example.mapper.apis.Mapper;
import com.example.mapper.databinding.ActivityEditScheduleSlotBinding;
import com.example.mapper.databinding.ActivityEditSubjectBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Date;

public class EditScheduleSlotActivity extends AppCompatActivity {

    private ActivityEditScheduleSlotBinding binding;

    public static int SUCCESS_RESULT_CODE = 402;

    public static String SCHEDULE_MODULE_NAME = "module_name";
    public static String SCHEDULE_SLOT_ID = "slot_id";
    public static String SCHEDULE_SLOT_DATE = "slot_date";
    public static String SCHEDULE_SLOT_START_TIME = "slot_start_at";
    public static String SCHEDULE_SLOT_END_TIME = "slot_end_at";

    private int slotId;
    private String moduleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditScheduleSlotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.saveBtn.setOnClickListener(view -> {
            saveData();
        });

        moduleName =  getIntent().getStringExtra(EditScheduleSlotActivity.SCHEDULE_MODULE_NAME);
        slotId = getIntent().getIntExtra(EditScheduleSlotActivity.SCHEDULE_SLOT_ID, -1);

        String date =  getIntent().getStringExtra(EditScheduleSlotActivity.SCHEDULE_SLOT_DATE);
        String startAt = getIntent().getStringExtra(EditScheduleSlotActivity.SCHEDULE_SLOT_START_TIME);
        String endAt =  getIntent().getStringExtra(EditScheduleSlotActivity.SCHEDULE_SLOT_END_TIME);

        getSupportActionBar().setTitle(getResources().getString(R.string.edit_schedule_slot_activity_title_prefix) + " #" + moduleName);

        binding.dateTxt.setText(date);
        binding.startsAtTxt.setText(startAt);
        binding.endsAtTxt.setText(endAt);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private boolean validateFields(){
        boolean hasErrors = false;
        String date = binding.dateTxt.getText().toString();
        String startAt = binding.startsAtTxt.getText().toString();
        String endAt = binding.endsAtTxt.getText().toString();


        binding.dateLayout.setErrorEnabled(false);
        binding.dateLayout.setError(null);
        if(date.length() < 1){
            binding.dateLayout.setErrorEnabled(true);
            binding.dateLayout.setError("The date field is required.");
            hasErrors |= true;
        }

        binding.startsAtLayout.setErrorEnabled(false);
        binding.startsAtLayout.setError(null);
        if(startAt.length() < 1){
            binding.startsAtLayout.setErrorEnabled(true);
            binding.startsAtLayout.setError("The date field is required.");
            hasErrors |= true;
        }

        binding.endsAtLayout.setErrorEnabled(false);
        binding.endsAtLayout.setError(null);
        if(endAt.length() < 1){
            binding.endsAtLayout.setErrorEnabled(true);
            binding.endsAtLayout.setError("The end at field is required.");
            hasErrors |= true;
        }


        return !hasErrors;
    }
    /**
     * Lock all of the fields and show the loading animation
     * @param isLoading whether to lock or unlock
     */
    private void setLoadingState(boolean isLoading){

        if(isLoading){
            binding.loadingIndicator.setVisibility(View.VISIBLE);
        }else{
            binding.loadingIndicator.setVisibility(View.INVISIBLE);
        }


        binding.dateTxt.setEnabled(!isLoading);
        binding.startsAtTxt.setEnabled(!isLoading);
        binding.endsAtTxt.setEnabled(!isLoading);
        binding.saveBtn.setEnabled(!isLoading);
    }

    private void processErrors(String message, @Nullable JSONObject errorObj){
        try {
            if(errorObj == null){
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }else{
                JSONObject errors = errorObj.getJSONObject("errors");

                if(errors.has("date")){
                    binding.dateTxt.setError(errors.getJSONArray("date").get(0).toString());
                }else{
                    binding.dateTxt.setError(null);
                }

                if(errors.has("start_at")){
                    binding.startsAtTxt.setError(errors.getJSONArray("start_at").get(0).toString());
                }else{
                    binding.startsAtTxt.setError(null);
                }

                if(errors.has("end_at")){
                    binding.endsAtTxt.setError(errors.getJSONArray("end_at").get(0).toString());
                }else{
                    binding.endsAtTxt.setError(null);
                }
            };
        } catch (JSONException e) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
    private void resetErrors(){
        binding.dateTxt.setError(null);
        binding.startsAtTxt.setError(null);
        binding.endsAtTxt.setError(null);
    }
    private void saveData(){
        if (validateFields()) { // all fields are valid
            setLoadingState(true); // lock all fields
            resetErrors();

            String date = binding.dateTxt.getText().toString();
            String startAt = binding.startsAtTxt.getText().toString();
            String endAt = binding.endsAtTxt.getText().toString();

            Mapper.reScheduleSlot(this, slotId, date, startAt, endAt, new Mapper.TaskTransition() {
                @Override
                public void onBefore() {
                    setLoadingState(true);
                }
            }, new Mapper.ResponseCallBack() {
                @Override
                public void onSuccess(JSONObject response) {
                    setLoadingState(false);

                    Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();

                    setResult(SUCCESS_RESULT_CODE);
                    finish();
                }

                @Override
                public void onError(String message, JSONObject error) {
                    setLoadingState(false);
                    processErrors(message, error);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}