package com.example.mapper.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mapper.apis.Mapper;
import com.example.mapper.apis.Validators;
import com.example.mapper.databinding.ActivityChangePasswordBinding;
import com.example.mapper.databinding.ActivityResetPasswordStep1Binding;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;

    private LinearProgressIndicator loadingIndicator;

    private TextInputEditText currentPasswordTxt;
    private TextInputLayout currentPasswordTxtLayout;

    private TextInputEditText newPasswordTxt;
    private TextInputLayout newPasswordTxtLayout;

    private TextInputEditText newPasswordConfirmTxt;
    private TextInputLayout newPasswordConfirmTxtLayout;

    private Button saveBtn;
    private Button backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingIndicator = binding.loadingIndicator;

        currentPasswordTxt = binding.currentPasswordTxt;
        currentPasswordTxtLayout = binding.currentPasswordTxtLayout;

        newPasswordTxt = binding.newPasswordTxt;
        newPasswordTxtLayout = binding.newPasswordTxtLayout;

        newPasswordConfirmTxt = binding.confirmNewPasswordTxt;
        newPasswordConfirmTxtLayout = binding.confirmNewPasswordTxtLayout;

        saveBtn = binding.saveBtn;
        backBtn = binding.backBtn;

        saveBtn.setOnClickListener(view -> {
            if (validateAndShowErrors()) { // all fields are valid
                setLoadingState(true); // lock all fields

                String currentPassword = currentPasswordTxt.getText().toString().trim();
                String newPassword = newPasswordTxt.getText().toString().trim();
                String newPasswordConfirmation = newPasswordConfirmTxt.getText().toString().trim();

                Mapper.ChangePassword(this, currentPassword, newPassword, newPasswordConfirmation, new Mapper.TaskTransition() {
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
                        Toast.makeText(ChangePasswordActivity.this, "Password is successfully changed.", Toast.LENGTH_SHORT).show();
                        finish();
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
        String currentPassword = currentPasswordTxt.getText().toString().trim();
        String newPassword = newPasswordTxt.getText().toString().trim();
        String confirmPassword = newPasswordConfirmTxt.getText().toString().trim();

        boolean hasErrors = false;

        if(currentPassword.length() < 1){
            currentPasswordTxtLayout.setErrorEnabled(true);
            currentPasswordTxtLayout.setError("Current password field is required.");
            hasErrors |= true;
        }else{
            currentPasswordTxtLayout.setErrorEnabled(false);
            currentPasswordTxtLayout.setError(null);
        }

        if(newPassword.length() < 1){
            newPasswordTxtLayout.setError("New password field is required.");
            newPasswordTxtLayout.setErrorEnabled(true);
            hasErrors |= true;
        }else{
            if(!newPassword.equals(confirmPassword)){
                newPasswordTxtLayout.setError("Password don't match.");
                newPasswordTxtLayout.setErrorEnabled(true);
                hasErrors |= true;
            }else{
                newPasswordTxtLayout.setErrorEnabled(false);
                newPasswordTxtLayout.setError(null);
            }
        }


        return !hasErrors;
    }

    private void processErrors(String message, @Nullable JSONObject errorObj){
        try {
            if(errorObj == null){
                Toast.makeText(ChangePasswordActivity.this, message, Toast.LENGTH_SHORT).show();
            }else{
                JSONObject errors = errorObj.getJSONObject("errors");

                if(errors.has("new_password")){
                    newPasswordTxtLayout.setError(errors.getJSONArray("new_password").get(0).toString());
                }else{
                    newPasswordTxtLayout.setError(null);
                }
            };
        } catch (JSONException e) {
            Toast.makeText(ChangePasswordActivity.this, message, Toast.LENGTH_SHORT).show();
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

        newPasswordTxtLayout.setEnabled(!isLoading);
        newPasswordConfirmTxtLayout.setEnabled(!isLoading);
        backBtn.setEnabled(!isLoading);
    }

}