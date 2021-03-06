package com.devian.detected.view.tasks_tab;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.devian.detected.R;
import com.devian.detected.model.domain.tasks.GeoTextTask;
import com.devian.detected.utils.LocalStorage;
import com.devian.detected.view.interfaces.OnTaskItemSelectedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener,
        TaskListAdapter.OnTaskItemClickListener {

    private static final String TAG = "TaskFragment";

    private OnTaskItemSelectedListener taskItemSelectedCallback;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.task_refreshLayout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.task_tvEmpty)
    TextView tvEmpty;

    private TaskListAdapter mAdapter;
    private TaskViewModel viewModel;
    private LocalStorage localStorage;

    private ArrayList<GeoTextTask> tasks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_task, container, false);
        ButterKnife.bind(this, v);

        localStorage = new LocalStorage(getActivityNonNull());
        viewModel = ViewModelProviders.of(getActivityNonNull()).get(TaskViewModel.class);
        setupView();
        bindView();

        return v;
    }

    private void bindView() {
        Log.d(TAG, "bind: ");
        viewModel.bindTaskList().observe(this, taskListWrapper -> {
            hideProgress();
            List<GeoTextTask> geoTextTaskList = taskListWrapper.getObject();
            if (geoTextTaskList == null || geoTextTaskList.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                tvEmpty.setVisibility(View.INVISIBLE);
                tasks = new ArrayList<>(geoTextTaskList);
                displayTaskList(tasks);
            }
        });
    }
    
    

    private void updateTaskList() {
        Log.d(TAG, "updateTaskList: ");
        showProgress();
        viewModel.updateTaskList();
    }

    private void displayTaskList(ArrayList<GeoTextTask> taskList) {
        Log.d(TAG, "displayTaskList: ");
        if (taskList == null || taskList.isEmpty())
            return;
        mAdapter.setTaskList(taskList);
    }

    private void setupView() {
        Log.d(TAG, "setupView: ");
        refreshLayout.setOnRefreshListener(this);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new TaskListAdapter(null, this);
        recyclerView.setAdapter(mAdapter);
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 74,
                getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new TaskListAdapter.TaskListItemDecorator(space));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        if (savedInstanceState != null) {
            ArrayList<GeoTextTask> tmp = savedInstanceState.getParcelableArrayList("tasks");
            if (tmp != null) {
                tasks = tmp;
            }
            refreshLayout.setRefreshing(savedInstanceState.getBoolean("refresh"));
        } else {
            GeoTextTask[] tmp = localStorage.getData("tasks", GeoTextTask[].class);
            if (tmp != null) {
                tasks = new ArrayList<>(Arrays.asList(tmp));
            }
            updateTaskList();
        }
        displayTaskList(tasks);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        if (tasks != null)
            outState.putParcelableArrayList("tasks", tasks);
        outState.putBoolean("refresh", refreshLayout.isRefreshing());
    }


    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh");
        if (isRefreshAvailable()) {
            showProgress();
            updateTaskList();
        } else {
            hideProgress();
        }
    }

    private Date lastRefresh = new Date();

    private boolean isRefreshAvailable() {
        Date currTime = new Date();
        if (currTime.getTime() - lastRefresh.getTime() >= getResources().getInteger(R.integer.refresh_delay)) {
            lastRefresh = currTime;
            return true;
        } else {
            return false;
        }
    }

    private void showProgress() {
        Log.d(TAG, "showProgress: ");
        refreshLayout.setRefreshing(true);
    }

    private void hideProgress() {
        Log.d(TAG, "hideProgress: ");
        refreshLayout.setRefreshing(false);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (tasks != null) {
            localStorage.putData("tasks", tasks);
        }
    }
    
    private FragmentActivity getActivityNonNull() {
        if (super.getActivity() != null) {
            return super.getActivity();
        } else {
            throw new RuntimeException("null returned from getActivity()");
        }
    }

    @Override
    public void OnItemClicked(GeoTextTask task) {
        Log.d(TAG, "OnItemClicked: " + task.getTitle());
        taskItemSelectedCallback.onTaskItemSelected(task);
    }

    public void setOnTaskItemSelectedListener(OnTaskItemSelectedListener listener) {
        Log.d(TAG, "setOnTaskItemSelectedListener");
        this.taskItemSelectedCallback = listener;
    }
}
