package com.example.mapper.activities.ui.schedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.models.Schedule;
import com.example.mapper.models.Subject;

import java.util.Collections;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleViewHolder>{
    List<Schedule> list = Collections.emptyList();

    Context context;
    ClickListener clickListener;

    public ScheduleAdapter(List<Schedule> list,
                           Context context, ClickListener listener)
    {
        this.list = list;
        this.context = context;
        this.clickListener = listener;
    }


    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        final int index = holder.getAdapterPosition();

        Schedule schedule = list.get(position);

        holder.noText.setText(String.format("%02d", position + 1));
        holder.subjectNameText.setText(schedule.module.subjectName);
        holder.moduleNameText.setText(schedule.module.name);
        holder.startAtText.setText(schedule.start_at_time);
        holder.endAtText.setText(schedule.end_at_time);
        holder.dateTxt.setText(schedule.date);

        holder.itemView.setOnClickListener(view -> clickListener.click(index));
        holder.menuBtn.setOnClickListener(view -> clickListener.onOptionClick(index));

        if(schedule.isFinished){
            holder.notFinishedBtn.setVisibility(View.GONE);
            holder.finishedBtn.setVisibility(View.VISIBLE);
        }else{
            holder.notFinishedBtn.setVisibility(View.VISIBLE);
            holder.finishedBtn.setVisibility(View.GONE);
        }

        holder.finishedBtn.setOnClickListener(view -> {
            clickListener.onStatusToggle(holder, position, !schedule.isFinished);
        });
        holder.notFinishedBtn.setOnClickListener(view -> {
            clickListener.onStatusToggle(holder, position, !schedule.isFinished);
        });

    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context
                = parent.getContext();
        LayoutInflater inflater
                = LayoutInflater.from(context);

        // Inflate the layout
        View subjectView = inflater.inflate(R.layout.schedule_slot_card, parent, false);
        ScheduleViewHolder viewHolder = new ScheduleViewHolder(subjectView);

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
