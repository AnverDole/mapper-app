package com.example.mapper.database;

import android.content.Context;

import androidx.room.Room;

import java.util.List;

public class LocalScheduleSlotData {
    private AppDatabase db;
    public LocalScheduleSlotData(Context context){
        db = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "local.db")
                .build();
    }
    public List<ScheduleSlot> scheduleSlots() {
        return db.scheduleSlotDAO().getAll();
    }


    public boolean isExists(int id){
        return db.scheduleSlotDAO().isExists(id);
    }

    public void update(ScheduleSlot scheduleSlot){
        db.scheduleSlotDAO().update(scheduleSlot);
    }

    public void delete(ScheduleSlot scheduleSlot){
        db.scheduleSlotDAO().delete(scheduleSlot);
    }

    public void delete(int slotId){
        db.scheduleSlotDAO().delete(slotId);
    }


    public void deleteAll(){
        db.scheduleSlotDAO().deleteAll();
    }

    public void insert(ScheduleSlot scheduleSlot){
        db.scheduleSlotDAO().insert(scheduleSlot);
    }

}
