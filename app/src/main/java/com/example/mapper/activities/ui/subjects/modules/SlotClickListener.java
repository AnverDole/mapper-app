package com.example.mapper.activities.ui.subjects.modules;

import com.example.mapper.activities.ui.schedule.ScheduleViewHolder;

public interface SlotClickListener {
    void click(int index);
    void onOptionClick(int index);
    void onStatusToggle(SlotViewHolder slotViewHolder, int index, boolean isFinished);

}
