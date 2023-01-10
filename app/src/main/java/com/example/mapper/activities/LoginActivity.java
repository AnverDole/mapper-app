package com.example.mapper.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapper.apis.Validators;
import com.example.mapper.apis.Mapper;
import com.example.mapper.apis.SecureSharedPreferences;
import com.example.mapper.models.User;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class LoginActivity extends AppCompatActivity {

    private com.example.mapper.databinding.ActivityLoginBinding binding;
    private LinearProgressIndicator loadingIndicator;

    private TextInputEditText emailTxt;
    private TextInputLayout emailTxtLayout;

    private TextInputEditText passwordTxt;
    private TextInputLayout passwordTxtLayout;

    private MaterialCheckBox rememberMeChkBox;
    private Button forgotPasswordBtn;
    private Button signInBtn;
    private Button gotoSignUpBtn;

    private final String LOGIN_REMEMBER_ME_KEY = "login_remember_me";
    private final String LOGIN_REMEMBER_ME_EMAIL_KEY = "login_remember_me_email";
    private final String LOGIN_REMEMBER_ME_PASSWORD_KEY = "login_remember_me_password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = com.example.mapper.databinding.ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingIndicator = binding.loadingIndicator;

        emailTxt = binding.emailTxt;
        emailTxtLayout = binding.emailTxtLayout;

        passwordTxt = binding.passwordTxt;
        passwordTxtLayout = binding.passwordTxtLayout;

        rememberMeChkBox = binding.rememberMeChkbox;
        forgotPasswordBtn = binding.forgotPasswordBtn;
        signInBtn = binding.signInBtn;
        gotoSignUpBtn = binding.gotoSignUpBtn;

        SharedPreferences  sharedPref = null;

        try {
            sharedPref = SecureSharedPreferences
                    .getEncryptedSharedPreferences(getApplicationContext());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SharedPreferences finalSharedPref = sharedPref;

        // When click, goto login activity
        signInBtn.setOnClickListener(view -> {
            if (validateAndShowErrors()) { // all fields are valid
                setLoadingState(true); // lock all fields

                User user = new User();
                user.email = emailTxt.getText().toString().trim();
                user.password = passwordTxt.getText().toString().trim();

                Mapper.SignIn(user, new Mapper.TaskTransition() {
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


                        try {
                            String token = response.getString("token");
                            JSONObject jUser = response.getJSONObject("user");


                            Mapper.SetAuthenticationToken(getApplicationContext(), token);


                            if(rememberMeChkBox.isChecked()){
                                //remember credentials
                                rememberCredentials(finalSharedPref);
                            }else{
                                //clear remembered credentials
                                clearRememberedCredentials(finalSharedPref);
                            }

                            User user = new User();
                            user.email = jUser.getString("email");
                            user.firstname = jUser.getString("first_name");
                            user.lastname = jUser.getString("last_name");

                            Mapper.SetAuthenticatedUser(user);

                            GotoHome();
                        } catch (JSONException e) {
                            processErrors(null, null);
                        }
                    }

                    @Override
                    public void onError(String message, JSONObject error) {
                        setLoadingState(false);

                        processErrors(message, error);

                        if(rememberMeChkBox.isChecked()){
                            //remember credentials
                            rememberCredentials(finalSharedPref);
                        }else{
                            //clear remembered credentials
                            clearRememberedCredentials(finalSharedPref);
                        }
                    }
                });
            }
        });
        gotoSignUpBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });
        forgotPasswordBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, ResetPasswordStep1Activity.class);
            startActivity(intent);
        });

        //prefill credentials if previously remembered.
        if (finalSharedPref != null) {
            boolean rememberMe = finalSharedPref.getBoolean(LOGIN_REMEMBER_ME_KEY, false);

            rememberMeChkBox.setChecked(rememberMe);

            if (rememberMe) {
                String email = finalSharedPref.getString(LOGIN_REMEMBER_ME_EMAIL_KEY, null);
                String password = finalSharedPref.getString(LOGIN_REMEMBER_ME_PASSWORD_KEY, null);


                emailTxt.setText(email);
                passwordTxt.setText(password);

            }
        }
    }

    private void rememberCredentials(SharedPreferences sharedPreferences){
        if (sharedPreferences != null){
            sharedPreferences
                        .edit()
                        .putBoolean(LOGIN_REMEMBER_ME_KEY, true)
                        .putString(LOGIN_REMEMBER_ME_EMAIL_KEY, emailTxt.getText().toString())
                        .putString(LOGIN_REMEMBER_ME_PASSWORD_KEY, passwordTxt.getText().toString())
                        .apply();
        }
    }
    private void clearRememberedCredentials(SharedPreferences sharedPreferences){
        if (sharedPreferences != null) {
            sharedPreferences.edit()
                    .remove(LOGIN_REMEMBER_ME_KEY)
                    .remove(LOGIN_REMEMBER_ME_EMAIL_KEY)
                    .remove(LOGIN_REMEMBER_ME_PASSWORD_KEY)
                    .apply();
        }
    }

    private void processErrors(String message, @Nullable JSONObject errorObj){
        try {
            if(errorObj == null){
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }else{
                JSONObject errors = errorObj.getJSONObject("errors");

                if(errors.has("email")){
                    emailTxtLayout.setError(errors.getJSONArray("email").get(0).toString());
                }else{
                    emailTxtLayout.setError(null);
                }
            };
        } catch (JSONException e) {
            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validate the input fields and show appropriate error messages
     * if there is any.
     * @return
     */
    private boolean validateAndShowErrors(){
        String email = emailTxt.getText().toString().trim();
        String password = passwordTxt.getText().toString().trim();

        boolean hasErrors = false;

        if(email.length() < 1){ // email field is empty
            emailTxtLayout.setError("Email address field is required.");
            hasErrors |= true;
        }else{
            if(Validators.isEmailValid(email)){
                emailTxtLayout.setError(null);
            }else{ //given email is not a valid email address
                emailTxtLayout.setError("Invalid or missing email address.");
                hasErrors |= true;
            }
        }

        if(password.length() < 1){ // password field is empty
            passwordTxtLayout.setError("Password field is required.");
            hasErrors |= true;
        }else{
            passwordTxtLayout.setError(null);
        }

        return !hasErrors;
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
        passwordTxt.setEnabled(!isLoading);
        rememberMeChkBox.setEnabled(!isLoading);
        forgotPasswordBtn.setEnabled(!isLoading);
        signInBtn.setEnabled(!isLoading);
        gotoSignUpBtn.setEnabled(!isLoading);
    }
    private void GotoHome(){
        Intent intent = new Intent(LoginActivity.this, IndexActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        LoginActivity.this.finish();
    }


}