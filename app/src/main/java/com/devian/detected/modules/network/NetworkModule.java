package com.devian.detected.modules.network;

import com.devian.detected.BuildConfig;
import com.devian.detected.model.domain.network.ServerResponse;
import com.devian.detected.modules.security.SecurityModule;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkModule {

    private static final boolean encryptionEnabled = true;

    private static NetworkModule mInstance;
    
    private static final String BASE_URL = BuildConfig.SERVER_BASE_URL;

    private Retrofit mRetrofit;

    private NetworkModule() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient okHttpClient = builder.build();

        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
    }

    public static NetworkModule getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkModule();
        }
        return mInstance;
    }

    public NetworkInterface getApi() {
        return mRetrofit.create(NetworkInterface.class);
    }

    public Map<String, String> proceedHeader(String data) {
        Map<String, String> headers = new HashMap<>();
        if (encryptionEnabled) {
            headers.put("data", SecurityModule.encrypt(data));
        } else {
            headers.put("data", data);
        }
        return headers;
    }
    
    public Map<String, String> proceedHeaders(HashMap<String, String> data) {
        Map<String, String> headers = new HashMap<>();
        if (encryptionEnabled) {
            for (Map.Entry<String, String> stringStringEntry : data.entrySet()) {
                headers.put(
                        ((Map.Entry) stringStringEntry).getKey().toString(),
                        SecurityModule.encrypt(((Map.Entry) stringStringEntry).getValue().toString()));
            }
        } else {
            for (Map.Entry<String, String> stringStringEntry : data.entrySet()) {
                headers.put(
                        ((Map.Entry) stringStringEntry).getKey().toString(),
                        ((Map.Entry) stringStringEntry).getValue().toString());
            }
        }
        return headers;
    }

    public String proceedResponse(ServerResponse response) {
        if (encryptionEnabled) {
            return SecurityModule.decrypt(response.getData());
        } else {
            return response.getData();
        }
    }

    public String proceedResponse(String data) {
        if (encryptionEnabled) {
            return SecurityModule.decrypt(data);
        } else {
            return data;
        }
    }
}
