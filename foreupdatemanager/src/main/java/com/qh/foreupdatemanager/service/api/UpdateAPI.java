package com.qh.foreupdatemanager.service.api;


import com.qh.foreupdatemanager.entity.UpdateInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface UpdateAPI {
    @GET
    Call<List<UpdateInfo>> getAppConfig(@Url String url);
}
