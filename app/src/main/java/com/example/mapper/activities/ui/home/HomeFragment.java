package com.example.mapper.activities.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.activities.IndexActivity;
import com.example.mapper.activities.dialogs.GenerateScheduleModuleDialog;
import com.example.mapper.activities.dialogs.LogoutDialog;
import com.example.mapper.activities.ui.schedule.ClickListener;
import com.example.mapper.activities.ui.schedule.EditScheduleSlotActivity;
import com.example.mapper.activities.ui.schedule.ScheduleAdapter;
import com.example.mapper.activities.ui.schedule.ScheduleFragment;
import com.example.mapper.activities.ui.schedule.ScheduleSelectedBottomSheet;
import com.example.mapper.activities.ui.schedule.ScheduleViewHolder;
import com.example.mapper.activities.ui.subjects.SubjectSelectedBottomSheet;
import com.example.mapper.activities.ui.subjects.modules.ModuleActivity;
import com.example.mapper.apis.Mapper;
import com.example.mapper.database.ScheduleSlot;
import com.example.mapper.databinding.FragmentHomeBinding;
import com.example.mapper.databinding.FragmentScheduleBinding;
import com.example.mapper.models.Module;
import com.example.mapper.models.Schedule;
import com.example.mapper.models.Subject;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private int page = 1;
    private int perPage = 10;
    private boolean hasMoreSlots = true;
    private boolean isFetching = false;

    private ArrayList<Schedule> schedules = new ArrayList<>();
    private ScheduleAdapter scheduleAdapter;
    RecyclerView recyclerView;
    ClickListener clickListener;
    ScheduleSelectedBottomSheet scheduleSelectedBottomSheet;

    private int filterStatus = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        scheduleSelectedBottomSheet = new ScheduleSelectedBottomSheet(getContext());

        recyclerView = binding.scheduleRecyclerView;

        ActivityResultLauncher<Intent> refreshableActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                    if (result.getResultCode() == EditScheduleSlotActivity.SUCCESS_RESULT_CODE) {
                        loadSchedule();
                    }
                });

        clickListener = new ClickListener() {
            @Override
            public void click(int index) {
                Schedule schedule = schedules.get(index);

                Intent intent = new Intent(getContext(), ModuleActivity.class);
                intent.putExtra("MODULE_ID", schedule.module.id);
                intent.putExtra("MODULE_NO", index + 1);
                intent.putExtra("MODULE_NAME", schedule.module.name);
                intent.putExtra("MODULE_PRIORITY",  schedule.module.priority);
                intent.putExtra("MODULE_PRIORITY_TEXT",  schedule.module.getPriorityString());
                intent.putExtra("MODULE_DURATION", schedule.module.duration);
                intent.putExtra("SUBJECT_NAME",  schedule.module.subjectName);
                intent.putExtra("SUBJECT_ID",  schedule.module.subjectId);
                refreshableActivityResultLauncher.launch(intent);
            }

            @Override
            public void onOptionClick(int index) {
                scheduleSelectedBottomSheet.show(index);
            }


            @Override
            public void onStatusToggle(ScheduleViewHolder scheduleViewHolder, int index, boolean isFinished) {
                Schedule schedule = schedules.get(index);

                toggleFinish(schedule, isFinished, new ToggleScheduleFinish() {
                    @Override
                    public void onStatusToggleSuccess() {
                        schedules.get(index).isFinished = isFinished;
                        scheduleAdapter.notifyItemChanged(index);
                        Toast.makeText(getActivity(), "Done", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onStatusToggleFailed() {
                        scheduleAdapter.notifyItemChanged(index);
                        Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        binding.contentSection.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                    if(!hasMoreSlots) return;
                    if(isFetching) return;

                    // on scroll change it is checking when users scroll as bottom.
                    if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                        // in this method it is incrementing page number,
                        // making progress bar visible and calling fetch data method.
                        page++;
                        binding.nextPageLoadIndicator.setVisibility(View.VISIBLE);
                        loadSchedule(perPage, page);
                    }
                });

        binding.filterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if(checkedIds.contains(R.id.pending)){
                filterStatus = 1;
            }else if(checkedIds.contains(R.id.finished)){
                filterStatus = 2;
            }else{
                filterStatus = 0;
            }
            loadSchedule();
        });

        scheduleAdapter = new ScheduleAdapter(schedules, requireContext(), clickListener);
        recyclerView.setAdapter(scheduleAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        binding.retryBtn.setOnClickListener(view -> loadSchedule());

        scheduleSelectedBottomSheet.setActions(index -> {
            Schedule schedule = schedules.get(index);

            Intent intent = new Intent(getContext(), EditScheduleSlotActivity.class);
            intent.putExtra(EditScheduleSlotActivity.SCHEDULE_MODULE_NAME, schedule.module.name);
            intent.putExtra(EditScheduleSlotActivity.SCHEDULE_SLOT_ID, schedule.id);
            intent.putExtra(EditScheduleSlotActivity.SCHEDULE_SLOT_DATE, schedule.date);
            intent.putExtra(EditScheduleSlotActivity.SCHEDULE_SLOT_START_TIME, schedule.start_at_time);
            intent.putExtra(EditScheduleSlotActivity.SCHEDULE_SLOT_END_TIME, schedule.end_at_time);

            refreshableActivityResultLauncher.launch(intent);
        });

        IndexActivity.registerOptionMenu(R.menu.activity_index_menu);
        IndexActivity.registerOptionMenuItemClickEventObserver(R.id.nav_home, R.id.menu_refresh, () -> {
            loadSchedule();
        });

        loadSchedule();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadSchedule(){
        setMainLoadingSectionVisibility(true);
        setContentSectionVisibility(false);
        setErrorsSectionVisibility(false);
        setNoScheduleSectionVisibility(false);

        schedules.clear();
        page = 1;
        isFetching = true;
        Mapper.fetchTodayActivities(requireActivity(), filterStatus, perPage, page, new Mapper.TaskTransition() {},
                new Mapper.ResponseCallBack() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        parseSchedule(response);


                        setMainLoadingSectionVisibility(false);

                        if(schedules.isEmpty()){
                            setNoScheduleSectionVisibility(true);
                        }else{
                            setContentSectionVisibility(true);
                            binding.contentSection.scrollTo(0, 0);
                        }


                        isFetching = false;
                    }

                    @Override
                    public void onError(String message, @Nullable JSONObject error) {
                        setMainLoadingSectionVisibility(false);
                        setErrorsSectionVisibility(true);

                        setMainLoadingSectionVisibility(false);

                        binding.errorMessageText.setText(message);
                        isFetching = false;
                    }

                    @Override
                    public void NotAuthenticated() {
                        LogoutDialog.resetAuth(getActivity());
                    }
                });
    }
    private void loadSchedule(int perPage, int page){
        isFetching = true;
        Mapper.fetchTodayActivities(requireActivity(), filterStatus, perPage, page, new Mapper.TaskTransition() {},
                new Mapper.ResponseCallBack() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        parseSchedule(response);
                        isFetching = false;
                    }

                    @Override
                    public void onError(String message, @Nullable JSONObject error) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        isFetching = false;
                    }

                    @Override
                    public void NotAuthenticated() {
                        LogoutDialog.resetAuth(getActivity());
                    }
                });
    }

    private void setMainLoadingSectionVisibility(boolean isVisible){
        if(isVisible){
            binding.loadingIndicatorSection.setVisibility(View.VISIBLE);
        }else{
            binding.loadingIndicatorSection.setVisibility(View.GONE);
        }
    }
    private void setContentSectionVisibility(boolean isVisible){
        if(isVisible){
            binding.contentSection.setVisibility(View.VISIBLE);
        }else{
            binding.contentSection.setVisibility(View.GONE);
        }
    }
    private void setErrorsSectionVisibility(boolean isVisible){
        if(isVisible){
            binding.errorSection.setVisibility(View.VISIBLE);
        }else{
            binding.errorSection.setVisibility(View.GONE);
        }
    }
    private void setNoScheduleSectionVisibility(boolean isVisible){
        if(isVisible){
            binding.noScheduleSection.setVisibility(View.VISIBLE);
        }else{
            binding.noScheduleSection.setVisibility(View.GONE);
        }
    }

    private void parseSchedule(JSONObject fetchResponse){
        try{
            JSONArray jsonScheduleSlots =  fetchResponse.getJSONArray("slots");


            JSONObject jsonPagination = fetchResponse.getJSONObject("pagination");
            hasMoreSlots = jsonPagination.getBoolean("has_next");

            if(!hasMoreSlots){
                binding.nextPageLoadIndicator.setVisibility(View.GONE);
            }

            for(int i = 0; i < jsonScheduleSlots.length(); i++){

                JSONObject jsonScheduleSlot = jsonScheduleSlots.getJSONObject(i);
                JSONObject jsonModule = jsonScheduleSlot.getJSONObject("module");
                JSONObject jsonSubject = jsonModule.getJSONObject("subject");

                Schedule schedule = new Schedule();
                schedule.id = jsonScheduleSlot.getInt("id");
                schedule.date = jsonScheduleSlot.getString("date");
                schedule.start_at_time = jsonScheduleSlot.getString("start_at_time");
                schedule.end_at_time = jsonScheduleSlot.getString("end_at_time");

                if(jsonScheduleSlot.getInt("is_finished") == 1){
                    schedule.isFinished = true;
                }else{
                    schedule.isFinished = false;
                }


                Module module = new Module();
                module.id = jsonModule.getInt("id");
                module.name = jsonModule.getString("title");
                module.subjectId = jsonSubject.getInt("id");
                module.subjectName = jsonSubject.getString("name");
                module.priority = jsonModule.getInt("priority");
                module.duration = jsonModule.getString("duration");

                Subject subject = new Subject();
                subject.id = jsonSubject.getInt("id");
                subject.name = jsonSubject.getString("name");

                module.subject = subject;
                schedule.module= module;


                this.schedules.add(schedule);


            }

            scheduleAdapter.notifyDataSetChanged();
        }catch (JSONException e){
            Log.e("SSD", e.getMessage());
            e.printStackTrace();
        }
    }
    private void toggleFinish(Schedule schedule, Boolean isFinished, ToggleScheduleFinish toggleScheduleFinish){
        Mapper.toggleFinishScheduleSlot(requireActivity(), schedule.id, isFinished, new Mapper.TaskTransition() {},
                new Mapper.ResponseCallBack() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Toast.makeText(getActivity(), "Done", Toast.LENGTH_SHORT).show();
                        toggleScheduleFinish.onStatusToggleSuccess();
                    }

                    @Override
                    public void onError(String message, @Nullable JSONObject error) {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        toggleScheduleFinish.onStatusToggleFailed();
                    }

                    @Override
                    public void NotAuthenticated() {
                        LogoutDialog.resetAuth(getActivity());
                    }
                });
    }

    private interface ToggleScheduleFinish{
        void onStatusToggleSuccess();
        void onStatusToggleFailed();
    }


}