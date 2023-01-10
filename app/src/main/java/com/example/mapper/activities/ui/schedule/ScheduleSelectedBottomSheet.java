package com.example.mapper.activities.ui.schedule;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.mapper.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ScheduleSelectedBottomSheet extends BottomSheetDialog {

    View sheetView;

    private Actions actions;
    private int index;

    public ScheduleSelectedBottomSheet(@NonNull Context context) {
        super(context);

        sheetView = this.getLayoutInflater().inflate(R.layout.schedule_selected_bottom_sheet, null);
        this.setContentView(sheetView);
    }

    public void show(Integer index) {
        this.index = index;
        super.show();
    }

    public void setActions(Actions actions){
        this.actions = actions;

        sheetView.findViewById(R.id.reschedule_btn).setOnClickListener(view -> {
            actions.onReSchedule(index);
            this.hide();
        });
    }

    public interface Actions{
        void onReSchedule(int index);
    }
}
