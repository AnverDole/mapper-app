package com.example.mapper.models;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Settings {
    public static List<String> PrioritisationMethods = Arrays.asList(new String[]{
            "High Priority First",
            "Lowest Priority First",
            "Random"});

    public int prioritisation;
    public int durationBetweenActivities;
    public int activityMaxDuration;
    public String dayStartsAt;
    public String dayEndsAt;
}
