package com.qh.foreupdatemanager.pop;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.qh.forceupdatemanager.databinding.PopUpdateInfoBinding;


public class UpdateInfoAppPop extends DialogFragment {
    private PopUpdateInfoBinding binding;
    private String  oldVersion ;
    private String newVersion;
    private String upInfo;
    private UpdateInfoInter updateInfoInter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PopUpdateInfoBinding.inflate(inflater,container,false);
        try {
            binding.ivIcon.setImageDrawable(
                    requireContext().getPackageManager()
                            .getApplicationIcon(requireContext().getPackageName())
            );
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        binding.tvNewVersion.setText(newVersion);
        binding.tvOldVersion.setText(oldVersion);
        binding.tvContent.setText(upInfo);
        setCancelable(false);
        onclick();
        return binding.getRoot();
    }
    public static UpdateInfoAppPop newInstance(){
        UpdateInfoAppPop popFragment = new UpdateInfoAppPop();
        Bundle args = new Bundle();
        popFragment.setArguments(args);
        return popFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null ) {
            oldVersion = getArguments().getString("oldVersion");
            newVersion = getArguments().getString("newVersion");
            upInfo = getArguments().getString("upInfo");
        }
    }

    public UpdateInfoInter getUpdateInfoInter() {
        return updateInfoInter;
    }

    public void setUpdateInfoInter(UpdateInfoInter updateInfoInter) {
        this.updateInfoInter = updateInfoInter;
    }

    private void onclick(){
        binding.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateInfoInter.update();
            }
        });
        binding.btnIgnore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateInfoInter.close();
                dismiss();
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
                params.width = (int) (getResources().getDisplayMetrics().heightPixels *0.4);
                params.height = (int) (getResources().getDisplayMetrics().widthPixels * 0.2);
            }
            else {
                params.height = (int) (getResources().getDisplayMetrics().heightPixels *0.2);
                params.width= (int) (getResources().getDisplayMetrics().widthPixels * 0.4);
            }
            getDialog().getWindow().setAttributes(params);
        }
    }

    public interface  UpdateInfoInter{
        void update();
        void close();
    }
}
