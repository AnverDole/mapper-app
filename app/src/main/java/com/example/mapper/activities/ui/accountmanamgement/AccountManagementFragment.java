package com.example.mapper.activities.ui.accountmanamgement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.mapper.activities.ChangePasswordActivity;
import com.example.mapper.activities.IndexActivity;
import com.example.mapper.activities.dialogs.LogoutDialog;
import com.example.mapper.activities.viewmodels.AccountManagementViewModel;
import com.example.mapper.apis.Mapper;
import com.example.mapper.databinding.FragmentAccountManagementBinding;
import com.example.mapper.models.AccountManagement;
import com.example.mapper.models.User;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class AccountManagementFragment extends Fragment {
    private FragmentAccountManagementBinding binding;
    LinearProgressIndicator loadingIndicator;
    LinearLayout errorRetryLayout;
    ScrollView pageLayout;


    TextView errorMessageText;
    Button retryBtn;
    Button changePasswordBtn;


    TextInputLayout firstNameTextLayout;
    TextInputEditText firstNameText;

    TextInputLayout lastNameTextLayout;
    TextInputEditText lastNameText;

    Button saveButton;


    AccountManagementViewModel accountManagementViewModel;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAccountManagementBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        accountManagementViewModel = new ViewModelProvider(this).get(AccountManagementViewModel.class);


        loadingIndicator = binding.loadingIndicator;

        errorRetryLayout = binding.errorRetryLayout;
        errorMessageText = binding.errorMessageText;
        pageLayout = binding.pageLayout;
        retryBtn = binding.retryBtn;
        changePasswordBtn = binding.changePasswordBtn;

        firstNameTextLayout = binding.firstNameTxtLayout;
        firstNameText = binding.firstNameTxt;

        lastNameTextLayout = binding.lastNameTxtLayout;
        lastNameText = binding.lastNameTxt;

        saveButton = binding.saveBtn;

        View.OnFocusChangeListener firstNameFocusChangeListener = (view, b) -> {
            accountManagementViewModel.firstName().setValue(firstNameText.getText().toString());
        };
        View.OnFocusChangeListener lastNameFocusChangeListener = (view, b) -> {
            accountManagementViewModel.lastName().setValue(lastNameText.getText().toString());
        };

        firstNameText.setOnFocusChangeListener(firstNameFocusChangeListener);
        lastNameText.setOnFocusChangeListener(lastNameFocusChangeListener);

        saveButton.setOnClickListener(view->{
            saveData();
        });
        retryBtn.setOnClickListener(view -> {
            loadData(accountManagementViewModel);
        });
        changePasswordBtn.setOnClickListener(view -> {
            Intent intent = new Intent(requireActivity(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        accountManagementViewModel.firstName().observe(getActivity(), firstname -> {
            firstNameText.setText(firstname);
        });

        accountManagementViewModel.lastName().observe(getActivity(), lastname -> {
            lastNameText.setText(lastname);
        });

        IndexActivity.registerOptionMenu(R.menu.activity_index_menu);
        IndexActivity.registerOptionMenuItemClickEventObserver(R.id.nav_account_management, R.id.menu_refresh, () -> {
            loadData(accountManagementViewModel);
        });
        loadData(accountManagementViewModel);


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private boolean validateFields(){
        boolean hasErrors = false;

        resetErrors();

        String firstName = firstNameText.getText().toString();
        String lastName = lastNameText.getText().toString();

        if(firstName.length() < 1){
            firstNameTextLayout.setErrorEnabled(true);
            firstNameTextLayout.setError("The first name field is required.");
            hasErrors |= true;
        }

        if(lastName.length() < 1){
            lastNameTextLayout.setErrorEnabled(true);
            lastNameTextLayout.setError("The last name field is required.");
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
            loadingIndicator.setVisibility(View.VISIBLE);
        }else{
            loadingIndicator.setVisibility(View.INVISIBLE);
        }

        firstNameText.setEnabled(!isLoading);
        lastNameText.setEnabled(!isLoading);
        saveButton.setEnabled(!isLoading);
    }

    private void processErrors(String message, @Nullable JSONObject errorObj){
        try {
            if(errorObj == null){
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }else{
                JSONObject errors = errorObj.getJSONObject("errors");

                if(errors.has("first_name")){
                    lastNameTextLayout.setError(errors.getJSONArray("first_name").get(0).toString());
                }else{
                    lastNameTextLayout.setError(null);
                }

                if(errors.has("last_name")){
                    lastNameTextLayout.setError(errors.getJSONArray("last_name").get(0).toString());
                }else{
                    lastNameTextLayout.setError(null);
                }
            };
        } catch (JSONException e) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }
    private void resetErrors(){
        firstNameTextLayout.setError(null);
        firstNameTextLayout.setErrorEnabled(false);

        lastNameTextLayout.setError(null);
        lastNameTextLayout.setErrorEnabled(false);
    }
    private void loadData(AccountManagementViewModel accountManagementViewModel){
        Mapper.GetAccountManagement(requireActivity(), new Mapper.TaskTransition() {
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

                    String firstName = response.getString("first_name");
                    String lastName = response.getString("last_name");

                    accountManagementViewModel.firstName().setValue(firstName);
                    accountManagementViewModel.lastName().setValue(lastName);

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

            AccountManagement accountManagement = new AccountManagement();
            accountManagement.firstName = accountManagementViewModel.firstName().getValue();
            accountManagement.lastName = accountManagementViewModel.lastName().getValue();

            Mapper.UpdateAccountManagement(requireActivity(), accountManagement, new Mapper.TaskTransition() {
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

                    User user = Mapper.GetAuthenticatedUser();
                    user.firstname =  accountManagement.firstName;
                    user.lastname =  accountManagement.lastName;
                    IndexActivity.refreshLeftMenu();

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