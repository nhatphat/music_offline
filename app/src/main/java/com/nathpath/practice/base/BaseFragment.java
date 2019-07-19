package com.nathpath.practice.base;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nathpath.practice.LoadingFragment;

public abstract class BaseFragment extends Fragment{
    private LoadingFragment loadingFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingFragment = new LoadingFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResource(), container, false);
        return view;
    }

    protected abstract @LayoutRes int getLayoutResource();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        OnViewCreated(view);
        event();
    }

    protected abstract void OnViewCreated(View view);

    protected abstract void event();

    public void showLoading(boolean isShow){
        if(isShow && !loadingFragment.isAdded()){
            loadingFragment.show(getChildFragmentManager(), "loading");
        }else{
            if(!getChildFragmentManager().isStateSaved() && loadingFragment.getShowsDialog()){
                loadingFragment.dismiss();
            }
        }
    }

    protected void showSettingsDialog() {
        if(getContext() == null){
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Cần quyền truy cập bộ nhớ");
        builder.setMessage("Vui lòng cấp quyền mới xài được app nha...");
        builder.setPositiveButton("GOTO SETTINGS", (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    protected void openSettings() {
        if(getContext() == null){
            return;
        }
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    public void toast(String mess){
        Toast toast = Toast.makeText(getContext(), mess, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
