package younsuk.memento.phasei.pause;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by Younsuk on 11/4/2015.
 */
public class MementoRecorderActivity extends SingleFragmentActivity {

    /**  */
    public static Intent newIntent(Context context){
        Intent intent = new Intent(context, MementoRecorderActivity.class);
        return intent;
    }

    @Override
    protected Fragment createFragment(){
        return new MementoRecorderFragment();
    }


}
