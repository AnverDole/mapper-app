package com.example.mapper.activities.ui.subjects.modules;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.activities.ui.subjects.ClickListener;
import com.example.mapper.models.Module;
import com.example.mapper.models.Subject;

import java.util.Collections;
import java.util.List;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleViewHolder>{
    List<Module> list = Collections.emptyList();

    Context context;
    ClickListener clickListener;

    public ModuleAdapter(List<Module> list,
                         Context context, ClickListener listener)
    {
        this.list = list;
        this.context = context;
        this.clickListener = listener;
    }


    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        final int index = holder.getAdapterPosition();

        Module module = list.get(position);

        holder.subjectNameTxt.setText(module.subjectName);
        holder.moduleNameText.setText(module.name);

        holder.priorityText.setText(module.getPriorityString());

        String hours = module.duration.split(":")[0];
        String minutes = module.duration.split(":")[1];
        holder.durationText.setText(String.format("%s Hours : %s Minutes", hours, minutes));

        holder.indexText.setText(String.format("%02d", position + 1));

        holder.itemView.setOnClickListener(view -> clickListener.click(index));
        holder.menuBtn.setOnClickListener(view -> clickListener.onOptionClick(index));
    }


    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context
                = parent.getContext();
        LayoutInflater inflater
                = LayoutInflater.from(context);

        // Inflate the layout
        View subjectView = inflater.inflate(R.layout.module_card, parent, false);
        ModuleViewHolder viewHolder = new ModuleViewHolder(subjectView);

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
