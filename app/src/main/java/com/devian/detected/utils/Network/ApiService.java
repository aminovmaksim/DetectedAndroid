package com.devian.detected.utils.Network;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface ApiService {
    @GET("/testConnection")
    Call<ServerResponse> testConnection();
    
    @GET("/auth")
    Call<ServerResponse> auth(@HeaderMap Map<String, String> headers);
    
    @GET("/getUserInfo")
    Call<ServerResponse> getUserInfo(@HeaderMap Map<String, String> headers);
    
    @GET("/getStats")
    Call<ServerResponse> getStats(@HeaderMap Map<String, String> headers);
    
    @GET("/getMapTasks")
    Call<ServerResponse> getMapTasks();
    
    @GET("/getTextTasks")
    Call<ServerResponse> getTextTasks();
    
    @POST("/scanTag")
    Call<ServerResponse> scanTag(@HeaderMap Map<String, String> headers);
    
    @GET("/getRankTop10")
    Call<ServerResponse> getRankTop10();
    
    @GET("/getPersonalRank")
    Call<ServerResponse> getPersonalRank(@HeaderMap Map<String, String> headers);
    
    @GET("/getEvent")
    Call<ServerResponse> getEvent();
}
