package com.example.myapplication;

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
    Call<ResponseBody> addFingerprint(@Body FingerPrintPost fingerPrintPost);

    @GET("/map/get/{floor_no}")
    Call<String> getFloorMap(@Path("floor_no") int i);

    @GET("/localization/mac/get")
    Call<List<Mac>> getMacs();

    @GET("/localization/poi/get")
    Call<List<PointOfInterest>> getPOIs();

    @GET("/route/get/{poi_id}")
    Call<ResponseBody> getRoute(@Path("poi_id") int i, @Query("latitude") double d, @Query("longitude") double d2, @Query("floor") int i2);
}
