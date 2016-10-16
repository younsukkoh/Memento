package younsuk.memento.phasei.pause;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.SurfaceTexture;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * When the this fragment is instantiated, as GoogleApiClient initializes, the location of the memento will update.
 *
 * Created by Younsuk on 11/11/2015.
 */
public class MementoPreviewFragment extends Fragment implements TextureView.SurfaceTextureListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MementoPreviewFragment";
    private final static int REQUEST_CONNECTION_FAILURE_RESOLUTION = 777;

    private MediaPlayer mMediaPlayer;
    private TextureView mTextureView;
    private File mVideoFile;

    private GoogleApiClient mGoogleApiClient;
    private double mLatitude;
    private double mLongitude;
    private String mAddress;

    private ImageButton mOkButton;
    private ImageButton mCancelButton;
    private ImageButton mSaveButton;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mVideoFile = (File)getActivity().getIntent().getSerializableExtra(MementoPreviewActivity.EXTRA_VIDEO_FILE_MPA);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this) //As connection happens, the latitude, the longitude and the address will be setup.
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.memento_preview_fragment, container, false);

        mTextureView = (TextureView)view.findViewById(R.id.memento_preview_fragment_textureView);
        mTextureView.setSurfaceTextureListener(this);

        mOkButton = (ImageButton)view.findViewById(R.id.memento_preview_fragment_imageButton_ok);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Memento memento = new Memento(mVideoFile);
                memento.setLatitude(mLatitude);
                memento.setLongitude(mLongitude);
                memento.setAddress(mAddress);
                memento.setThumbnail(ThumbnailUtils.createVideoThumbnail(memento.getPath(), MediaStore.Video.Thumbnails.MINI_KIND));

                MementoLab.get(getActivity()).addMemento(memento);
                Intent intent = MementoFragment_PagerActivity.newIntent(getActivity(), mVideoFile);
                startActivity(intent);
            }
        });

        mCancelButton = (ImageButton)view.findViewById(R.id.memento_preview_fragment_imageButton_cancel);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoFile = null;
                getActivity().finish();
            }
        });

        mSaveButton = (ImageButton)view.findViewById(R.id.memento_preview_fragment_imageButton_save);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {

        //Mirrors effect for the front camera, but it should not do this for back camera.
//        Matrix matrix = new Matrix();
//        matrix.setScale(-1, 1);
//        matrix.postTranslate(mTextureView.getWidth(), 0);
//        mTextureView.setTransform(matrix);
        //

        Surface surface = new Surface(surfaceTexture);
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(mVideoFile.toString());
            mMediaPlayer.setSurface(surface);
//            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
        }
        catch (IOException e){
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) { }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) { return false; }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) { }

    /** Create a File and save it to SD cared. */
    private void saveToExternalStorage(){
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return;

        //Specify a location to which the media will be saved, if such dir is missing, make one (mkdirs).
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Memento");
        if (!mediaStorageDir.exists())
            if (!mediaStorageDir.mkdirs())
                return;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
    }

    /** Retrieve data for the current location
     *  */
    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(getActivity(), 0, this)
                    .build();

            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer placeLikelihoods) {
                    Place place = placeLikelihoods.get(0).getPlace();

                    mLatitude = place.getLatLng().latitude;
                    mLongitude = place.getLatLng().longitude;
                    mAddress = place.getName().toString();

                    placeLikelihoods.release();
                }
            });
        }
        else {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();

            StringBuilder address = new StringBuilder();
            try {
                Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(mLatitude, mLongitude, 1); //1 is the max result
                if (addresses.size() > 0){
                    Address a = addresses.get(0);
                    for (int i = 0; i <a.getMaxAddressLineIndex(); i ++) //Get full street address
                        address.append(a.getAddressLine(i)).append(" ");
                    //                address.append(address.getCountryName());
                }
            }
            catch (IOException ioe){
                ioe.printStackTrace();
            }
            mAddress = address.toString();
        }
    }

    @Override
    public void onConnectionSuspended(int i) { /** N/A */ }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(getActivity(), REQUEST_CONNECTION_FAILURE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
        else
            Log.i(TAG, "Location services connection failed: " + connectionResult.getErrorCode());
    }

    /** Release media player for later usage */
    private void releaseMediaPlayer(){
        if (mMediaPlayer != null){
            mMediaPlayer.reset();
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause(){
        super.onPause();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
//        Log.i(TAG, "YOLO pause MPF");
    }

    @Override
    public void onStop(){
        super.onDestroy();
//        Log.i(TAG, "YOLO stop MPF");
        releaseMediaPlayer();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
//        Log.i(TAG, "YOLO destroy MPF");
    }

}
