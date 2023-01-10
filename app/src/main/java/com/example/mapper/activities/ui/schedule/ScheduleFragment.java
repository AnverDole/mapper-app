package com.example.mapper.activities.ui.schedule;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mapper.R;
import com.example.mapper.activities.IndexActivity;
import com.example.mapper.activities.dialogs.GenerateScheduleModuleDialog;
import com.example.mapper.activities.dialogs.LogoutDialog;
import com.example.mapper.activities.ui.subjects.modules.ModuleActivity;
import com.example.mapper.apis.Mapper;
import com.example.mapper.databinding.FragmentScheduleBinding;
import com.example.mapper.models.Module;
import com.example.mapper.models.Schedule;
import com.example.mapper.models.Subject;
import com.example.mapper.synchronization.SyncAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ScheduleFragment extends Fragment {

    private FragmentScheduleBinding binding;


    private int page = 1;
    private int perPage = 10;
    private boolean hasMoreSlots = true;
    private boolean isFetching = false;

    private int filterStatus = 0;


    private ArrayList<Schedule> schedules = new ArrayList<>();
    private ScheduleAdapter scheduleAdapter;
    RecyclerView recyclerView;
    ClickListener clickListener;
    ScheduleSelectedBottomSheet scheduleSelectedBottomSheet;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentScheduleBinding.inflate(inflater, container, false);
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
        clickListener = new ClickListener() {
            @Override
            public void click(int index) {
                Schedule schedule = schedules.get(index);

                Intent intent = new Intent(getContext(), ModuleActivity.class);
                intent.putExtra("MODULE_ID", schedule.module.id);
                intent.putExtra("MODULE_NO", index + 1);
                intent.putExtra("MODULE_NAME", schedule.module.name);
                intent.putExtra("MODULE_PRIORITY",  schedule.module.priority);
                intent.putExtra("MODULE_PRIORITY_TEXT", schedule.module.getPriorityString());
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

        scheduleAdapter = new ScheduleAdapter(schedules, requireContext(), clickListener);
        recyclerView.setAdapter(scheduleAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        binding.retryBtn.setOnClickListener(view -> loadSchedule());

        binding.generateSchedule.setOnClickListener(view -> {
            generateSchedule();
        });

        IndexActivity.registerOptionMenu(R.menu.schedule_menu);
        IndexActivity.registerOptionMenuItemClickEventObserver(R.id.nav_schedule, R.id.menu_generate_schedule, () -> {
            generateSchedule();
        });
        IndexActivity.registerOptionMenuItemClickEventObserver(R.id.nav_schedule, R.id.menu_refresh, () -> {
            loadSchedule();
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

        loadSchedule();


        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        IndexActivity.registerOptionMenuItemClickEventObserver(R.id.nav_schedule, R.id.menu_refresh, () -> {
            loadSchedule();
        });
    }

    private void loadSchedule(){
        setMainLoadingSectionVisibility(true);
        setContentSectionVisibility(false);
        setErrorsSectionVisibility(false);
        setNoScheduleSectionVisibility(false);

        schedules.clear();
        page = 1;
        isFetching = true;
        Mapper.fetchSchedule(requireActivity(), filterStatus, perPage, page, new Mapper.TaskTransition() {},
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

        Mapper.fetchSchedule(requireActivity(), filterStatus, perPage, page, new Mapper.TaskTransition() {},
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

    private void generateSchedule(){
        GenerateScheduleModuleDialog generateScheduleModuleDialog = new GenerateScheduleModuleDialog();
        generateScheduleModuleDialog.setEventListeners(() -> {
            loadSchedule();

            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            settingsBundle.putString("auth_token", Mapper.GetAuthenticationToken(getContext()));

            /*
             * Request the sync for the default account, authority, and
             * manual sync settings
             */
            Account account = SyncAccount.Create(getContext());
            ContentResolver.requestSync(account, getContext().getString(R.string.authority), settingsBundle);

        });
        generateScheduleModuleDialog.show(getActivity(), binding.getRoot());
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

            switch (filterStatus){
                case 0:
                    binding.noContentMsg.setText(getContext().getString(R.string.schedule_fragment_no_tasks));
                    binding.generateSchedule.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    binding.noContentMsg.setText(getContext().getString(R.string.schedule_fragment_no_pending_tasks));
                    binding.generateSchedule.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    binding.noContentMsg.setText(getContext().getString(R.string.schedule_fragment_no_finished_tasks));
                    binding.generateSchedule.setVisibility(View.GONE);
                    break;
            }

            binding.noTasksSection.setVisibility(View.VISIBLE);
        }else{
            binding.noTasksSection.setVisibility(View.GONE);
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        scheduleSelectedBottomSheet = null;
    }

    private interface ToggleScheduleFinish{
        void onStatusToggleSuccess();
        void onStatusToggleFailed();
    }

}