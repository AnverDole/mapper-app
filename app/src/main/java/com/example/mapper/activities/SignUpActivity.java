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
import com.example.mapper.apis.SecureSharedPreferences;
import com.example.mapper.models.User;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SignUpActivity extends AppCompatActivity {

    private com.example.mapper.databinding.ActivitySignUpBinding binding;
    private LinearProgressIndicator loadingIndicator;

    private TextInputEditText emailTxt;
    private TextInputLayout emailTxtLayout;

    private TextInputEditText firstNameTxt;
    private TextInputLayout firstNameTxtLayout;

    private TextInputEditText lastNameTxt;
    private TextInputLayout lastNameTxtLayout;

    private TextInputEditText passwordTxt;
    private TextInputLayout passwordTxtLayout;

    private TextInputEditText confirmPasswordTxt;
    private TextInputLayout confirmPasswordTxtLayout;

    private Button signUpBtn;
    private Button gotoSignInBtn;
    private Button backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = com.example.mapper.databinding.ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingIndicator = binding.loadingIndicator;

        emailTxt = binding.emailTxt;
        emailTxtLayout = binding.emailTxtLayout;

        firstNameTxt = binding.firstNameTxt;
        firstNameTxtLayout = binding.firstNameTxtLayout;

        lastNameTxt = binding.lastNameTxt;
        lastNameTxtLayout = binding.lastNameTxtLayout;

        passwordTxt = binding.passwordTxt;
        passwordTxtLayout = binding.passwordTxtLayout;

        confirmPasswordTxt = binding.confirmPasswordTxt;
        confirmPasswordTxtLayout = binding.confirmPasswordTxtLayout;

        signUpBtn = binding.signUpBtn;
        backBtn = binding.backBtn;
        gotoSignInBtn = binding.gotoSignInBtn;


        signUpBtn.setOnClickListener(view -> {
            if(validateAndShowErrors()){
                setLoadingState(true);

                User user = new User();
                user.email = emailTxt.getText().toString().trim();
                user.firstname = firstNameTxt.getText().toString().trim();
                user.lastname = lastNameTxt.getText().toString().trim();
                user.password = passwordTxt.getText().toString().trim();

                Mapper.SignUp(user, new Mapper.TaskTransition() {
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

                            SecureSharedPreferences
                                    .getEncryptedSharedPreferences(getApplicationContext())
                                    .edit()
                                    .putString("token", token);


                            Mapper.SetAuthenticationToken(getApplicationContext(), token);


                            User user = new User();
                            user.email = jUser.getString("email");
                            user.firstname = jUser.getString("first_name");
                            user.lastname = jUser.getString("last_name");

                            Mapper.SetAuthenticatedUser(user);

                            GotoHome();


                        } catch (JSONException | GeneralSecurityException | IOException e) {
                            processErrors(null, null);
                        }
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
        gotoSignInBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            SignUpActivity.this.finish();
        });

    }

    private void processErrors(String message, @Nullable JSONObject errorObj){
        try {
            if(errorObj == null){
                Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();
            }else{
                JSONObject errors = errorObj.getJSONObject("errors");

                if(errors.has("email")){
                    emailTxtLayout.setError(errors.getJSONArray("email").get(0).toString());
                }
                else{
                    emailTxtLayout.setError(null);
                }
                if(errors.has("first_name")){
                    firstNameTxtLayout.setError(errors.getJSONArray("first_name").get(0).toString());
                }
                else{
                    firstNameTxtLayout.setError(null);
                }
                if(errors.has("last_name")){
                    lastNameTxtLayout.setError(errors.getJSONArray("last_name").get(0).toString());
                }
                else{
                    lastNameTxtLayout.setError(null);
                }
                if(errors.has("password")){
                    passwordTxtLayout.setError(errors.getJSONArray("password").get(0).toString());
                }else{
                    passwordTxtLayout.setError(null);
                }
            };
        } catch (JSONException e) {
            Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validate the input fields and show appropriate error messages
     * if there is any.
     * @return
     */
    private boolean validateAndShowErrors(){
        String email = emailTxt.getText().toString().trim();
        String firstName = firstNameTxt.getText().toString().trim();
        String lastName = lastNameTxt.getText().toString().trim();
        String password = passwordTxt.getText().toString().trim();
        String confirmPassword = confirmPasswordTxt.getText().toString().trim();

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

        if(firstName.length() < 1){
            firstNameTxtLayout.setError("First name field is required.");
            hasErrors |= true;
        }else{
            firstNameTxtLayout.setError(null);
        }
        if(lastName.length() < 1){
            lastNameTxtLayout.setError("Last name field is required.");
            hasErrors |= true;
        }else{
            lastNameTxtLayout.setError(null);
        }

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
        firstNameTxt.setEnabled(!isLoading);
        lastNameTxt.setEnabled(!isLoading);
        passwordTxt.setEnabled(!isLoading);
        confirmPasswordTxt.setEnabled(!isLoading);
        signUpBtn.setEnabled(!isLoading);
        gotoSignInBtn.setEnabled(!isLoading);
        backBtn.setEnabled(!isLoading);
    }

    private void GotoHome(){
        Intent intent = new Intent(SignUpActivity.this, IndexActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        SignUpActivity.this.finish();
    }
}