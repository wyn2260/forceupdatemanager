package com.qh.foreupdatemanager.pop;




import static com.qh.foreupdatemanager.tools.UpdateApkTool.cancelDownLoad;
import static com.qh.foreupdatemanager.tools.UpdateApkTool.installApk;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.qh.forceupdatemanager.databinding.PopCustomizedUpdateAppBinding;
import com.qh.foreupdatemanager.tools.UpdateApkTool;

import java.io.File;
public class CustomizedUpdateAppPop extends DialogFragment {
    private PopCustomizedUpdateAppBinding binding;
    private String appName;
    private static String apkPath = "";
    private int isUpdate = 0 ;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PopCustomizedUpdateAppBinding.inflate(inflater,container,false);
        try {
            binding.ivIcon.setImageDrawable(
                    requireContext().getPackageManager()
                            .getApplicationIcon(requireContext().getPackageName())
            );
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        binding.tvAppName.setText(appName);
        setCancelable(false);
        onclick();
        binding.progressBar.setProgress(0);
        binding.tvProgress.setText("0%");
        if(isUpdate == 1 ){
            binding.btCancel.setEnabled(false);
        }
        return binding.getRoot();
    }
    private void onclick() {
        binding.btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelDownLoad();
                dismiss();
            }
        });
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public static CustomizedUpdateAppPop newInstance(){
        CustomizedUpdateAppPop popFragment = new CustomizedUpdateAppPop();
        Bundle args = new Bundle();
        popFragment.setArguments(args);
        return popFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null ) {
            appName = getArguments().getString("appName");
            apkPath = getArguments().getString("apk");
            isUpdate = getArguments().getInt("isUpdate");
        }
        downLoadApp();
   }
    private void downLoadApp() {
        File file = new File(getContext().getExternalFilesDir(null),"update.apk");
        if (file.exists()) {
            boolean deleted = file.delete();
        }

        UpdateApkTool.downLoadApkFromUrl(apkPath, file, new UpdateApkTool.ProgressListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgress(int percent) {
                new Handler(Looper.getMainLooper()).post(()->{
                   binding.progressBar.setProgress(percent);
                   binding.tvProgress.setText(percent+"%");
                });
            }
            @Override
            public void onCompleted(File file) {
                new Handler(Looper.getMainLooper()).post(()->{
                    dismiss();
                    installApk(getContext(),file);
                });
            }
            @Override
            public void onError(Exception e) {
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() != null && getDialog().getWindow() != null ) {
            // 设置透明
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // 设置弹窗大小
            WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            boolean isH = width > height;
            if(isH){
                params.width = (int) (getResources().getDisplayMetrics().heightPixels *0.6);
                params.height = (int) (getResources().getDisplayMetrics().widthPixels * 0.2);
            }
            else {
                params.height = (int) (getResources().getDisplayMetrics().heightPixels *0.2);
                params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.6);
            }
            getDialog().getWindow().setAttributes(params);
        }
    }
}
