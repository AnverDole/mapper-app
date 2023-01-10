package com.example.mapper.activities.ui.subjects.modules;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.example.mapper.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ModuleSelectedBottomSheet extends BottomSheetDialog {


    View sheetView;

    private Actions actions;
    private int index;

    public ModuleSelectedBottomSheet(@NonNull Context context) {
        super(context);

        sheetView = this.getLayoutInflater().inflate(R.layout.module_selected_bottom_sheet, null);
        this.setContentView(sheetView);
    }

    public void show(Integer index) {
        this.index = index;
        super.show();
    }

    public void setActions(Actions actions){
        this.actions = actions;
        sheetView.findViewById(R.id.edit_btn).setOnClickListener(view -> {
            actions.onEdit(this.index);
            this.hide();
        });

        sheetView.findViewById(R.id.delete_btn).setOnClickListener(view -> {
            actions.onDelete(this.index);
            this.hide();
        });
    }

    public interface Actions{
        void onEdit(int index);
        void onDelete(int index);
    }
}
