package com.qh.foreupdatemanager;



import static com.qh.foreupdatemanager.tools.UpdateApkTool.compareVersion;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;


import com.qh.foreupdatemanager.entity.UpdateInfo;
import com.qh.foreupdatemanager.pop.CustomizedUpdateAppPop;
import com.qh.foreupdatemanager.pop.UpdateInfoAppPop;
import com.qh.foreupdatemanager.service.UpdateHexRepository;

import java.util.List;

public class ForceUpdateUtil {
    public static void ForceUpdatePackage(
            FragmentActivity activity,
            String url
    ) {
        UpdateHexRepository repository = new UpdateHexRepository();

        LiveData<List<UpdateInfo>> liveData = repository.fetchProtocolList(url);

        Observer<List<UpdateInfo>> observer = new Observer<List<UpdateInfo>>() {
            @Override
            public void onChanged(List<UpdateInfo> updateInfos) {

                liveData.removeObserver(this);

                if (updateInfos == null || updateInfos.isEmpty()) return;

                try {
                    String currentVersion = activity
                            .getPackageManager()
                            .getPackageInfo(activity.getPackageName(), 0)
                            .versionName;

                    UpdateInfo latestUpdate = null;

                    for (UpdateInfo info : updateInfos) {
                        if (Boolean.TRUE.equals(info.getUpdate())) {
                            if (latestUpdate == null ||
                                    compareVersion(info.getVersion(), latestUpdate.getVersion()) > 0) {
                                latestUpdate = info;
                            }
                        }
                    }

                    if (latestUpdate != null &&
                            compareVersion(latestUpdate.getVersion(), currentVersion) > 0) {

                        Log.e("Update", "发现新版本: " + latestUpdate.getVersion());

                        StringBuilder infoBuilder = new StringBuilder();
                        if (latestUpdate.getUpInfo() != null) {
                            for (String s : latestUpdate.getUpInfo()) {
                                infoBuilder.append(s).append("\n");
                            }
                        }

                        UpdateInfoAppPop pop = UpdateInfoAppPop.newInstance();

                        Bundle bundle = new Bundle();
                        bundle.putString("oldVersion", currentVersion);
                        bundle.putString("newVersion", latestUpdate.getVersion());
                        bundle.putString("upInfo", infoBuilder.toString());

                        pop.setArguments(bundle);

                        if (!activity.isFinishing() && !activity.isDestroyed()) {
                            pop.show(activity.getSupportFragmentManager(), "UpdateDialog");
                        }

                        UpdateInfo finalLatestUpdate = latestUpdate;

                        pop.setUpdateInfoInter(new UpdateInfoAppPop.UpdateInfoInter() {
                            @Override
                            public void update() {
                                pop.dismiss();
                                CustomizedUpdateAppPop pop2 = CustomizedUpdateAppPop.newInstance();
                                Bundle bundle2 = new Bundle();
                                bundle2.putString("apk", finalLatestUpdate.getDownloadUri());
                                bundle2.putString("appName", getAppName(activity.getApplicationContext()));
                                bundle2.putInt("isUpdate", 1);
                                pop2.setArguments(bundle2);
                                if (!activity.isFinishing() && !activity.isDestroyed()) {
                                    pop2.show(activity.getSupportFragmentManager(), "updateDialog");
                                }
                            }
                            @Override
                            public void close() {
                                // 可扩展：忽略更新逻辑
                            }
                        });
                    } else {
                        Log.e("Update", "当前已是最新版本");
                    }

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };

        liveData.observeForever(observer);
    }


    private static boolean isShowingUpdate = false; // ✅ 防止重复弹窗
    public static void UpdatePackage(
            FragmentActivity activity,
            String url
    ) {
        if (isShowingUpdate) return;
        UpdateHexRepository repository = new UpdateHexRepository();
        LiveData<List<UpdateInfo>> liveData = repository.fetchProtocolList(url);

        Observer<List<UpdateInfo>> observer = new Observer<List<UpdateInfo>>() {
            @Override
            public void onChanged(List<UpdateInfo> updateInfos) {

                liveData.removeObserver(this);

                if (updateInfos == null || updateInfos.isEmpty()) return;

                try {
                    String currentVersion = activity
                            .getPackageManager()
                            .getPackageInfo(activity.getPackageName(), 0)
                            .versionName;

                    UpdateInfo latestUpdate = null;

                    for (UpdateInfo info : updateInfos) {
                        if (Boolean.TRUE.equals(info.getUpdate())) {
                            if (latestUpdate == null ||
                                    compareVersion(info.getVersion(), latestUpdate.getVersion()) > 0) {
                                latestUpdate = info;
                            }
                        }
                    }

                    if (latestUpdate != null &&
                            compareVersion(latestUpdate.getVersion(), currentVersion) > 0) {

                        isShowingUpdate = true;
                        Log.e("Update", "发现新版本: " + latestUpdate.getVersion());
                        CustomizedUpdateAppPop pop = CustomizedUpdateAppPop.newInstance();
                        Bundle bundle = new Bundle();
                        bundle.putString("apk", latestUpdate.getDownloadUri());
                        bundle.putString("appName", getAppName(activity.getApplicationContext()));
                        bundle.putInt("isUpdate", 1);
                        pop.setArguments(bundle);
                        if (!activity.isFinishing() && !activity.isDestroyed()) {
                            pop.show(activity.getSupportFragmentManager(), "updateDialog");
                        }
                        pop.getDialog().setOnDismissListener(dialog -> {
                            isShowingUpdate = false;
                        });
                    } else {
                        Log.e("Update", "当前已是最新版本");
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };

        liveData.observeForever(observer);
    }
    public static String getAppName(Context context) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai = context.getApplicationInfo();
        return (String) pm.getApplicationLabel(ai);
    }
}
