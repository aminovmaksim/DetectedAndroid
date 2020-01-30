package com.devian.detected.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.devian.detected.R;
import com.devian.detected.model.domain.network.ServerResponse;
import com.devian.detected.model.domain.tasks.Task;
import com.devian.detected.view.extra.ScanActivity;
import com.devian.detected.view.map_tab.MapViewModel;
import com.devian.detected.view.tasks_tab.TaskViewModel;
import com.devian.detected.view.profile_tab.ProfileViewModel;
import com.devian.detected.view.tasks_tab.TaskFragment;
import com.devian.detected.view.tasks_tab.TaskInfoFragment;
import com.devian.detected.model.domain.tasks.GeoTextTask;
import com.devian.detected.utils.ui.CustomViewPager;
import com.devian.detected.utils.ui.PagerAdapter;
import com.devian.detected.utils.ui.popups.DefaultPopup;
import com.devian.detected.utils.ui.popups.ResultPopup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements
        View.OnClickListener,
        TaskFragment.OnTaskItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_SCAN_CODE = 200;
    
    private FirebaseUser firebaseUser;

    private MainViewModel mainViewModel;
    private TaskViewModel taskViewModel;
    private MapViewModel mapViewModel;
    private ProfileViewModel profileViewModel;

    @BindView(R.id.fab_qr) FloatingActionButton fab_qr;
    @BindView(R.id.tab_layout) TabLayout tabLayout;
    @BindView(R.id.pager) CustomViewPager viewPager;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        taskViewModel = ViewModelProviders.of(this).get(TaskViewModel.class);
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mapViewModel = ViewModelProviders.of(this).get(MapViewModel.class);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        setupView();
        bindView();
    }

    public void setupView() {
        Log.d(TAG, "setupView: ");
        tabLayout.setupWithViewPager(viewPager);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        fab_qr.setOnClickListener(this);
    }

    public void bindView() {
        Log.d(TAG, "bindView: ");
        mainViewModel.bindCompletedTask().observe(this, taskDataWrapper -> {
            if (taskDataWrapper.isError()) {
                if (taskDataWrapper.getCode() == ServerResponse.TYPE_TASK_ALREADY_COMPLETED) {
                    showPopup(ResultPopup.RESULT_ACTIVATED, 0);
                }
                if (taskDataWrapper.getCode() == ServerResponse.TYPE_TASK_FAILURE) {
                    showPopup(ResultPopup.RESULT_FAILURE, 0);
                }
            } else {
                Task completedTask = taskDataWrapper.getObject();
                showPopup(ResultPopup.RESULT_SUCCESS, completedTask.getReward());
                taskViewModel.updateTaskList();
                mapViewModel.updateMarkers();
                profileViewModel.updateInformation(firebaseUser.getUid());
            }
        });
    }
    
    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: " + view.getId());
        if (view.getId() == R.id.fab_qr) {
            checkCameraPermission();
        }
    }

    public void proceedTask(String result) {
        Log.d(TAG, "proceedTask: " + result);
        mainViewModel.proceedTag(result, firebaseUser.getUid());
    }

    private void checkCameraPermission() {
        Log.d(TAG, "checkCameraPermission: ");
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            DefaultPopup popupPermissions =
                    new DefaultPopup(getResources().getString(R.string.camera_permission), this);
            popupPermissions.getPositiveOption().setOnClickListener(v -> {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                popupPermissions.dismiss();
            });
            popupPermissions.getNegativeOption().setOnClickListener(v -> {
                showToast(getResources().getString(R.string.camera_permission_denied), Toast.LENGTH_LONG);
                popupPermissions.dismiss();
            });
            popupPermissions.show();
        } else {
            Intent i = new Intent(this, ScanActivity.class);
            startActivityForResult(i, CAMERA_SCAN_CODE);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_SCAN_CODE) {
            if(resultCode == Activity.RESULT_OK){
                if (data != null) {
                    String result = data.getStringExtra("result");
                    proceedTask(result);
                }
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent i = new Intent(this, ScanActivity.class);
                startActivityForResult(i, 1);
            } else {
                showToast(getResources().getString(R.string.camera_permission_denied), Toast.LENGTH_LONG);
            }
        }
    }
    
    public void showToast(String text, int duration) {
        Toast.makeText(this, text, duration).show();
    }
    
    public void showPopup(int resultCode, int reward) {
        ResultPopup resultPopup = new ResultPopup(
                resultCode, reward, this);
        resultPopup.show();
    }
    
    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count != 0) {
            getSupportFragmentManager().popBackStack();
        }
    }
    
    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        if (fragment instanceof TaskFragment) {
            TaskFragment taskFragment = (TaskFragment) fragment;
            taskFragment.setOnTaskItemSelectedListener(this);
        }
    }
    
    @Override
    public void onTaskItemSelected(GeoTextTask task) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_main, TaskInfoFragment.newInstance(task), "taskinfo")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setCustomAnimations(R.animator.slide_in_left, R.animator.slide_in_right)
                .addToBackStack(TaskInfoFragment.class.getSimpleName())
                .commit();
    }
}
