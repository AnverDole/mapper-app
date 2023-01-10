package com.example.mapper.activities.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mapper.models.Subject;

import java.util.ArrayList;
import java.util.Date;

public class SubjectsViewModel extends ViewModel {
    MutableLiveData<ArrayList<Subject>> subjects;
}