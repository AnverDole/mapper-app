package com.example.mapper.activities.ui.subjects;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;

public class SubjectViewHolder extends RecyclerView.ViewHolder{
    TextView nameText;
    TextView modulesCountText;
    TextView indexText;
    AppCompatImageButton menuBtn;

    public SubjectViewHolder(@NonNull View itemView) {
        super(itemView);

        nameText = itemView.findViewById(R.id.name_txt);
        modulesCountText = itemView.findViewById(R.id.modules_count_txt);
        indexText = itemView.findViewById(R.id.no_txt);
        menuBtn = itemView.findViewById(R.id.menu_btn);
    }
}
