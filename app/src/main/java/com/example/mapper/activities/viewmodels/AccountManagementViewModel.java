package com.example.mapper.activities.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Date;

public class AccountManagementViewModel extends ViewModel {
    private MutableLiveData<String> firstname;
    private MutableLiveData<String> lastname;


    public MutableLiveData<String> firstName(){
        if(firstname == null){
            firstname = new MutableLiveData<>();
        }
        return firstname;
    }
    public MutableLiveData<String> lastName(){
        if(lastname == null){
            lastname = new MutableLiveData<>();
        }
        return lastname;
    }
}