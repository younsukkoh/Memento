package younsuk.memento.phasei.pause;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Younsuk on 10/26/2015.
 */
public class MementoFragment extends Fragment implements TextureView.SurfaceTextureListener, GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "MementoFragment";
    private static final String ARG_MEMENTO_FILE = "arg_memento_file";

    private static final String DIALOG_DATE = "dialog_date";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_PLACE_PICKER = 1;

    private Memento mMemento;
    private File mVideoFile;
    private MediaPlayer mMediaPlayer;
    private TextureView mTextureView;

    private ImageButton mListButton;
    private ImageButton mDeleteButton;

    private EditText mTitleField;
    private Button mWhoButton;
    private Button mWhenButton;
    private Button mWhereButton;
    private Button mWhyButton;

    private ImageButton mPlayButton;
    private ImageButton mRecordButton;

    /** Fragment argument bundle, which preserves fragment's independence. Retrieve data from other class */
    public static MementoFragment newInstance(File file){
        Bundle args = new Bundle();
        args.putSerializable(ARG_MEMENTO_FILE, file);
        MementoFragment fragment = new MementoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mVideoFile = (File)getArguments().getSerializable(ARG_MEMENTO_FILE);
        mMemento = MementoLab.get(getActivity()).getMemento(mVideoFile);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.memento_fragment, container, false);

        mTextureView = (TextureView)view.findViewById(R.id.memento_fragment_textureView);
        mTextureView.setSurfaceTextureListener(this);

        mListButton = (ImageButton)view.findViewById(R.id.memento_fragment_imageButton_list);
        mListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MementoListActivity.newIntent(getActivity());
                startActivity(intent);
            }
        });

        mDeleteButton = (ImageButton)view.findViewById(R.id.memento_fragment_imageButton_delete);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.memento_fragment_delete_warning)
                        .setMessage(R.string.memento_fragment_delete_warningMessage)
                        .setNegativeButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Memento memento = MementoLab.get(getActivity()).getMemento(mVideoFile);
                                MementoLab.get(getActivity()).removeMemento(memento);
                                getActivity().finish();
                            }
                        })
                        .setPositiveButton(android.R.string.cancel, null)
                        .create();
                dialog.show();
            }
        });

        mTitleField = (EditText)view.findViewById(R.id.memento_fragment_editText);
        if (mMemento.getTitle() == null)
            mTitleField.setText("");
        else
            mTitleField.setText(mMemento.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mMemento.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mWhenButton = (Button)view.findViewById(R.id.memento_fragment_button_when);
        mWhenButton.setText(new SimpleDateFormat("'At ' h:mm a ' On ' EEE, MMM d, yyyy").format(mMemento.getDate()));
        mWhenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                MementoFragment_DatePicker dialog = MementoFragment_DatePicker.newInstance(mMemento.getDate()); //Send DatePicker information
                dialog.setTargetFragment(MementoFragment.this, REQUEST_DATE); //Get information from DatePicker
                dialog.show(fragmentManager, DIALOG_DATE); //Dialog_Date is not an important factor, just used as a tag
            }
        });

        mWhereButton = (Button)view.findViewById(R.id.memento_fragment_button_where);
        mWhereButton.setText(mMemento.getAddress());
        Log.i(TAG, "YOLO 1 " + mMemento.getLatitude() + " " + mMemento.getLongitude() + " " + mMemento.getAddress());
        mWhereButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText textInput = new EditText(getActivity());
                textInput.setInputType(InputType.TYPE_CLASS_TEXT);
                textInput.setHint("");

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.memento_fragment_button_where_title)
                        .setView(textInput)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mMemento.setAddress(textInput.getText().toString());
                            }
                        })
                        .setNegativeButton(R.string.memento_fragment_button_where_choose, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final LatLngBounds defaultLocation = new LatLngBounds(new LatLng(mMemento.getLatitude(), mMemento.getLongitude()), new LatLng(mMemento.getLatitude(), mMemento.getLongitude()));
                                try {
                                    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                                    intentBuilder.setLatLngBounds(defaultLocation);

                                    Intent intent = intentBuilder.build(getActivity());
                                    intent.putExtra("layout", R.layout.memento_fragment_place_picker);
                                    startActivityForResult(intent, REQUEST_PLACE_PICKER);
                                } catch (GooglePlayServicesNotAvailableException e) {
                                    e.printStackTrace();
                                } catch (GooglePlayServicesRepairableException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNeutralButton(android.R.string.cancel, null);
                builder.show();
            }
        });

        mWhyButton = (Button)view.findViewById(R.id.memento_fragment_button_why);
        mWhyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mPlayButton = (ImageButton)view.findViewById(R.id.memento_fragment_imageButton_play);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(mMemento.getUri(), "video/mp4");
                startActivity(intent);
            }
        });

        mRecordButton = (ImageButton)view.findViewById(R.id.memento_fragment_imageButton_record);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == REQUEST_DATE) {
            Date date = (Date)data.getSerializableExtra(MementoFragment_DatePicker.EXTRA_DATE);
            mMemento.setDate(date);
            mWhenButton.setText(new SimpleDateFormat("'At ' h:mm a ' On ' EEE, MMM d, yyyy").format(mMemento.getDate()));
        }

        if (requestCode == REQUEST_PLACE_PICKER) {
            Place place = PlacePicker.getPlace(data, getActivity());

            String placeAttributions = PlacePicker.getAttributions(data);
            if (placeAttributions == null) placeAttributions = "";

            mMemento.setAddress(place.getName().toString());
            mWhereButton.setText(mMemento.getAddress());
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Surface surface = new Surface(surfaceTexture);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mMemento.getPath());
            mMediaPlayer.setSurface(surface);
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
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) { return false; }
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    /** Release media player for later usage */
    private void releaseMediaPlayer(){
        if (mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        MementoLab.get(getActivity()).updateMemento(mMemento); //Upon update, the new data will need to be written out.
//        Log.i(TAG, "YOLO pause MF");
    }

    @Override
    public void onStop(){
        super.onDestroy();
//        Log.i(TAG, "YOLO stop MF");
        releaseMediaPlayer();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
//        Log.i(TAG, "YOLO destroy MF");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
