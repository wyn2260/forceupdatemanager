package com.qh.foreupdatemanager.service;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import com.qh.foreupdatemanager.entity.UpdateInfo;
import com.qh.foreupdatemanager.service.api.UpdateAPI;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UpdateHexRepository {
    private final UpdateAPI updateApi;
    public UpdateHexRepository(){
        updateApi = RetrofitClient.getInstance().create(UpdateAPI.class);
    }
    public LiveData<List<UpdateInfo>> fetchProtocolList(String url ){
        MutableLiveData<List<UpdateInfo>> liveData = new MutableLiveData<>();
        updateApi.getAppConfig(url).enqueue(new Callback<List<UpdateInfo>>() {
            @Override
            public void onResponse(Call<List<UpdateInfo>> call, Response<List<UpdateInfo>> response) {
                if(response.isSuccessful() && response.body() != null ) {
                    List<UpdateInfo> result = response.body();
                    for (UpdateInfo info : result) {
                        Log.d("UpdateRepo", "Fetched: " +
                                info.getVersion() + " | " +
                                info.getDownloadUri() + " | " +
                                info.getUpdate());
                    }
                    liveData.postValue(result);
                }
                else {
                    Log.e("UpdateRepo : " ,"Response Error : code = " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<UpdateInfo>> call, Throwable t) {
                Log.e("UpdateRepo  " , "Failed to fetch update info " ,t);
            }
        });
        return liveData;
    }
}