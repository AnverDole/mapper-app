package com.example.mapper.activities.ui.settings;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mapper.R;
import com.example.mapper.activities.IndexActivity;
import com.example.mapper.activities.dialogs.LogoutDialog;
import com.example.mapper.activities.viewmodels.SettingsViewModel;
import com.example.mapper.apis.Mapper;
import com.example.mapper.databinding.FragmentSettingsBinding;
import com.example.mapper.models.Settings;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;

    LinearProgressIndicator loadingIndicator;
    LinearLayout errorRetryLayout;
    ScrollView pageLayout;

    TextInputLayout prioritisationTxtLayout;
    AutoCompleteTextView prioritisationTxt;

    TextView errorMessageText;
    Button retryBtn;

    TextInputLayout durationBetweenActivitiesTxtLayout;
    TextInputEditText durationBetweenActivitiesTxt;

    TextInputLayout activityMaxDurationTxtLayout;
    TextInputEditText activityMaxDurationTxt;

    TextInputLayout dayStartsAtTxtLayout;
    TextInputEditText dayStartsAtTxt;

    TextInputLayout dayEndsAtTxtLayout;
    TextInputEditText dayEndsAtTxt;

    Button saveButton;

    ArrayAdapter prioritisationItemsAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        loadingIndicator = binding.loadingIndicator;

        errorRetryLayout = binding.errorRetryLayout;
        errorMessageText = binding.errorMessageText;
        pageLayout = binding.pageLayout;
        retryBtn = binding.retryBtn;

        prioritisationTxtLayout = binding.prioritisationTxtLayout;
        prioritisationTxt = binding.prioritisationTxt;

        durationBetweenActivitiesTxtLayout = binding.durationBetweenActivitiesTxtLayout;
        durationBetweenActivitiesTxt = binding.durationBetweenActivitiesTxt;

        activityMaxDurationTxtLayout = binding.maxDurationTxtLayout;
        activityMaxDurationTxt = binding.maxDurationTxt;

        dayStartsAtTxtLayout = binding.dayStartsAtTxtLayout;
        dayStartsAtTxt = binding.dayStartsAtTxt;

        dayEndsAtTxtLayout = binding.dayEndsAtTxtLayout;
        dayEndsAtTxt = binding.dayEndsAtTxt;

        saveButton = binding.saveBtn;

        prioritisationItemsAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, Settings.PrioritisationMethods);
        prioritisationTxt.setAdapter(prioritisationItemsAdapter);



        prioritisationTxt.setOnClickListener(view->{
            prioritisationTxt.showDropDown();
        });
        prioritisationTxtLayout.setOnClickListener(view->{
            prioritisationTxt.showDropDown();
        });
        prioritisationTxt.setOnTouchListener((view, e)-> {
            prioritisationItemsAdapter.getFilter().filter("");
            return false;
        });


        saveButton.setOnClickListener(view->{
            saveData();
        });
        retryBtn.setOnClickListener(view -> {
            loadData();
        });


        IndexActivity.registerOptionMenu(R.menu.activity_index_menu);
        IndexActivity.registerOptionMenuItemClickEventObserver(R.id.nav_settings, R.id.menu_refresh, () -> {
            loadData();
        });
        loadData();

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private boolean validateFields(){
        boolean hasErrors = false;
        String selectedMethod = prioritisationTxt.getText().toString();

        prioritisationTxtLayout.setErrorEnabled(false);
        prioritisationTxtLayout.setError(null);
        if(selectedMethod.length() > 0){
            boolean schedulingMethodsFieldIsValid = false;
            if(Settings.PrioritisationMethods.indexOf(selectedMethod) > -1){
                schedulingMethodsFieldIsValid |= true;
            }

            if(!schedulingMethodsFieldIsValid){
                prioritisationTxtLayout.setErrorEnabled(true);
                prioritisationTxtLayout.setError("The selected prioritisation method is invalid.");
                hasErrors |= true;
            }
        }else{
            prioritisationTxtLayout.setErrorEnabled(true);
            prioritisationTxtLayout.setError("The prioritisation method is required.");
            hasErrors |= true;
        }

        String durationBetweenActivities = durationBetweenActivitiesTxt.getText().toString();
        durationBetweenActivitiesTxtLayout.setErrorEnabled(false);
        durationBetweenActivitiesTxtLayout.setError(null);
        if(durationBetweenActivities.length() > 0){
            try{
                int duration = Integer.parseInt(durationBetweenActivities);
                if(duration < 1){ //less than 1 minute
                    durationBetweenActivitiesTxtLayout.setErrorEnabled(true);
                    durationBetweenActivitiesTxtLayout.setError("The given duration is invalid.");
                    hasErrors |= true;
                }
            }catch (Exception e){
                durationBetweenActivitiesTxtLayout.setErrorEnabled(true);
                durationBetweenActivitiesTxtLayout.setError("The given duration is invalid.");
                hasErrors |= true;
            }
        }else{
            durationBetweenActivitiesTxtLayout.setErrorEnabled(true);
            durationBetweenActivitiesTxtLayout.setError("The duration field is required.");
            hasErrors |= true;
        }

        String dayStartedAt = dayStartsAtTxt.getText().toString();
        dayStartsAtTxtLayout.setErrorEnabled(false);
        dayStartsAtTxtLayout.setError(null);
        if(dayStartedAt.length() < 1){
            dayStartsAtTxtLayout.setErrorEnabled(true);
            dayStartsAtTxtLayout.setError("The day starts at time is required.");
            hasErrors |= true;
        }else{
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                sdf.parse(dayStartedAt, new ParsePosition(0));

            }catch (Exception e){
                dayStartsAtTxtLayout.setErrorEnabled(true);
                dayStartsAtTxtLayout.setError("The day starts at time is invalid.");
                hasErrors |= true;
            }
        }

        String dayEndsAt = dayEndsAtTxt.getText().toString();
        dayEndsAtTxtLayout.setErrorEnabled(false);
        dayEndsAtTxtLayout.setError(null);
        if(dayEndsAt.length() < 1){
            dayEndsAtTxtLayout.setErrorEnabled(true);
            dayEndsAtTxtLayout.setError("The day ends at time is required.");
            hasErrors |= true;
        }else{
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                sdf.parse(dayEndsAt, new ParsePosition(0));

            }catch (Exception e){
                dayEndsAtTxtLayout.setErrorEnabled(true);
                dayEndsAtTxtLayout.setError("The day ends at time is invalid.");
                hasErrors |= true;
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
            loadingIndicator.setVisibility(View.INVISIBLE);
        }

        prioritisationTxt.setEnabled(!isLoading);
        durationBetweenActivitiesTxt.setEnabled(!isLoading);
        dayEndsAtTxt.setEnabled(!isLoading);
        dayStartsAtTxt.setEnabled(!isLoading);
        saveButton.setEnabled(!isLoading);
    }

    private void processErrors(String message, @Nullable JSONObject errorObj){
        try {
            if(errorObj == null){
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }else{
                JSONObject errors = errorObj.getJSONObject("errors");

                if(errors.has("prioritization")){
                    prioritisationTxtLayout.setError(errors.getJSONArray("prioritization").get(0).toString());
                }else{
                    prioritisationTxtLayout.setError(null);
                }

                if(errors.has("duration_between_activities")){
                    durationBetweenActivitiesTxtLayout.setError(errors.getJSONArray("duration_between_activities").get(0).toString());
                }else{
                    durationBetweenActivitiesTxtLayout.setError(null);
                }

                if(errors.has("day_starts_at")){
                    dayStartsAtTxtLayout.setError(errors.getJSONArray("day_starts_at").get(0).toString());
                }else{
                    dayStartsAtTxtLayout.setError(null);
                }

                if(errors.has("day_ends_at")){
                    dayEndsAtTxtLayout.setError(errors.getJSONArray("day_ends_at").get(0).toString());
                }else{
                    dayEndsAtTxtLayout.setError(null);
                }
            };
        } catch (JSONException e) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }
    private void resetErrors(){
        prioritisationTxtLayout.setError(null);
        durationBetweenActivitiesTxtLayout.setError(null);
        dayStartsAtTxtLayout.setError(null);
        dayEndsAtTxtLayout.setError(null);
    }
    private void loadData(){

        Mapper.GetSettings(requireActivity(), new Mapper.TaskTransition() {
            @Override
            public void onBefore() {
                setLoadingState(true);
                resetErrors();
                errorRetryLayout.setVisibility(View.INVISIBLE);
                pageLayout.setVisibility(View.VISIBLE);
            }
        }, new Mapper.ResponseCallBack() {
            @Override
            public void onSuccess(JSONObject response) {
                setLoadingState(false);

                try {
                    JSONObject prioritizationObj = response.getJSONObject("prioritization");

                    int prioritizationValue = prioritizationObj.getInt("value");

                    prioritisationItemsAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, Settings.PrioritisationMethods);
                    prioritisationTxt.setAdapter(prioritisationItemsAdapter);
                    prioritisationTxt.setText(prioritisationItemsAdapter.getItem(((int)(prioritizationValue / 100) - 1)).toString());


                    try {
                        durationBetweenActivitiesTxt.setText(String.valueOf(response.getInt("duration_between_activities")));
                    }catch (Exception $e){
                        $e.printStackTrace();
                    }

                    try {
                        activityMaxDurationTxt.setText(String.valueOf(response.getInt("activity_max_duration")));
                    }catch (Exception $e){
                        $e.printStackTrace();
                    }

                    try {
                        dayStartsAtTxt.setText(response.getString("day_starts_at"));
                    }catch (Exception $e){}

                    try {
                        dayEndsAtTxt.setText(response.getString("day_ends_at"));
                    }catch (Exception $e){}

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String message, @Nullable JSONObject error) {
                setLoadingState(false);
                errorRetryLayout.setVisibility(View.VISIBLE);
                pageLayout.setVisibility(View.INVISIBLE);
                errorMessageText.setText(message);
            }

            @Override
            public void NotAuthenticated() {
                setLoadingState(false);
                LogoutDialog.resetAuth(getActivity());
            }
        });
    }


    private void saveData(){
        if (validateFields()) { // all fields are valid
            setLoadingState(true); // lock all fields
            resetErrors();

            Settings settings = new Settings();
            settings.prioritisation = (prioritisationItemsAdapter.getPosition(prioritisationTxt.getText().toString()) + 1) * 100;
            settings.durationBetweenActivities = Integer.parseInt(durationBetweenActivitiesTxt.getText().toString());
            settings.activityMaxDuration = Integer.parseInt(activityMaxDurationTxt.getText().toString());

            settings.dayStartsAt = dayStartsAtTxt.getText().toString();
            settings.dayEndsAt = dayEndsAtTxt.getText().toString();

            Mapper.UpdateSettings(requireActivity(), settings, new Mapper.TaskTransition() {
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
                    Toast.makeText(getActivity(), "Done", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message, JSONObject error) {
                    setLoadingState(false);
                    processErrors(message, error);
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}