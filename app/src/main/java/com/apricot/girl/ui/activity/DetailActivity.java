package com.apricot.girl.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.apricot.girl.R;
import com.apricot.girl.ui.fragment.DetailFragment;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Apricot on 2016/5/3.
 */
public class DetailActivity extends BaseActivity{
    @Bind(R.id.view_pager)
    ViewPager viewPager;
    private DetailPagerAdapter pagerAdapter;
    private List<String> girlsUrl;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        girlsUrl=getIntent().getStringArrayListExtra("girlsUrl");
        index=getIntent().getIntExtra("index", 0);
        pagerAdapter=new DetailPagerAdapter();
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(index);
    }

    class DetailPagerAdapter extends FragmentStatePagerAdapter{


        public DetailPagerAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            return DetailFragment.newInstance(girlsUrl.get(position));
        }

        @Override
        public int getCount() {
            return girlsUrl.size();
        }


    }
    
}
