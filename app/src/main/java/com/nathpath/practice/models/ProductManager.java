package com.nathpath.practice.models;

import io.reactivex.Observable;

import java.util.List;

import retrofit2.http.GET;

public interface ProductManager {
    @GET("/menu")
    Observable<List<Product>> getAllProduct();
}
