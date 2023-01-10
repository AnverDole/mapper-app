package com.example.mapper.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mapper.apis.Validators;
import com.example.mapper.apis.Mapper;
import com.example.mapper.databinding.ActivityResetPasswordStep1Binding;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class ResetPasswordStep1Activity extends AppCompatActivity {

    private ActivityResetPasswordStep1Binding binding;

    private LinearProgressIndicator loadingIndicator;

    private TextInputEditText emailTxt;
    private TextInputLayout emailTxtLayout;

    private Button nextBtn;
    private Button backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityResetPasswordStep1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingIndicator = binding.loadingIndicator;

        emailTxt = binding.emailTxt;
        emailTxtLayout = binding.emailTxtLayout;

        nextBtn = binding.nextBtn;
        backBtn = binding.backBtn;

        nextBtn.setOnClickListener(view -> {
            if (validateAndShowErrors()) { // all fields are valid
                setLoadingState(true); // lock all fields

                String email = emailTxt.getText().toString().trim();

                Mapper.ForgotPasswordStep1(email, new Mapper.TaskTransition() {
                    @Override
                    public void onBefore() {
                        setLoadingState(true);
                    }

                    @Override
                    public void onAfter() {

                    }
                }, new Mapper.ResponseCallBack() {
                    @Override
                    public void onSuccess(JSONObject response) {


                        Intent intent = new Intent(ResetPasswordStep1Activity.this, ResetPasswordStep2Activity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);

                        setLoadingState(false);
                    }

                    @Override
                    public void onError(String message, JSONObject error) {
                        setLoadingState(false);
                        processErrors(message, error);
                    }
                });
            }
        });

        backBtn.setOnClickListener(view -> {
            finish();
        });

    }

    /**
     * Validate the input fields and show appropriate error messages
     * if there is any.
     * @return
     */
    private boolean validateAndShowErrors(){
        String email = emailTxt.getText().toString().trim();

        boolean hasErrors = false;

        if(email.length() < 1){
            emailTxtLayout.setError("Email address field is required.");
            hasErrors |= true;
        }else{
            if(Validators.isEmailValid(email)){
                emailTxtLayout.setError(null);
            }else{
                emailTxtLayout.setError("Invalid or missing email address.");
                hasErrors |= true;
            }
        }

        return !hasErrors;
    }

    private void processErrors(String message, @Nullable JSONObject errorObj){
        try {
            if(errorObj == null){
                Toast.makeText(ResetPasswordStep1Activity.this, message, Toast.LENGTH_SHORT).show();
            }else{
                JSONObject errors = errorObj.getJSONObject("errors");

                if(errors.has("email")){
                    emailTxtLayout.setError(errors.getJSONArray("email").get(0).toString());
                }else{
                    emailTxtLayout.setError(null);
                }
            };
        } catch (JSONException e) {
            Toast.makeText(ResetPasswordStep1Activity.this, message, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Lock all of the fields and show the loading animation
     * @param isLoading whether to lock or unlock
     */
    private void setLoadingState(boolean isLoading){

        if(isLoading){
            loadingIndicator.setVisibility(View.VISIBLE);
        }else{
            loadingIndicator.setVisibility(View.GONE);
        }

        emailTxt.setEnabled(!isLoading);
        nextBtn.setEnabled(!isLoading);
        backBtn.setEnabled(!isLoading);
    }

}