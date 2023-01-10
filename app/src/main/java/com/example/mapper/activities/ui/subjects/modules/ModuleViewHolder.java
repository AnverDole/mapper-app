package com.example.mapper.activities.ui.subjects.modules;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;

import javax.security.auth.Subject;

public class ModuleViewHolder extends RecyclerView.ViewHolder{
    TextView moduleNameText;
    TextView subjectNameTxt;
    TextView priorityText;
    TextView durationText;
    TextView indexText;
    AppCompatImageButton menuBtn;

    public ModuleViewHolder(@NonNull View itemView) {
        super(itemView);
        subjectNameTxt = itemView.findViewById(R.id.subject_name_txt);
        moduleNameText = itemView.findViewById(R.id.module_name);
        priorityText = itemView.findViewById(R.id.prioritisation_txt);
        durationText = itemView.findViewById(R.id.duration_txt);
        indexText = itemView.findViewById(R.id.no_txt);
        menuBtn = itemView.findViewById(R.id.menu_btn);
    }
}
