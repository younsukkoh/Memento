package younsuk.memento.phasei.pause;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.io.File;

/**
 * Created by Younsuk on 11/9/2015.
 */
public class MementoPreviewActivity extends SingleFragmentActivity {

    public static final String EXTRA_VIDEO_FILE_MPA = "extra_video_file_mpa";

    /** Get video file from recorder activity */
    public static Intent newIntent(Context context, File file){
        Intent intent = new Intent(context, MementoPreviewActivity.class);
        intent.putExtra(EXTRA_VIDEO_FILE_MPA, file);
        return intent;
    }

    @Override
    protected Fragment createFragment(){
        return new MementoPreviewFragment();
    }

}
