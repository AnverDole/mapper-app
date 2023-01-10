package com.example.mapper.activities.ui.subjects;

import android.os.Bundle;

import com.example.mapper.apis.Mapper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;

import androidx.navigation.ui.AppBarConfiguration;

import com.example.mapper.databinding.ActivityNewSubjectBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class NewSubjectActivity extends AppCompatActivity {

    private ActivityNewSubjectBinding binding;

    public static int SUCCESS_RESULT_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewSubjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.saveBtn.setOnClickListener(view -> {
            saveData();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private boolean validateFields(){
        boolean hasErrors = false;
        String subjectName = binding.subjectNameTxt.getText().toString();

        binding.subjectNameTxtLyt.setErrorEnabled(false);
        binding.subjectNameTxtLyt.setError(null);
        if(subjectName.length() < 1){
            binding.subjectNameTxtLyt.setErrorEnabled(true);
            binding.subjectNameTxtLyt.setError("The subject name method is required.");
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


        binding.subjectNameTxt.setEnabled(!isLoading);
        binding.saveBtn.setEnabled(!isLoading);
    }

    private void processErrors(String message, @Nullable JSONObject errorObj){
        try {
            if(errorObj == null){
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }else{
                JSONObject errors = errorObj.getJSONObject("errors");

                if(errors.has("name")){
                    binding.subjectNameTxtLyt.setError(errors.getJSONArray("name").get(0).toString());
                }else{
                    binding.subjectNameTxtLyt.setError(null);
                }
            };
        } catch (JSONException e) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
    private void resetErrors(){
        binding.subjectNameTxtLyt.setError(null);
    }
    private void saveData(){
        if (validateFields()) { // all fields are valid
            setLoadingState(true); // lock all fields
            resetErrors();

            String name = binding.subjectNameTxt.getText().toString();

            Mapper.createSubject(this, name, new Mapper.TaskTransition() {
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