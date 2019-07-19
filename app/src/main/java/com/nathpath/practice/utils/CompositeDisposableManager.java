package com.nathpath.practice.utils;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class CompositeDisposableManager {

    private static CompositeDisposableManager instance;
    private CompositeDisposable compositeDisposable;

    private CompositeDisposableManager(){
        compositeDisposable = new CompositeDisposable();
    }

    private static CompositeDisposableManager getInstance(){
        if(instance == null){
            instance = new CompositeDisposableManager();
        }

        return instance;
    }

    public static void addDisposable(Disposable disposable){
        getInstance().compositeDisposable.add(disposable);
    }

    public static void disposeDisposable(){
        if(!getInstance().compositeDisposable.isDisposed()){
            getInstance().compositeDisposable.dispose();
            instance = null;
        }
    }
}
