package com.example.mapper.activities.ui.subjects.modules;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mapper.R;
import com.example.mapper.activities.dialogs.DeleteModuleDialog;
import com.example.mapper.activities.dialogs.DeleteSubjectDialog;
import com.example.mapper.activities.dialogs.LogoutDialog;
import com.example.mapper.activities.ui.subjects.ClickListener;
import com.example.mapper.activities.ui.subjects.EditSubjectActivity;
import com.example.mapper.activities.ui.subjects.SubjectSelectedBottomSheet;
import com.example.mapper.apis.Mapper;
import com.example.mapper.databinding.ActivityModulesBinding;
import com.example.mapper.models.Module;
import com.example.mapper.models.Subject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ModulesActivity extends AppCompatActivity {

    private ActivityModulesBinding binding;

    public static final String SUBJECT_NO_KEY = "subject_no";
    public static final String SUBJECT_ID_KEY = "subject_id";
    public static final String SUBJECT_NAME_KEY = "subject_name";
    public static int SUCCESS_DELETE_RESULT_CODE = 1002;

    SubjectSelectedBottomSheet subjectSelectedBottomSheet;
    ModuleSelectedBottomSheet moduleSelectedBottomSheet;

    private ArrayList<Module> modules = new ArrayList<>();

    private ModuleAdapter moduleAdapter;
    RecyclerView recyclerView;
    ClickListener clickListener;
    Integer subjectId;

    private int page = 1;
    private int perPage = 10;
    private boolean hasMoreSubjects = true;
    private boolean isFetching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityModulesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = binding.modulesRecyclerView;

        Integer subjectNo = this.getIntent().getIntExtra(SUBJECT_NO_KEY, -1);
        subjectId = this.getIntent().getIntExtra(SUBJECT_ID_KEY, -1);

        getSupportActionBar().setTitle(this.getIntent().getStringExtra(SUBJECT_NAME_KEY));

        ActivityResultLauncher<Intent> refreshableSubjectNameResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == EditSubjectActivity.SUCCESS_RESULT_CODE) {
                        getSupportActionBar().setTitle(this.getIntent().getStringExtra(SUBJECT_NAME_KEY));
                    }
                });

        ActivityResultLauncher<Intent> refreshableModulesResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == EditModuleActivity.SUCCESS_RESULT_CODE
                        || result.getResultCode() == NewModuleActivity.SUCCESS_RESULT_CODE) {
                        loadModules();
                    }
                });

        clickListener = new ClickListener() {
            @Override
            public void click(int index) {
                Intent intent = new Intent(ModulesActivity.this, ModuleActivity.class);
                intent.putExtra("MODULE_ID", modules.get(index).id);
                intent.putExtra("MODULE_NO", index + 1);
                intent.putExtra("MODULE_NAME", modules.get(index).name);
                intent.putExtra("MODULE_PRIORITY", modules.get(index).priority);
                intent.putExtra("MODULE_PRIORITY_TEXT", modules.get(index).getPriorityString());
                intent.putExtra("MODULE_DURATION", modules.get(index).duration);
                intent.putExtra("SUBJECT_NAME", ModulesActivity.this.getIntent().getStringExtra(SUBJECT_NAME_KEY));
                intent.putExtra("SUBJECT_ID", subjectId);
                refreshableModulesResultLauncher.launch(intent);
            }

            @Override
            public void onOptionClick(int index) {
                moduleSelectedBottomSheet.show(index);
            }
        };

        moduleAdapter = new ModuleAdapter(modules, this, clickListener);
        recyclerView.setAdapter(moduleAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        subjectSelectedBottomSheet = new SubjectSelectedBottomSheet(this);
        moduleSelectedBottomSheet = new ModuleSelectedBottomSheet(this);


        binding.contentSection.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                    if(!hasMoreSubjects) return;
                    if(isFetching) return;

                    // on scroll change it is checking when users scroll as bottom.
                    if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                        // in this method it is incrementing page number,
                        // making progress bar visible and calling fetch data method.
                        page++;
                        binding.nextPageLoadIndicator.setVisibility(View.VISIBLE);
                        loadModules(perPage, page);
                    }
                });

