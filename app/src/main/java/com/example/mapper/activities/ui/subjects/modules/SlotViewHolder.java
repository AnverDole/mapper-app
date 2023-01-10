package com.example.mapper.activities.ui.subjects.modules;

import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SlotViewHolder extends RecyclerView.ViewHolder{
    TextView phaseText;
    TextView dateTxt;
    TextView startTimeTxT;
    TextView endTimeTxt;

    SwitchMaterial finishStatusSwitch;


    public SlotViewHolder(@NonNull View itemView) {
        super(itemView);

        phaseText = itemView.findViewById(R.id.phase);
        finishStatusSwitch = itemView.findViewById(R.id.is_finished_swt);
        dateTxt = itemView.findViewById(R.id.date);
        startTimeTxT = itemView.findViewById(R.id.start_time_txt);
        endTimeTxt = itemView.findViewById(R.id.end_time_txt);
    }

}
