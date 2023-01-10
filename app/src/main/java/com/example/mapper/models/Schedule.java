package com.example.mapper.models;

import java.util.Date;

public class Schedule {
    public int id;
    public String date;
    public String start_at_time;
    public String end_at_time;
    public boolean isFinished = false;
    public Module module;
}