//        binding.subjectOptionBtn.setOnClickListener(view -> {
//            subjectSelectedBottomSheet.show();
//        });
        binding.retryBtn.setOnClickListener(view -> {
            loadModules();
        });
        binding.noModulesSectionNewBtn.setOnClickListener(view -> {
            Intent intent = new Intent(ModulesActivity.this, NewModuleActivity.class);
            intent.putExtra("subject_id", subjectId);
            refreshableModulesResultLauncher.launch(intent);
        });
        binding.newModuleFab.setOnClickListener(view -> {
            Intent intent = new Intent(ModulesActivity.this, NewModuleActivity.class);
            intent.putExtra("subject_id", subjectId);
            refreshableModulesResultLauncher.launch(intent);
        });

        subjectSelectedBottomSheet.setActions(new SubjectSelectedBottomSheet.Actions() {
            @Override
            public void onEdit() {
                Intent intent = new Intent(ModulesActivity.this, EditSubjectActivity.class);
                intent.putExtra(EditSubjectActivity.SUBJECT_ID_KEY, subjectId);
                intent.putExtra(EditSubjectActivity.SUBJECT_NAME_KEY, ModulesActivity.this.getIntent().getStringExtra(SUBJECT_NAME_KEY));
                intent.putExtra(EditSubjectActivity.SUBJECT_NO, subjectNo);
                refreshableSubjectNameResultLauncher.launch(intent);
            }

            @Override
            public void onDelete() {
                DeleteSubjectDialog deleteSubjectDialog = new DeleteSubjectDialog();

                deleteSubjectDialog.setEventListeners(() -> {
                    setResult(SUCCESS_DELETE_RESULT_CODE);
                    finish();
                });

                Subject subject = new Subject();
                subject.id = subjectId;
                subject.name = ModulesActivity.this.getIntent().getStringExtra(SUBJECT_NAME_KEY);
                deleteSubjectDialog.show(ModulesActivity.this, subject, subjectNo);
            }
        });
        moduleSelectedBottomSheet.setActions(new ModuleSelectedBottomSheet.Actions() {
            @Override
            public void onEdit(int index) {
                Intent intent = new Intent(ModulesActivity.this, EditModuleActivity.class);

                Module module = modules.get(index);

                intent.putExtra("subject_id", subjectId);

                intent.putExtra("subject_no", index + 1);
                intent.putExtra("module_id", module.id);
                intent.putExtra("module_name", module.name);
                intent.putExtra("module_priority", module.priority);
                intent.putExtra("module_duration", module.duration);




                refreshableModulesResultLauncher.launch(intent);
            }

            @Override
            public void onDelete(int index) {
                DeleteModuleDialog deleteModuleDialog = new DeleteModuleDialog();

                deleteModuleDialog.setEventListeners(() -> {
                    loadModules();
                });

                deleteModuleDialog.show(ModulesActivity.this, modules.get(index), subjectNo);
            }
        });

        loadModules();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadModules(){
        setMainLoadingSectionVisibility(true);
        setContentSectionVisibility(false);
        setErrorsSectionVisibility(false);
        setNoSubjectsSectionVisibility(false);

        modules.clear();
        page = 1;
        isFetching = true;
        Mapper.fetchModule(this, subjectId, perPage, page, new Mapper.TaskTransition() {},
                new Mapper.ResponseCallBack() {
                    @Override
                    public void onSuccess(JSONObject response) {

                        parseModules(response);


                        setMainLoadingSectionVisibility(false);

                        if(modules.isEmpty()){
                            setNoSubjectsSectionVisibility(true);
                            binding.newModuleFab.setVisibility(View.INVISIBLE);
                        }else{
                            setContentSectionVisibility(true);
                            binding.contentSection.scrollTo(0, 0);
                            binding.newModuleFab.setVisibility(View.VISIBLE);
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
                        LogoutDialog.resetAuth(ModulesActivity.this);
                    }
                });
    }
    private void loadModules(int perPage, int page){
        isFetching = true;
        Mapper.fetchModule(this, subjectId, perPage, page, new Mapper.TaskTransition() {},
                new Mapper.ResponseCallBack() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        parseModules(response);
                        isFetching = false;
                    }

                    @Override
                    public void onError(String message, @Nullable JSONObject error) {
                        Toast.makeText(ModulesActivity.this, message, Toast.LENGTH_SHORT).show();
                        isFetching = false;
                    }

                    @Override
                    public void NotAuthenticated() {
                        LogoutDialog.resetAuth(ModulesActivity.this);
                    }
                });
    }

    private void parseModules(JSONObject fetchResponse){
        try{
            JSONArray jsonSubjects =  fetchResponse.getJSONArray("modules");
            Log.i("Sdsd", fetchResponse.toString());
            JSONObject jsonPagination = fetchResponse.getJSONObject("pagination");
            hasMoreSubjects = jsonPagination.getBoolean("has_next");

            if(!hasMoreSubjects){
                binding.nextPageLoadIndicator.setVisibility(View.GONE);
            }

            for(int i = 0; i < jsonSubjects.length(); i++){
                JSONObject jsonSubject = jsonSubjects.getJSONObject(i);

                Module module = new Module();
                module.id = jsonSubject.getInt("id");
                module.name = jsonSubject.getString("title");
                module.duration = jsonSubject.getString("duration");
                module.priority = jsonSubject.getInt("priority");
                module.subjectName = jsonSubject.getString("subjectName");
                module.subjectId = jsonSubject.getInt("subjectId");
                modules.add(module);
            }

            moduleAdapter.notifyDataSetChanged();
        }catch (JSONException e){
            e.printStackTrace();
        }
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
            binding.contentSection.setVisibility(View.INVISIBLE);
        }
    }
    private void setErrorsSectionVisibility(boolean isVisible){
        if(isVisible){
            binding.errorSection.setVisibility(View.VISIBLE);
        }else{
            binding.errorSection.setVisibility(View.GONE);
        }
    }
    private void setNoSubjectsSectionVisibility(boolean isVisible){
        if(isVisible){
            binding.noSubjectsSection.setVisibility(View.VISIBLE);
        }else{
            binding.noSubjectsSection.setVisibility(View.GONE);
        }
    }

}