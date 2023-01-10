package com.example.mapper.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapper.R;
import com.example.mapper.apis.Mapper;
import com.example.mapper.data.Messages;
import com.example.mapper.databinding.ActivityResetPasswordStep2Binding;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

public class ResetPasswordStep2Activity extends AppCompatActivity {

    private ActivityResetPasswordStep2Binding binding;
    private LinearProgressIndicator loadingIndicator;
    private TextView message;
    private TextView error_message;
    private TextInputEditText digit1Txt;
    private TextInputEditText digit2Txt;
    private TextInputEditText digit3Txt;
    private TextInputEditText digit4Txt;
    private TextInputEditText digit5Txt;
    private TextView timeLeftMessage;
    private Button nextBtn;
    private Button backBtn;
    private Button resendOTPButton;

    private String email = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityResetPasswordStep2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingIndicator = binding.loadingIndicator;
        message = binding.message;
        error_message = binding.errorMessage;

        digit1Txt = binding.otpDigit1;
        digit2Txt = binding.otpDigit2;
        digit3Txt = binding.otpDigit3;
        digit4Txt = binding.otpDigit4;
        digit5Txt = binding.otpDigit5;

        timeLeftMessage = binding.timeLeftMessage;
        nextBtn = binding.nextBtn;
        backBtn = binding.backBtn;
        resendOTPButton = binding.resendBtn;

        email = ResetPasswordStep2Activity.this.getIntent().getStringExtra("email");
        message.setText(getResources().getString(R.string.activity_reset_password_step2_message) + " ("+ email +").");


        digit1Txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length() > 0) {
                    digit2Txt.requestFocus();
                    digit2Txt.setSelection(digit2Txt.getText().length());
                }
                refreshNextBtnEnableStatus();
            }
        });

        digit2Txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length() < 1) {
                    digit1Txt.requestFocus();
                    digit1Txt.setSelection(digit1Txt.getText().length());
                }else{
                    digit3Txt.requestFocus();
                    digit3Txt.setSelection(digit3Txt.getText().length());
                }
                refreshNextBtnEnableStatus();
            }
        });
        digit2Txt.setOnKeyListener((view, i, keyEvent) -> {
            if(i == KeyEvent.KEYCODE_DEL) {
                digit1Txt.requestFocus();
                digit1Txt.setSelection(digit1Txt.getText().length());
            }
            return false;
        });

        digit3Txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length() < 1) {
                    digit2Txt.requestFocus();
                    digit2Txt.setSelection(digit2Txt.getText().length());
                }else{
                    digit4Txt.requestFocus();
                    digit4Txt.setSelection(digit4Txt.getText().length());
                }
                refreshNextBtnEnableStatus();
            }
        });
        digit3Txt.setOnKeyListener((view, i, keyEvent) -> {
            if(i == KeyEvent.KEYCODE_DEL) {
                digit2Txt.requestFocus();
                digit2Txt.setSelection(digit2Txt.getText().length());
            }
            return false;
        });

        digit4Txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length() < 1) {
                    digit3Txt.requestFocus();
                    digit3Txt.setSelection(digit3Txt.getText().length());
                }else{
                    digit5Txt.requestFocus();
                    digit5Txt.setSelection(digit5Txt.getText().length());
                }
                refreshNextBtnEnableStatus();
            }
        });
        digit4Txt.setOnKeyListener((view, i, keyEvent) -> {
            if(i == KeyEvent.KEYCODE_DEL) {
                digit3Txt.requestFocus();
                digit3Txt.setSelection(digit3Txt.getText().length());
            }
            return false;
        });

        digit5Txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length() < 1){
                    digit4Txt.requestFocus();
                    digit4Txt.setSelection(digit4Txt.getText().length());
                }
                refreshNextBtnEnableStatus();
            }
        });
        digit5Txt.setOnKeyListener((view, i, keyEvent) -> {
            if(i == KeyEvent.KEYCODE_DEL) {
                digit4Txt.requestFocus();
                digit4Txt.setSelection(digit4Txt.getText().length());
            }
            return false;
        });

        nextBtn.setOnClickListener(view -> {
            setLoadingState(true);

            String otp = digit1Txt.getText().toString()
                    + digit2Txt.getText().toString()
                    + digit3Txt.getText().toString()
                    + digit4Txt.getText().toString()
                    + digit5Txt.getText().toString();

            Mapper.ForgotPasswordStep2(email, otp, new Mapper.TaskTransition() {
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


                    Intent intent = new Intent(ResetPasswordStep2Activity.this, ResetPasswordStep3Activity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("otp", otp);
                    startActivity(intent);

                    setLoadingState(false);
                }

                @Override
                public void onError(String message, JSONObject error) {
                    setLoadingState(false);
                    processErrors(message, error);
                }
            });

//            setLoadingState(false);
//            Intent intent = new Intent(this, ResetPasswordStep3Activity.class);
//            startActivity(intent);
        });

        backBtn.setOnClickListener(view -> {
            finish();
        });

        resendOTPButton.setOnClickListener(view -> {
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
                    lockResendOTP(20000);
                    setLoadingState(false);
                }

                @Override
                public void onError(String message, JSONObject error) {
                    setLoadingState(false);
                    processErrors(message, error);
                }
            });
        });

        lockResendOTP(20000);
    }

    private void refreshNextBtnEnableStatus(){
        boolean canExecuteAction = digit1Txt.getText().toString().length() > 0 &&
                digit2Txt.getText().toString().length() > 0 &&
                digit3Txt.getText().toString().length() > 0 &&
                digit4Txt.getText().toString().length() > 0 &&
                digit5Txt.getText().toString().length() > 0;

        nextBtn.setEnabled(canExecuteAction);
    }

    private void lockResendOTP(int duration){
        resendOTPButton.setEnabled(false);
        timeLeftMessage.setVisibility(View.VISIBLE);
        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int)(millisUntilFinished / 1000);
                timeLeftMessage.setText("Wait " + secondsLeft + " Seconds to resend a new one.");
            }

            public void onFinish() {
                resendOTPButton.setEnabled(true);
                timeLeftMessage.setVisibility(View.GONE);
            }

        }.start();
    }

    private void processErrors(String message, @Nullable JSONObject errorObj){
        try {
            if(errorObj == null){
                Toast.makeText(ResetPasswordStep2Activity.this, message, Toast.LENGTH_SHORT).show();
            }else{
                JSONObject errors = errorObj.getJSONObject("errors");

                if(errors.has("otp")){
                    error_message.setText(errors.getString("otp"));
                    error_message.setVisibility(View.VISIBLE);
                }else{
                    error_message.setText(null);
                    error_message.setVisibility(View.GONE);
                    Toast.makeText(ResetPasswordStep2Activity.this, Messages.UNKNOWN_ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
                    finish();
                }

            };
        } catch (JSONException e) {
            Toast.makeText(ResetPasswordStep2Activity.this, Messages.UNKNOWN_ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
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

        digit1Txt.setEnabled(!isLoading);
        digit2Txt.setEnabled(!isLoading);
        digit3Txt.setEnabled(!isLoading);
        digit4Txt.setEnabled(!isLoading);
        digit5Txt.setEnabled(!isLoading);
        nextBtn.setEnabled(!isLoading);
        backBtn.setEnabled(!isLoading);
    }

}