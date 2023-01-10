package com.example.mapper.database;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScheduleSlotDAO {
    @Insert
    void insertAll(ScheduleSlot... scheduleSlots);

    @Query("SELECT * FROM schedule_slot")
    List<ScheduleSlot> getAll();

    @Insert
    void insert(ScheduleSlot scheduleSlot);

    @Query("select exists (select * from schedule_slot where id = :id)")
    boolean isExists(int id);


    @Update
    void update(ScheduleSlot scheduleSlot);

    @Update
    void updateSlots(ScheduleSlot... scheduleSlot);

    @Delete
    void delete(ScheduleSlot scheduleSlot);

    @Delete
    void deleteSlots(ScheduleSlot... scheduleSlot);

    @Query("delete from schedule_slot where id =:id")
    void delete(int id);

    @Query("delete from schedule_slot")
    void deleteAll();
}