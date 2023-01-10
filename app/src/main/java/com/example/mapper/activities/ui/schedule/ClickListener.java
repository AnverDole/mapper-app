package com.example.mapper.activities.ui.schedule;

public interface ClickListener {
    void click(int index);
    void onOptionClick(int index);
    void onStatusToggle(ScheduleViewHolder scheduleViewHolder, int index, boolean isFinished);

}
