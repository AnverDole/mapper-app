package com.example.mapper.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Calendar;
import java.util.Date;

@Entity(tableName = "schedule_slot")
public class ScheduleSlot {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "module_id")
    public Integer moduleId;

    @ColumnInfo(name = "date")
    public String date;

    @ColumnInfo(name = "start_at")
    public String startAt;

    @ColumnInfo(name = "end_at")
    public String endAt;

    @ColumnInfo(name = "updated_at")
    public Date updatedAt = Calendar.getInstance().getTime();
}

