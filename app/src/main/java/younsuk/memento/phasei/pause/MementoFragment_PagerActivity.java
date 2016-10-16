package younsuk.memento.phasei.pause;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.util.List;

/**
 * Created by Younsuk on 11/12/2015.
 */
public class MementoFragment_PagerActivity extends AppCompatActivity {

    private static final String TAG = "MementoFragmentPagerAct";
    private static final String EXTRA_MEMENTO_FILE = "extra_memento_file";
    private static final String EXTRA_MEMENTO_LOCATION = "extra_memento_location";

    private ViewPager mViewPager;
    private List<Memento> mMementos;

    /** Receive video file from other class */
    public static Intent newIntent(Context context, File file){
        Intent intent = new Intent(context, MementoFragment_PagerActivity.class);
        intent.putExtra(EXTRA_MEMENTO_FILE, file);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memento_fragment_pager_activity);

        mMementos = MementoLab.get(this).getMementos();
        FragmentManager fragmentManager = getSupportFragmentManager();

        mViewPager = (ViewPager)findViewById(R.id.memento_fragment_pager_activity_viewPager);
        mViewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Memento memento = mMementos.get(position);
                return MementoFragment.newInstance(memento.getFile());
            }

            @Override
            public int getCount() {
                return mMementos.size();
            }
        });

        //By default view pager shows the first item in pager adapter, so this fixes that
        File file = (File)getIntent().getSerializableExtra(EXTRA_MEMENTO_FILE);
        for (int i = 0; i < mMementos.size(); i ++){
            if (mMementos.get(i).getFile().equals(file)){
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    public void finish(){
        Intent intent = MementoListActivity.newIntent(this);
        startActivity(intent);
    }
}
