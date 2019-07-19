package com.nathpath.practice;

import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.nathpath.practice.base.BaseDialogFragment;

public class LoadingFragment extends BaseDialogFragment{

    @Override
    protected Dialog createDialog() {
        Dialog dialog = new Dialog(context);

        View view = View.inflate(context, getLayoutResource(), null);
        Window window = dialog.getWindow();

        dialog.setContentView(view);
        dialog.setCancelable(false);


        if(window != null){
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(R.color.transparent);
        }

        return dialog;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_loading;
    }

    @Override
    protected void initView(View view) {

    }

    @Override
    protected void event() {

    }
}
