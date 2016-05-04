package com.apricot.girl.util;

import android.content.Context;
import android.graphics.Bitmap;

import com.apricot.girl.dao.GirlDAO;
import com.apricot.girl.model.Girl;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.RealmObject;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by Apricot on 2016/4/26.
 */
public class MyRetrofit {
    private Retrofit retrofit;
    private GankApi gankApi;
    private Context mContext;
    public static final int REQUEST_GIRL_COUNT=10;

    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setExclusionStrategies(new ExclusionStrategy() {   //把RealmObject排除在外,不然会报错。
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getDeclaringClass().equals(RealmObject.class);
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            })
            .create();

    public MyRetrofit(Context context){
        mContext=context;
        OkHttpClient client=new OkHttpClient();
        client.setReadTimeout(12, TimeUnit.SECONDS);
        retrofit=new Retrofit.Builder()
                .baseUrl("http://gank.io/api/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        gankApi=retrofit.create(GankApi.class);
    }

    public List<Girl> getLatest(int page){
        GankApi.Result<List<Girl>> result=null;

        try {
            result=gankApi.latest(REQUEST_GIRL_COUNT, page).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(result==null||result.error){
            return null;
        }else{
            Bitmap bitmap=null;
            for(Girl girl:result.results){
                try{
                    bitmap= Glide.with(mContext)
                            .load(girl.getUrl())
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get();
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(bitmap!=null){
                    girl.setWidth(bitmap.getWidth());
                    girl.setHeight(bitmap.getHeight());
                }else{
                    girl.setWidth(0);
                    girl.setHeight(0);
                }
            }
        }
        GirlDAO.bulkInsert(result.results);
        return result.results;
    }
}
