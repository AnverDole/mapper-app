package com.example.mapper.activities.ui.subjects.modules;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapper.R;
import com.example.mapper.activities.IndexActivity;
import com.example.mapper.activities.dialogs.LogoutDialog;
import com.example.mapper.activities.ui.schedule.EditScheduleSlotActivity;
import com.example.mapper.activities.ui.schedule.ScheduleSelectedBottomSheet;
import com.example.mapper.apis.Mapper;
import com.example.mapper.databinding.ActivityModuleBinding;
import com.example.mapper.models.Module;
import com.example.mapper.models.Schedule;
import com.example.mapper.models.Subject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ModuleActivity extends AppCompatActivity {

    private ActivityModuleBinding binding;

    private int page = 1;
    private int perPage = 10;
    private boolean hasMoreSlots = true;
    private boolean isFetching = false;

    private ArrayList<Schedule> slots = new ArrayList<>();
    private SlotAdapter slotAdapter;
    RecyclerView recyclerView;
    SlotClickListener clickListener;

    ScheduleSelectedBottomSheet scheduleSelectedBottomSheet;

    int moduleId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityModuleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        recyclerView = binding.scheduleRecyclerView;

        scheduleSelectedBottomSheet = new ScheduleSelectedBottomSheet(this);

        clickListener = new SlotClickListener() {
            @Override
            public void click(int index) {
                scheduleSelectedBottomSheet.show(index);
            }

            @Override
            public void onOptionClick(int index) {

            }

            @Override
            public void onStatusToggle(SlotViewHolder slotViewHolder, int index, boolean isFinished) {
                Schedule schedule = slots.get(index);
                toggleFinish(schedule, isFinished, new ToggleScheduleFinish() {
                    @Override
                    public void onStatusToggleSuccess() {

                    }

                    @Override
                    public void onStatusToggleFailed() {
                        slotAdapter.notifyItemChanged(index);
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

        String subjectName = getIntent().getStringExtra("SUBJECT_NAME");
        Integer subjectId = getIntent().getIntExtra("SUBJECT_ID", -1);


        moduleId = getIntent().getIntExtra("MODULE_ID", -1);
        String moduleName = getIntent().getStringExtra("MODULE_NAME");
        String modulePriorityText = getIntent().getStringExtra("MODULE_PRIORITY_TEXT");

        String duration = getIntent().getStringExtra("MODULE_DURATION");

        String hours = duration.split(":")[0];
        String minutes = duration.split(":")[1];
        binding.durationTxt.setText(String.format("%s Hours : %s Minutes", hours, minutes));

        getSupportActionBar().setTitle(moduleName);

        binding.subjectNameTxt.setText(subjectName);
        binding.priorityTxt.setText(modulePriorityText);


        slotAdapter = new SlotAdapter(slots, ModuleActivity.this, clickListener);
        recyclerView.setAdapter(slotAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ModuleActivity.this));

        binding.retryBtn.setOnClickListener(view -> loadSchedule());

        binding.subject.setOnClickListener(view -> {
            Intent intent = new Intent(this, ModulesActivity.class);
            intent.putExtra(ModulesActivity.SUBJECT_ID_KEY, subjectId);
            intent.putExtra(ModulesActivity.SUBJECT_NAME_KEY, subjectName);
            intent.putExtra(ModulesActivity.SUBJECT_NO_KEY, 1);
            startActivity(intent);
        });


        ActivityResultLauncher<Intent> refreshableActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == EditScheduleSlotActivity.SUCCESS_RESULT_CODE) {
                        loadSchedule();
                    }
                });

        scheduleSelectedBottomSheet.setActions(index -> {
            Schedule schedule = slots.get(index);

            Intent intent = new Intent(this, EditScheduleSlotActivity.class);
            intent.putExtra(EditScheduleSlotActivity.SCHEDULE_MODULE_NAME, schedule.module.name);
            intent.putExtra(EditScheduleSlotActivity.SCHEDULE_SLOT_ID, schedule.id);
            intent.putExtra(EditScheduleSlotActivity.SCHEDULE_SLOT_DATE, schedule.date);
            intent.putExtra(EditScheduleSlotActivity.SCHEDULE_SLOT_START_TIME, schedule.start_at_time);
            intent.putExtra(EditScheduleSlotActivity.SCHEDULE_SLOT_END_TIME, schedule.end_at_time);

            refreshableActivityResultLauncher.launch(intent);
        });

        IndexActivity.registerOptionMenuItemClickEventObserver(R.id.nav_schedule, R.id.menu_refresh, () -> {
            loadSchedule();
        });

        loadSchedule();
    }


    private void loadSchedule(){
        setMainLoadingSectionVisibility(true);
        setContentSectionVisibility(false);
        setErrorsSectionVisibility(false);
        setNoScheduleSectionVisibility(false);

        slots.clear();
        page = 1;
        isFetching = true;
        Mapper.fetchScheduleOfModule(this, moduleId, perPage, page, new Mapper.TaskTransition() {},
                new Mapper.ResponseCallBack() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        parseSchedule(response);

                        setMainLoadingSectionVisibility(false);

                        if(slots.isEmpty()){
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
                        LogoutDialog.resetAuth(ModuleActivity.this);
                    }
                });
    }
    private void loadSchedule(int perPage, int page){
        isFetching = true;
        Mapper.fetchSchedule(ModuleActivity.this, moduleId, perPage, page, new Mapper.TaskTransition() {},
                new Mapper.ResponseCallBack() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        parseSchedule(response);
                        isFetching = false;
                    }

                    @Override
                    public void onError(String message, @Nullable JSONObject error) {
                        Toast.makeText(ModuleActivity.this, message, Toast.LENGTH_SHORT).show();
                        isFetching = false;
                    }

                    @Override
                    public void NotAuthenticated() {
                        LogoutDialog.resetAuth(ModuleActivity.this);
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
                schedule.start_at_time = jsonScheduleSlot.getString("start_at_time");
                schedule.end_at_time = jsonScheduleSlot.getString("end_at_time");
                schedule.date = jsonScheduleSlot.getString("date");

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


                this.slots.add(schedule);
            }

            slotAdapter.notifyDataSetChanged();
            Log.e("SSD", "SDsd");
        }catch (JSONException e){
            Log.e("SSD", e.getMessage());
            e.printStackTrace();
        }
    }
    private void toggleFinish(Schedule schedule, Boolean isFinished, ToggleScheduleFinish toggleScheduleFinish){
        Mapper.toggleFinishScheduleSlot(this, schedule.id, isFinished, new Mapper.TaskTransition() {},
                new Mapper.ResponseCallBack() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Toast.makeText(ModuleActivity.this, "Done", Toast.LENGTH_SHORT).show();
                        toggleScheduleFinish.onStatusToggleSuccess();
                    }

                    @Override
                    public void onError(String message, @Nullable JSONObject error) {
                        Toast.makeText(ModuleActivity.this, message, Toast.LENGTH_SHORT).show();
                        toggleScheduleFinish.onStatusToggleFailed();
                    }

                    @Override
                    public void NotAuthenticated() {
                        LogoutDialog.resetAuth(ModuleActivity.this);
                    }
                });
    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private interface ToggleScheduleFinish{
        void onStatusToggleSuccess();
        void onStatusToggleFailed();
    }
}