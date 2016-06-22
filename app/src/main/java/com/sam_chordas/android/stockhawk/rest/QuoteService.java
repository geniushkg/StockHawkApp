package com.sam_chordas.android.stockhawk.rest;


import com.sam_chordas.android.stockhawk.entity.Result;
import com.sam_chordas.android.stockhawk.entity.Results;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by geniushkg on 6/21/2016.
 */
public interface QuoteService {

    @GET("/v1/public/yql")
    Call<Result> getHistoricalData(@Query("q") String q, @Query("diagnostics") String diagnostics,
                           @Query("env") String env, @Query("format") String format
    );

    @GET("/v1/public/yql")
    Call<Result> getHistoricalData(@Query("q") String q );
}
