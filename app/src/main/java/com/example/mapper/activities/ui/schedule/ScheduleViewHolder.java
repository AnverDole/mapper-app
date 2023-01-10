package com.example.mapper.activities.ui.schedule;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;

public class ScheduleViewHolder extends RecyclerView.ViewHolder{
    TextView noText;
    TextView subjectNameText;
    TextView moduleNameText;
    TextView startAtText;
    TextView endAtText;
    TextView dateTxt;
    AppCompatImageButton menuBtn;
    AppCompatImageButton notFinishedBtn;
    AppCompatImageButton finishedBtn;

    public ScheduleViewHolder(@NonNull View itemView) {
        super(itemView);

        noText = itemView.findViewById(R.id.no_txt);
        subjectNameText = itemView.findViewById(R.id.subject_txt);
        moduleNameText = itemView.findViewById(R.id.module_name_txt);
        startAtText = itemView.findViewById(R.id.start_txt);
        endAtText = itemView.findViewById(R.id.end_txt);
        dateTxt = itemView.findViewById(R.id.date_txt);
        menuBtn = itemView.findViewById(R.id.menu_btn);
        notFinishedBtn = itemView.findViewById(R.id.not_finished_btn);
        finishedBtn = itemView.findViewById(R.id.finished_btn);
    }

}
