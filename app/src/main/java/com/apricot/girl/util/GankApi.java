package com.apricot.girl.util;

import com.apricot.girl.model.Girl;

import java.util.List;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by Apricot on 2016/4/26.
 */
public interface GankApi {
    @GET("data/%E7%A6%8F%E5%88%A9/{count}/{page}")
    Call<Result<List<Girl>>> latest(@Path("count") int count,@Path("page") int page);

    class Result<T> {
        public boolean error;
        public T results;
    }
}
