package com.example.mapper.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapper.apis.Mapper;
import com.example.mapper.data.Messages;
import com.example.mapper.databinding.ActivityResetPasswordStep3Binding;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class ResetPasswordStep3Activity extends AppCompatActivity {

    private ActivityResetPasswordStep3Binding binding;

    private LinearProgressIndicator loadingIndicator;
    private TextInputEditText passwordTxt;
    private TextInputLayout passwordTxtLayout;
    private TextInputEditText confirmPasswordTxt;
    private TextInputLayout confirmPasswordTxtLayout;
    private Button saveBtn;
    private Button backBtn;

    private String email;
    private String otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityResetPasswordStep3Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingIndicator = binding.loadingIndicator;
        passwordTxt = binding.passwordTxt;
        passwordTxtLayout = binding.passwordTxtLayout;
        confirmPasswordTxt = binding.confirmPasswordTxt;
        confirmPasswordTxtLayout = binding.confirmPasswordTxtLayout;
        backBtn = binding.backBtn;
        saveBtn = binding.saveBtn;

        Intent intent = ResetPasswordStep3Activity.this.getIntent();
        email = intent.getStringExtra("email");
        otp = intent.getStringExtra("otp");

        saveBtn.setOnClickListener(view -> {
            if(validateAndShowErrors()){
                setLoadingState(true);

                String password = passwordTxt.getText().toString().trim();

                Mapper.ForgotPasswordStep3(email, otp, password, new Mapper.TaskTransition() {
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
                        setLoadingState(false);

                        Toast.makeText(ResetPasswordStep3Activity.this, Messages.ACCOUNT_PASSWORD_RESTED_MESSAGE, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ResetPasswordStep3Activity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        ResetPasswordStep3Activity.this.finish();
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
        String password = passwordTxt.getText().toString().trim();
        String confirmPassword = confirmPasswordTxt.getText().toString().trim();

        boolean hasErrors = false;

        if(password.length() < 1){ //password field is empty
            passwordTxtLayout.setError("Password field is required.");
            confirmPasswordTxtLayout.setError(null);
            hasErrors |= true;
        }else{
            passwordTxtLayout.setError(null);

            if(confirmPassword.length() < 1){ //confirm password field is empty
                confirmPasswordTxtLayout.setError("Confirm password field is required.");
                hasErrors |= true;
            }else{
                if(!confirmPassword.equals(password)){ //confirm password is not matching with password
                    confirmPasswordTxtLayout.setError("Passwords not matched.");
                    hasErrors |= true;
                }else{
                    confirmPasswordTxtLayout.setError(null);
                }
            }

        }

        return !hasErrors;
    }

    private void processErrors(String message, @Nullable JSONObject errorObj){
        try {
            if(errorObj == null){
                Toast.makeText(ResetPasswordStep3Activity.this, message, Toast.LENGTH_SHORT).show();
            }else{
                JSONObject errors = errorObj.getJSONObject("errors");

                if(errors.has("password")){
                    passwordTxtLayout.setError(errors.getJSONArray("password").get(0).toString());
                }else{
                    passwordTxtLayout.setError(null);
                }
            };
        } catch (JSONException e) {
            Toast.makeText(ResetPasswordStep3Activity.this, message, Toast.LENGTH_SHORT).show();
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

        passwordTxt.setEnabled(!isLoading);
        confirmPasswordTxt.setEnabled(!isLoading);
        saveBtn.setEnabled(!isLoading);
        backBtn.setEnabled(!isLoading);
    }

}