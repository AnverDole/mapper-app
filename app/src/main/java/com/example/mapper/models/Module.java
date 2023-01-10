package com.example.mapper.models;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Module {
    public int id;
    public String name;
    public String duration;
    public int priority;

    public String subjectName;
    public int subjectId;
    public Subject subject;

    public static List<String> PrioritisationMethods = Arrays.asList(new String[]{
            "High Priority",
            "Medium Priority",
            "Low Priority"
    });

    public String getPriorityString(){
        switch (priority){
            case 3:
                return "Low Priority";
            case 2:
                return "Medium Priority";
            case 1:
                return "High Priority";
            default:
                return "N/A";
        }
    }


}
