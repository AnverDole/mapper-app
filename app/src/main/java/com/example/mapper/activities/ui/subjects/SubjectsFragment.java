package com.example.mapper.activities.ui.subjects;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mapper.R;
import com.example.mapper.activities.IndexActivity;
import com.example.mapper.activities.dialogs.DeleteSubjectDialog;
import com.example.mapper.activities.dialogs.LogoutDialog;
import com.example.mapper.activities.ui.subjects.modules.ModulesActivity;
import com.example.mapper.apis.Mapper;
import com.example.mapper.databinding.FragmentSubjectsBinding;
import com.example.mapper.models.Subject;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SubjectsFragment extends Fragment{
    private FragmentSubjectsBinding binding;


    private int page = 1;
    private int perPage = 10;
    private boolean hasMoreSubjects = true;
    private boolean isFetching = false;

    private ArrayList<Subject> subjects = new ArrayList<>();
    private SubjectAdapter subjectAdapter;
    RecyclerView recyclerView;
    ClickListener clickListener;
    SubjectSelectedBottomSheet subjectSelectedBottomSheet;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentSubjectsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        subjectSelectedBottomSheet = new SubjectSelectedBottomSheet(getContext());

        recyclerView = binding.subjectsRecyclerView;


        ActivityResultLauncher<Intent> refreshableActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == NewSubjectActivity.SUCCESS_RESULT_CODE
                        || result.getResultCode() == EditSubjectActivity.SUCCESS_RESULT_CODE
                        || result.getResultCode() == ModulesActivity.SUCCESS_DELETE_RESULT_CODE
                    ) {
                        loadSubjects();
                    }
                });


        subjectSelectedBottomSheet.setActions(new SubjectSelectedBottomSheet.Actions() {

            @Override
            public void onEdit(int index) {
                Subject subject = subjects.get(index);

                Intent intent = new Intent(requireActivity(), EditSubjectActivity.class);
                intent.putExtra(EditSubjectActivity.SUBJECT_ID_KEY, subject.id);
                intent.putExtra(EditSubjectActivity.SUBJECT_NAME_KEY, subject.name);
                intent.putExtra(EditSubjectActivity.SUBJECT_NO, index);
                refreshableActivityResultLauncher.launch(intent);
            }

            @Override
            public void onDelete(int index) {
                Subject subject = subjects.get(index);
                DeleteSubjectDialog deleteSubjectDialog = new DeleteSubjectDialog();
                deleteSubjectDialog.setEventListeners(() -> loadSubjects());
                deleteSubjectDialog.show(getActivity(), subject, index + 1);
            }
        });
        clickListener = new ClickListener() {
            @Override
            public void click(int index) {
                Intent intent = new Intent(requireContext(), ModulesActivity.class);
                intent.putExtra(ModulesActivity.SUBJECT_ID_KEY, subjects.get(index).id);
                intent.putExtra(ModulesActivity.SUBJECT_NAME_KEY, subjects.get(index).name);
                intent.putExtra(ModulesActivity.SUBJECT_NO_KEY, index + 1);
                refreshableActivityResultLauncher.launch(intent);
            }

            @Override
            public void onOptionClick(int index) {
                subjectSelectedBottomSheet.show(index);
            }
        };


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
                loadSubjects(perPage, page);
            }
        });

        subjectAdapter = new SubjectAdapter(subjects, requireContext(), clickListener);
        recyclerView.setAdapter(subjectAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        binding.retryBtn.setOnClickListener(view -> loadSubjects());

        IndexActivity.registerOptionMenu(R.menu.activity_index_menu);
        IndexActivity.registerOptionMenuItemClickEventObserver(R.id.nav_subject, R.id.menu_refresh, () -> {
            loadSubjects();
        });

        binding.noSubjectsSectionNewBtn.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), NewSubjectActivity.class);
            refreshableActivityResultLauncher.launch(intent);
        });
        binding.newSubjectFab.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), NewSubjectActivity.class);
            refreshableActivityResultLauncher.launch(intent);
        });

        loadSubjects();

        return root;
    }


    private void loadSubjects(){
        setMainLoadingSectionVisibility(true);
        setContentSectionVisibility(false);
        setErrorsSectionVisibility(false);
        setNoSubjectsSectionVisibility(false);

        subjects.clear();
        page = 1;
        isFetching = true;
        Mapper.fetchSubject(requireActivity(), perPage, page, new Mapper.TaskTransition() {},
                new Mapper.ResponseCallBack() {
            @Override
            public void onSuccess(JSONObject response) {

                parseSubjects(response);


                setMainLoadingSectionVisibility(false);

                if(subjects.isEmpty()){
                    binding.newSubjectFab.setVisibility(View.INVISIBLE);
                    setNoSubjectsSectionVisibility(true);
                }else{
                    binding.newSubjectFab.setVisibility(View.VISIBLE);
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
    private void loadSubjects(int perPage, int page){
        isFetching = true;
        Mapper.fetchSubject(requireActivity(), perPage, page, new Mapper.TaskTransition() {},
                new Mapper.ResponseCallBack() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        parseSubjects(response);
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
    private void setNoSubjectsSectionVisibility(boolean isVisible){
        if(isVisible){
            binding.noSubjectsSection.setVisibility(View.VISIBLE);
        }else{
            binding.noSubjectsSection.setVisibility(View.GONE);
        }
    }

    private void parseSubjects(JSONObject fetchResponse){
        try{
            JSONArray jsonSubjects =  fetchResponse.getJSONArray("subjects");

            JSONObject jsonPagination = fetchResponse.getJSONObject("pagination");
            hasMoreSubjects = jsonPagination.getBoolean("has_next");

            if(!hasMoreSubjects){
                binding.nextPageLoadIndicator.setVisibility(View.GONE);
            }

            for(int i = 0; i < jsonSubjects.length(); i++){
                JSONObject jsonSubject = jsonSubjects.getJSONObject(i);

                Subject subject = new Subject();
                subject.id = jsonSubject.getInt("id");
                subject.name = jsonSubject.getString("name");
                subject.module_count = jsonSubject.getInt("moduleCount");
                subjects.add(subject);
            }

            subjectAdapter.notifyDataSetChanged();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        subjectSelectedBottomSheet = null;
    }

}