package com.example.myapplication;

import org.json.JSONObject;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NavApi {
    @POST("/localization/fp/post")
    Call<ResponseBody> addFingerprint(@Body FingerprintPost fingerprintPost);
}

