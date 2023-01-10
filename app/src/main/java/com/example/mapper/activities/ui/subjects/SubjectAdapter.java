package com.example.mapper.activities.ui.subjects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.models.Subject;

import java.util.Collections;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectViewHolder>{
    List<Subject> list = Collections.emptyList();

    Context context;
    ClickListener clickListener;

    public SubjectAdapter(List<Subject> list,
                          Context context, ClickListener listener)
    {
        this.list = list;
        this.context = context;
        this.clickListener = listener;
    }


    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        final int index = holder.getAdapterPosition();

        Subject subject = list.get(position);

        holder.nameText.setText(subject.name);
        holder.indexText.setText(String.format("%02d", position + 1));
        holder.modulesCountText.setText(String.format("%2d Modules", subject.module_count));

        holder.itemView.setOnClickListener(view -> clickListener.click(index));
        holder.menuBtn.setOnClickListener(view -> clickListener.onOptionClick(index));
    }


    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context
                = parent.getContext();
        LayoutInflater inflater
                = LayoutInflater.from(context);

        // Inflate the layout
        View subjectView = inflater.inflate(R.layout.subject_card, parent, false);
        SubjectViewHolder viewHolder = new SubjectViewHolder(subjectView);

        return viewHolder;
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }


}
