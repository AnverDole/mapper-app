package com.example.mapper.activities.ui.subjects;

import android.content.Intent;
import android.os.Bundle;

import com.example.mapper.R;
import com.example.mapper.apis.Mapper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;

import androidx.navigation.ui.AppBarConfiguration;

import com.example.mapper.databinding.ActivityEditSubjectBinding;
import com.example.mapper.databinding.ActivityNewSubjectBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class EditSubjectActivity extends AppCompatActivity {

    private ActivityEditSubjectBinding binding;

    public static final String SUBJECT_ID_KEY = "subject_id";
    public static final String SUBJECT_NAME_KEY = "subject_name";
    public static final String SUBJECT_NO = "subject_no";
    public static int SUCCESS_RESULT_CODE = 102;

    private int subjectId;
    private int subjectNo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditSubjectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.saveBtn.setOnClickListener(view -> {
            saveData();
        });

        subjectNo = this.getIntent().getIntExtra(SUBJECT_NO, -1);
        subjectId = this.getIntent().getIntExtra(SUBJECT_ID_KEY, -1);
        binding.subjectNameTxt.setText(this.getIntent().getStringExtra(SUBJECT_NAME_KEY));

        getSupportActionBar().setTitle(getResources().getString(R.string.title_activity_edit_subject) + " #" + subjectNo);

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

            Mapper.updateSubject(this, subjectId, name, new Mapper.TaskTransition() {
                @Override
                public void onBefore() {
                    setLoadingState(true);
                }
            }, new Mapper.ResponseCallBack() {
                @Override
                public void onSuccess(JSONObject response) {
                    setLoadingState(false);

                    Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent();
                    intent.putExtra(SUBJECT_NAME_KEY, name);

                    setResult(SUCCESS_RESULT_CODE, intent);
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