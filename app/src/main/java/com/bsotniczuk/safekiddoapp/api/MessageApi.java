package com.bsotniczuk.safekiddoapp.api;

import com.bsotniczuk.safekiddoapp.datamodel.JsonMessage;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MessageApi {

    //https://run.mocky.io/v3/6125f2d0-0688-4547-aae8-0295d984f196

    @GET("v3/6125f2d0-0688-4547-aae8-0295d984f196")
    Call<JsonMessage> getMessages();
}
