package com.apricot.girl;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Apricot on 2016/4/26.
 */
public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder(this)
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build());
    }
}
