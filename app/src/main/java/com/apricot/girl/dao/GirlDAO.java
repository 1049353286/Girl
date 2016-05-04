package com.apricot.girl.dao;

import com.apricot.girl.model.Girl;

import java.util.List;

import io.realm.Realm;

/**
 * Created by Apricot on 2016/4/26.
 */
public class GirlDAO {
    static Realm realm;

    public static void bulkInsert(List<Girl> girls){
        realm=Realm.getDefaultInstance();
        realm.beginTransaction();
        for(Girl girl:girls){
            realm.copyToRealmOrUpdate(girl);
        }
        realm.commitTransaction();
        if(realm!=null){
            realm.close();
        }
    }
}
