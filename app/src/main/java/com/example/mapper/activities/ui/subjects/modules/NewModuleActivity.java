package com.example.mapper.activities.ui.subjects.modules;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapper.R;
import com.example.mapper.apis.Mapper;
import com.example.mapper.databinding.ActivityNewModuleBinding;
import com.example.mapper.databinding.ActivityNewSubjectBinding;
import com.example.mapper.models.Module;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class NewModuleActivity extends AppCompatActivity {

    private ActivityNewModuleBinding binding;

    public static int SUCCESS_RESULT_CODE = 101;
    private int subjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewModuleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        subjectId = getIntent().getIntExtra("subject_id", -1);

        binding.saveBtn.setOnClickListener(view -> {
            saveData();
        });

        Spinner spinner = binding.prioritySpinner;

        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        Module.PrioritisationMethods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private boolean validateFields(){
        boolean hasErrors = false;
        String moduleTitle = binding.moduleTitleTxt.getText().toString();
        String durationText = binding.moduleDurationTxt.getText().toString();
        String priority = binding.prioritySpinner.getSelectedItem().toString();

        binding.moduleTitleTxtLyt.setErrorEnabled(false);
        binding.moduleTitleTxtLyt.setError(null);
        if(moduleTitle.length() < 1){
            binding.moduleTitleTxtLyt.setErrorEnabled(true);
            binding.moduleTitleTxtLyt.setError("The title field is required.");
            hasErrors |= true;
        }

        binding.moduleDurationTxtLyt.setErrorEnabled(false);
        binding.moduleDurationTxtLyt.setError(null);
        if(durationText.length() < 1){
            binding.moduleDurationTxtLyt.setErrorEnabled(true);
            binding.moduleDurationTxtLyt.setError("The duration field is required.");
            hasErrors |= true;
        }

        binding.prioritySpinnerLyt.setErrorEnabled(false);
        binding.prioritySpinnerLyt.setError(null);
        if(priority.length() < 1){
            binding.prioritySpinnerLyt.setErrorEnabled(true);
            binding.prioritySpinnerLyt.setError("The priority field is required.");
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


        binding.moduleTitleTxt.setEnabled(!isLoading);
        binding.saveBtn.setEnabled(!isLoading);
    }

    private void processErrors(String message, @Nullable JSONObject errorObj){
        try {
            if(errorObj == null){
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }else{
                JSONObject errors = errorObj.getJSONObject("errors");

                if(errors.has("title")){
                    binding.moduleTitleTxtLyt.setError(errors.getJSONArray("title").get(0).toString());
                }else{
                    binding.moduleTitleTxtLyt.setError(null);
                }

                if(errors.has("duration")){
                    binding.moduleDurationTxt.setError(errors.getJSONArray("duration").get(0).toString());
                }else{
                    binding.moduleDurationTxt.setError(null);
                }

                if(errors.has("priority")){
                    binding.prioritySpinnerLyt.setError(errors.getJSONArray("priority").get(0).toString());
                }else{
                    binding.moduleTitleTxtLyt.setError(null);
                }
            };
        } catch (JSONException e) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
    private void resetErrors(){
        binding.moduleTitleTxtLyt.setError(null);
    }
    private void saveData(){
        if (validateFields()) { // all fields are valid
            setLoadingState(true); // lock all fields
            resetErrors();

            String title = binding.moduleTitleTxt.getText().toString();
            String duration = binding.moduleDurationTxt.getText().toString();
            Integer priority = binding.prioritySpinner.getSelectedItemPosition() + 1;
            HashMap<String, String> data = new HashMap<>();
            data.put("title", title);
            data.put("duration", duration);
            data.put("priority", String.valueOf(priority));

            Mapper.createModule(this, subjectId, data, new Mapper.TaskTransition() {
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