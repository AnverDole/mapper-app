package com.example.mapper.activities.ui.subjects;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.example.mapper.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class SubjectSelectedBottomSheet extends BottomSheetDialog {

    View sheetView;

    private Actions actions;
    private int index;

    public SubjectSelectedBottomSheet(@NonNull Context context) {
        super(context);

        sheetView = this.getLayoutInflater().inflate(R.layout.subject_selected_bottom_sheet, null);
        this.setContentView(sheetView);
    }

    public void show(Integer index) {
        this.index = index;
        super.show();
    }

    public void setActions(Actions actions){
        this.actions = actions;
        sheetView.findViewById(R.id.edit_subject_btn).setOnClickListener(view -> {
            actions.onEdit(index);
            actions.onEdit();
            this.hide();
        });

        sheetView.findViewById(R.id.delete_subject_btn).setOnClickListener(view -> {
            actions.onDelete(index);
            actions.onDelete();
            this.hide();
        });

    }

    public interface Actions{
        default void onEdit(int index){}
        default void onEdit(){}
        default void onDelete(int index){}
        default void onDelete(){}
    }
}
