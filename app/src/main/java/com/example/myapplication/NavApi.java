package com.example.myapplication;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NavApi {
    @GET("/map/get/{floor_no}")
    Call<String> getFloorMap(@Path("floor_no") int floor_no);
    @GET("/localization/poi/get")
    Call<List<PointOfInterest>> getPOIs(@Query("floor_no") int floor_no);

}
