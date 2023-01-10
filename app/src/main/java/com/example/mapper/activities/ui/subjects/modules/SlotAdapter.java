package com.example.mapper.activities.ui.subjects.modules;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.models.Schedule;

import java.util.Collections;
import java.util.List;

public class SlotAdapter extends RecyclerView.Adapter<SlotViewHolder>{
    List<Schedule> list = Collections.emptyList();

    Context context;
    SlotClickListener clickListener;

    public SlotAdapter(List<Schedule> list, Context context, SlotClickListener listener)
    {
        this.list = list;
        this.context = context;
        this.clickListener = listener;
    }


    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        final int index = holder.getAdapterPosition();

        Schedule schedule = list.get(position);

        holder.phaseText.setText(String.format("Phase %02d", position + 1));
        holder.finishStatusSwitch.setChecked(schedule.isFinished);
        holder.dateTxt.setText(schedule.date);
        holder.startTimeTxT.setText(schedule.start_at_time);
        holder.endTimeTxt.setText(schedule.end_at_time);

        holder.finishStatusSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            clickListener.onStatusToggle(holder, position, b);
        });

        holder.itemView.setOnClickListener(view -> {
            clickListener.click(index);
        });
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the layout
        View subjectView = inflater.inflate(R.layout.module_slot_card, parent, false);
        SlotViewHolder viewHolder = new SlotViewHolder(subjectView);

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
