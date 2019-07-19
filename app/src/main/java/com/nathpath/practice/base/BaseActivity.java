package com.nathpath.practice.base;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.Toast;

import com.nathpath.practice.utils.CompositeDisposableManager;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());

        initView();
        initData();
        event();
    }
    protected abstract @LayoutRes int getLayoutResource();

    protected abstract void initView();

    protected abstract void initData();

    protected abstract void event();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CompositeDisposableManager.disposeDisposable();
    }

    public void addFragmentInto(@IdRes int container, BaseFragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        BaseFragment oldFragment = getCurrentFragmentOn(container);
        if (oldFragment != null) {
            transaction.replace(container, fragment);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(null);
        } else {
            transaction.add(container, fragment);
        }
        transaction.commit();
    }

    protected BaseFragment getCurrentFragmentOn(@IdRes int container) {
        return (BaseFragment) getSupportFragmentManager().findFragmentById(container);
    }

    public void toast(String mess){
        Toast toast = Toast.makeText(this, mess, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
