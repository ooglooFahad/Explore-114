package oogloo.com.explore114;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.WindowManager;

public class TabsActivity extends AppCompatActivity {
    static ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    int Numboftabs = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_tabs);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.mylogoicon1);
        SpannableString s = new SpannableString("   EXPLORE 114");
        s.setSpan(new TypefaceSpan(this, "ALGER.TTF"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);

        SpannableString surah = new SpannableString("SURAH");
        surah.setSpan(new TypefaceSpan(this, "Bariol_Regular.otf"), 0, surah.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableString parah = new SpannableString("PARAH");
        parah.setSpan(new TypefaceSpan(this, "Bariol_Regular.otf"), 0, parah.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        CharSequence Titles[] = {parah, surah};
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), Titles, Numboftabs);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true);
        tabs.setSelectedIndicatorColors(R.color.tab_active);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        tabs.setViewPager(pager);
    }
}
