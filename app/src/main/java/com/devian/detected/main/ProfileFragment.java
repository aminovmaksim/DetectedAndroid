package com.devian.detected.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.devian.detected.MainActivity;
import com.devian.detected.R;
import com.devian.detected.utils.LevelManager;
import com.devian.detected.utils.Network.NetworkService;
import com.devian.detected.utils.Network.ServerResponse;
import com.devian.detected.utils.domain.RankRow;
import com.devian.detected.utils.domain.UserStats;
import com.devian.detected.utils.security.AES256;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    
    private static final String TAG = "ProfileFragment";
    
    private FirebaseAuth mAuth;
    
    private Gson gson = new Gson();
    
    @BindView(R.id.profile_tvName)
    TextView tvName;
    
    @BindView(R.id.profile_tvLevel)
    TextView tvLevel;
    @BindView(R.id.profile_tvScannedTags)
    TextView tvScannedTags;
    @BindView(R.id.profile_tvRating)
    TextView tvRating;
    @BindView(R.id.profile_tvListTop10)
    TextView tvListTop10;
    @BindView(R.id.profile_progressLevel)
    ProgressBar progressLevel;
    
    private FloatingActionButton fab_settings, fab_exit, fab_edit;
    private TextView tv_fab_exit, tv_fab_edit;
    private Animation fab_open, fab_close, fab_clock, fab_anticlock;
    private Boolean fab_settings_open = false;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, v);
        mAuth = FirebaseAuth.getInstance();
        init();
        init_fab(v);
        
        return v;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        updateStatistics();
        updateRankings();
    }
    
    private void init() {
        tvName.setText(mAuth.getCurrentUser().getDisplayName());
    }
    
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
    
        }
    }
    
    void updateUI(UserStats userStats, List<RankRow> top10, RankRow selfRank) {
        Log.d(TAG, "updateUI: " + gson.toJson(userStats));
        if (userStats != null) {
            tvLevel.setText(String.valueOf(userStats.getLevel()));
            tvScannedTags.setText(String.valueOf(userStats.getTags()));
            progressLevel.setProgress(LevelManager.getPercentsCompleted(userStats.getPoints()));
        }
        if (top10 != null) {
            String text = String.valueOf(gson.toJson(top10));
            tvListTop10.setText(text);
        }
        if (selfRank != null) {
            String rank = String.valueOf(selfRank.getRank());
            tvRating.setText(rank);
        }
    }
    
    private void updateStatistics() {
        Map<String, String> headers = new HashMap<>();
        headers.put("data", AES256.encrypt(mAuth.getUid()));
        Log.d(TAG, "updateStatistics: " + AES256.encrypt(mAuth.getUid()));
        NetworkService.getInstance().getApi().getStats(headers).enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                Log.d(TAG, "onResponse: " + gson.toJson(response.body()));
                if (response.body() == null)
                    return;
                try {
                    if (response.body().getType() == ServerResponse.TYPE_STATS_EXISTS) {
                        UserStats userStats = gson.fromJson(response.body().getData(), UserStats.class);
                        updateUI(userStats, null, null);
                    } else {
                        Log.e(TAG, "onResponse: user stats does not exist on the server");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
    
    void updateRankings() {
        Log.d(TAG, "updateRankings");
        // Get top 10 users
        NetworkService.getInstance().getApi().getRankTop10().enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.body() == null)
                    return;
                try {
                    if (response.body().getType() == ServerResponse.TYPE_RANK_SUCCESS) {
                        Type listType = new TypeToken<ArrayList<RankRow>>() {
                        }.getType();
                        List<RankRow> top10 = gson.fromJson(response.body().getData(), listType);
                        updateUI(null, top10, null);
                    } else
                        Log.e(TAG, "onResponse (top10): type != TYPE_RANK_SUCCESS");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            
            }
            
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    
        // Get personal rank
        Map<String, String> headers = new HashMap<>();
        headers.put("data", AES256.encrypt(mAuth.getUid()));
        NetworkService.getInstance().getApi().getPersonalRank(headers).enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.body() == null)
                    return;
                try {
                    if (response.body().getType() == ServerResponse.TYPE_RANK_SUCCESS) {
                        RankRow rankRow = gson.fromJson(response.body().getData(), RankRow.class);
                        updateUI(null, null, rankRow);
                    } else
                        Log.e(TAG, "onResponse (personal rank): type != TYPE_RANK_SUCCESS");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        
            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
    
    void logout() {
        // Firebase sign out
        mAuth.signOut();
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(getActivity(),
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                    }
                });
    }
    
    private void init_fab(View v) {
        fab_settings = v.findViewById(R.id.fab_settings);
        fab_exit = v.findViewById(R.id.fab_exit);
        fab_edit = v.findViewById(R.id.fab_edit);
        
        fab_close = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        fab_open = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fab_clock = AnimationUtils.loadAnimation(getContext(), R.anim.fab_rotate_clock);
        fab_anticlock = AnimationUtils.loadAnimation(getContext(), R.anim.fab_rotate_anticlock);
        
        tv_fab_exit = v.findViewById(R.id.profile_tv_fab_exit);
        tv_fab_edit = v.findViewById(R.id.profile_tv_fab_edit);
        
        fab_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fab_settings_open) {
                    fab_exit.startAnimation(fab_close);
                    fab_edit.startAnimation(fab_close);
                    fab_settings.startAnimation(fab_anticlock);
                    
                    fab_exit.setClickable(false);
                    fab_edit.setClickable(false);
                    
                    tv_fab_exit.setVisibility(View.INVISIBLE);
                    tv_fab_edit.setVisibility(View.INVISIBLE);
                    tv_fab_exit.startAnimation(fab_close);
                    tv_fab_edit.startAnimation(fab_close);
                    
                    fab_settings_open = false;
                } else {
                    fab_exit.startAnimation(fab_open);
                    fab_edit.startAnimation(fab_open);
                    fab_settings.startAnimation(fab_clock);
                    
                    fab_exit.setClickable(true);
                    fab_edit.setClickable(true);
                    
                    tv_fab_exit.setVisibility(View.VISIBLE);
                    tv_fab_edit.setVisibility(View.VISIBLE);
                    tv_fab_exit.startAnimation(fab_open);
                    tv_fab_edit.startAnimation(fab_open);
                    
                    fab_settings_open = true;
                }
            }
        });
        
        fab_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Выход из аккаунта")
                        .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                logout();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        
        fab_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            
            }
        });
    }
}
