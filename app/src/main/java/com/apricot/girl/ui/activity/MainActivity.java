package com.apricot.girl.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.apricot.girl.R;
import com.apricot.girl.model.Girl;
import com.apricot.girl.ui.adapter.MyRecyclerAdapter;
import com.apricot.girl.ui.adapter.onGirlClickListener;
import com.apricot.girl.util.MyRetrofit;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,onGirlClickListener {
    public static final String TAG="MainActivity";
    public static final int MERGE_LIST=1;
    private static int page=2;
    private List<Girl> girls=new ArrayList<Girl>();

    @Bind(R.id.rv_main)
    RecyclerView mainRecyclerView;

    @Bind(R.id.sw_main)
    SwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private MyRetrofit retrofit;
    private Realm realm;
    private MyRecyclerAdapter myRecyclerAdapter;
    private StaggeredGridLayoutManager stagg;
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MERGE_LIST:
                    List<Girl> netgirls= (List<Girl>) msg.obj;
                    if(girls.size()==0){
                        girls.addAll(netgirls);    //addall adapter才能notify   girls=netgirls adapter can't notify
                        Log.d(TAG,"the first time start");
                    }else{
                        mergeList(girls,netgirls);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        swipeRefreshLayout.setOnRefreshListener(this);
        initRealm();
        initRecyclerView();
        retrofit=new MyRetrofit(getApplicationContext());
        if(realm.where(Girl.class).findAll().size()>0){
            Log.d(TAG,"load from db");
            LoadFormDB();
        }else{
            Log.d(TAG, "load from service");
            LoadFromService(LoadImageAsyncTask.GET_LATEST);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.removeAllChangeListeners();
        realm.close();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initRealm(){
        realm=Realm.getDefaultInstance();
        RealmChangeListener realmListener=new RealmChangeListener() {
            @Override
            public void onChange() {
                Toast.makeText(MainActivity.this,"realm changed",Toast.LENGTH_SHORT).show();
            }
        };
        realm.addChangeListener(realmListener);
    }

    public void initRecyclerView(){
        stagg=new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        myRecyclerAdapter =new MyRecyclerAdapter(getApplicationContext(), girls);
        myRecyclerAdapter.setOnGirlClickListener(this);
        mainRecyclerView.setAdapter(myRecyclerAdapter);
        mainRecyclerView.setLayoutManager(stagg);
        mainRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int[] lastVisibleItem = stagg.findLastCompletelyVisibleItemPositions(null);
                    int position = Math.max(lastVisibleItem[0], lastVisibleItem[1]);
                    if (position + 1 == myRecyclerAdapter.getItemCount()) {
                        LoadFromService(LoadImageAsyncTask.GET_MORE);
                    }
                }
            }
        });
    }

    private void LoadFormDB(){
        RealmResults<Girl> results=realm.where(Girl.class)
                .findAllSorted("publishedAt",false);
        girls.clear();
        girls.addAll(results);
        myRecyclerAdapter.notifyDataSetChanged();
    }

    private void LoadFromService(int i){
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        new LoadImageAsyncTask().execute(i);
    }

    private void mergeList(List<Girl> girls,List<Girl> netgirls){ //Realm access from incorrect thread. Realm objects can only be accessed on the thread they were created.
        Girl lastNetGirl=netgirls.get(netgirls.size()-1);
        for(int i=0;i<girls.size();i++){
            if(lastNetGirl.getPublishedAt().equals(girls.get(i).getPublishedAt())&&i!=girls.size()-1){   //在子线程中 getPublishedAt 出错
                netgirls.addAll(girls.subList(i+1,girls.size()-1));
                girls.addAll(netgirls);
                Log.d(TAG,"mergeList successful");
            }
        }
    }

    @Override
    public void onRefresh() {
        LoadFromService(LoadImageAsyncTask.GET_LATEST);
    }

    @Override
    public void onGirlClick(View view, int position) {
        ArrayList<String> girlsUrl=new ArrayList<>();
        Intent intent=new Intent(this,DetailActivity.class);
        for(Girl girl:girls){
            girlsUrl.add(girl.getUrl());
        }
        intent.putStringArrayListExtra("girlsUrl",girlsUrl);
        intent.putExtra("index",position);
        startActivity(intent);
    }

    class LoadImageAsyncTask extends AsyncTask<Integer,Void,Integer>{
        public static final int GET_LATEST = 1;
        public static final int GET_MORE = 2;
        @Override
        protected Integer doInBackground(Integer... params) {
            switch (params[0]){
                case GET_LATEST: {
                    List<Girl> temp = retrofit.getLatest(1);
                    if (temp != null) {
                        Message message=new Message(); //在主线程中合并List
                        message.what=MERGE_LIST;
                        message.obj=temp;
                        mHandler.sendMessage(message);
                        return GET_LATEST;
                    } else {
                        return -1;
                    }
                }

                case GET_MORE: {
                    List<Girl> temp = retrofit.getLatest(page);
                    if (temp != null) {
                        page++;
                        girls.addAll(temp);
                        Log.d(TAG,"get more");
                        return GET_MORE;
                    } else {
                        return -1;
                    }
                }
                default:return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if(swipeRefreshLayout.isRefreshing()){
                swipeRefreshLayout.setRefreshing(false);
            }
            myRecyclerAdapter.notifyDataSetChanged();
        }
    }
}
