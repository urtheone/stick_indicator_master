package com.urtheone.stick_indicator_master;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.urtheone.stickindicator.StickIndicator;

public class MainActivity extends AppCompatActivity {

    private SimpleFragmentAdapter mSimpleFragmentAdapter;
    private ViewPager mViewPager;
    private StickIndicator mStickIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStickIndicator = (StickIndicator) findViewById(R.id.indicator);
        mSimpleFragmentAdapter = new SimpleFragmentAdapter(getSupportFragmentManager());

        mSimpleFragmentAdapter.addFragment(SimpleFragment.newInstance(R.color.colorBlue));
        mSimpleFragmentAdapter.addFragment(SimpleFragment.newInstance(R.color.colorYellow));
        mSimpleFragmentAdapter.addFragment(SimpleFragment.newInstance(R.color.colorGreen));

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSimpleFragmentAdapter);
        mStickIndicator.setViewPager(mViewPager);

    }
}
