package com.example.mapper.activities.viewmodels;

import androidx.databinding.BaseObservable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Date;
import java.util.List;

public class SettingsViewModel extends ViewModel {
    private MutableLiveData<Integer> prioritizationCategoryIndex;
    private MutableLiveData<Integer> durationBetweenActivities; // in minuets
    private MutableLiveData<Date> dayStartsAt;
    private MutableLiveData<Date> dayEndsAt;


    public MutableLiveData<Integer> prioritizationCategoryIndex(){
        if(prioritizationCategoryIndex == null){
            prioritizationCategoryIndex = new MutableLiveData<>();
        }
        return prioritizationCategoryIndex;
    }
    public MutableLiveData<Integer> durationBetweenActivities(){
        if(durationBetweenActivities == null){
            durationBetweenActivities = new MutableLiveData<>();
        }
        return durationBetweenActivities;
    }
    public MutableLiveData<Date> dayStartsAt(){
        if(dayStartsAt == null){
            dayStartsAt = new MutableLiveData<>();
        }
        return dayStartsAt;
    }
    public MutableLiveData<Date> dayEndsAt(){
        if(dayEndsAt == null){
            dayEndsAt = new MutableLiveData<>();
        }
        return dayEndsAt;
    }
}