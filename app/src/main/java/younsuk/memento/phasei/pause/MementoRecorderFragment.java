package younsuk.memento.phasei.pause;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Younsuk on 11/11/2015.
 */
public class MementoRecorderFragment extends Fragment {

    private static final String TAG = "MementoRecorderFragment";
    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MEDIA_TYPE_VIDEO = 2;
    private static final int CAMERA_FACING_BACK = Camera.CameraInfo.CAMERA_FACING_BACK;
    private static final int CAMERA_FACING_FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT;

    private Camera mCamera;
    private TextureView mTextureView;
    private MediaRecorder mMediaRecorder;
    private ImageButton mCaptureButton;
    private ImageButton mSwitchViewButton;
    private ImageButton mPlayButton;
    private ImageButton mListButton;
    private boolean mIsRecording = false;
    private SurfaceTexture mSurface;
    private File mVideoFile;
    private boolean mCameraFacingBack = true;
    //----------------------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.memento_recorder_fragment, container, false);

        mTextureView = (TextureView)view.findViewById(R.id.memento_recorder_fragment_textureView);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mSurface = surface;
                startPreview(mSurface, CAMERA_FACING_BACK);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) { }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                //If returns true, no rendering should happen inside the surface texture after this method is invoked.
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) { }
        });

        mCaptureButton = (ImageButton)view.findViewById(R.id.memento_recorder_fragment_button_capture);
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRecording) {
                    setButtonsVisibilities(true);

                    mMediaRecorder.stop();
                    releaseMediaRecorder();
                    mCamera.lock(); //take camera access back from MediaRecorder
                    mIsRecording = false;
                    releaseCamera();

                    reviewRecordedOutput();

                }//close isRecording == true
                else {
                    if (prepareVideoRecorder(getCameraView())) {
                        mMediaRecorder.start();
                        mIsRecording = true;
                        setButtonsVisibilities(false);
                    } else {
                        releaseMediaRecorder();
                        setButtonsVisibilities(true);
                    }
                }//close isRecording == false
            }
        });

        mSwitchViewButton = (ImageButton)view.findViewById(R.id.memento_recorder_fragment_button_switchView);
        mSwitchViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseMediaRecorder();
                releaseCamera();

                if (mCameraFacingBack) {
                    mCamera = getCameraInstance(CAMERA_FACING_FRONT);
                    startPreview(mSurface, CAMERA_FACING_FRONT);
                    mCameraFacingBack = false;
                }
                else {
                    mCamera = getCameraInstance(CAMERA_FACING_BACK);
                    startPreview(mSurface, CAMERA_FACING_BACK);
                    mCameraFacingBack = true;
                }
            }
        });

        mPlayButton = (ImageButton)view.findViewById(R.id.memento_recorder_fragment_button_play);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoFile == null)
                    return;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri mVideoUri = Uri.fromFile(mVideoFile);
                intent.setDataAndType(mVideoUri, "video/mp4");
                startActivity(intent);
            }
        });

        mListButton = (ImageButton)view.findViewById(R.id.memento_recorder_fragment_button_list);
        mListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MementoListActivity.newIntent(getActivity());
                startActivity(intent);
            }
        });

        return view;
    }

    /** Create a File for saving an image or video. */
    private static File getExternalOutputMediaFile(int type){
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return  null;

        //Specify a location to which the media will be saved, if such dir is missing, make one (mkdirs).
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Memento");
        if (!mediaStorageDir.exists())
            if (!mediaStorageDir.mkdirs())
                return null;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE)
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        else if (type == MEDIA_TYPE_VIDEO)
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        else
            return null;

        return mediaFile;
    }

    private File getInternalOutputMediaFile(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File videoFile = new File(getActivity().getFilesDir(), "VID_" + timeStamp + ".mp4");
        return videoFile;
    }

    /** Sets up the video recorder in portrait mode*/
    private boolean prepareVideoRecorder(int cameraView){
        mCamera = getCameraInstance(cameraView);
        try {
            mCamera.setPreviewTexture(mTextureView.getSurfaceTexture());
        }
        catch (IOException e){
            Log.e(TAG, "Error while getting surface texture: " + e.getMessage());
            return false;
        }

        mMediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        if (mCameraFacingBack)
            mMediaRecorder.setOrientationHint(90);//Sets the video recording to portrait mode
        else
            mMediaRecorder.setOrientationHint(270);

        //UPDATE
        mVideoFile = getExternalOutputMediaFile(MEDIA_TYPE_VIDEO);
//        mVideoFile = getInternalOutputMediaFile();
        mMediaRecorder.setOutputFile(mVideoFile.toString());
        //UPDATE

        try {
            mMediaRecorder.prepare();
        }
        catch (IllegalStateException e){
            Log.e(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        catch (IOException e){
            Log.e(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }

        return true;
    }

    /** Inform the user if the camera in usage is facing the back or the front, -1 if neither. */
    private int getCameraView(){
        return (mCameraFacingBack ? CAMERA_FACING_BACK:CAMERA_FACING_FRONT);
    }

    /** A safe way to get an instance of the Camera object. */
    private Camera getCameraInstance(int cameraView){
        Camera camera = mCamera;
        try {
            camera = Camera.open(cameraView);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        setCameraDisplayOrientation(cameraView, camera);

        return camera;
    }

    /** Set orientation to portrait mode. */
    private void setCameraDisplayOrientation(int cameraId, Camera camera){
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        int degrees = 0;
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation){
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == CAMERA_FACING_FRONT){
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; //Compensate for the mirror effect
        }
        else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /** Start the preview for the recorder before the user starts recording. */
    private void startPreview(SurfaceTexture surface, int cameraView){
        mCamera = getCameraInstance(cameraView);
        try {
            mCamera.setPreviewTexture(mSurface);
            mCamera.startPreview();
        }
        catch (IOException e){
            Log.e(TAG, "Error while preparing preview: " + e.getMessage());
        }
    }

    /** See the recorded output before saving it to a fragment */
    private void reviewRecordedOutput(){
        Intent intent = MementoPreviewActivity.newIntent(getActivity(), mVideoFile);
        startActivity(intent);
    }

    /** Release media recorder */
    private void releaseMediaRecorder(){
        if (mMediaRecorder != null){
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    /** Release camera for other applications to use */
    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }

    /** Sets visibility of buttons when recording */
    private void setButtonsVisibilities(boolean buttonsShouldBeVisible){
        if (buttonsShouldBeVisible) {
            mSwitchViewButton.setVisibility(View.VISIBLE);
            mPlayButton.setVisibility(View.VISIBLE);
            mListButton.setVisibility(View.VISIBLE);
        }
        else {
            mSwitchViewButton.setVisibility(View.GONE);
            mPlayButton.setVisibility(View.GONE);
            mListButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();
        startPreview(mSurface, getCameraView());
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
        releaseMediaRecorder();
        releaseCamera();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
