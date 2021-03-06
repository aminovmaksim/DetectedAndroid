package com.devian.detected.model.domain.network;

import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class ServerResponse {

    private int type;
    private String data;

    public static final int TYPE_DEFAULT = 0;

    public static final int TYPE_ERROR = -1;

    public static final int TYPE_AUTH_SUCCESS = 10;

    public static final int TYPE_STATS_EXISTS = 20;

    public static final int TYPE_TASK_SUCCESS = 30;
    public static final int TYPE_TASK_ADDED = 31;
    public static final int TYPE_TASK_COMPLETED = 32;

    public static final int TYPE_RANK_SUCCESS = 40;

    public static final int TYPE_CHANGE_NICKNAME_SUCCESS = 50;
    
    public static final int TYPE_ADD_TAG_SUCCESS = 100;
    
    public static final int TYPE_AUTH_FAILURE = -10;

    public static final int TYPE_STATS_DOES_NOT_EXIST = -20;

    public static final int TYPE_TASK_FAILURE = -30;
    public static final int TYPE_TASK_ALREADY_COMPLETED = -32;

    public static final int TYPE_RANK_FAILURE = -40;

    public static final int TYPE_CHANGE_NICKNAME_FAILURE = -50;
    public static final int TYPE_CHANGE_NICKNAME_EXISTS = -51;
    
    public static final int TYPE_ADD_TAG_FAILURE = -100;
    public static final int TYPE_ADD_TAG_ADMIN_FAILURE = -101;
}
