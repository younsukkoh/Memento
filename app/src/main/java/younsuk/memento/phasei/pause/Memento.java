package younsuk.memento.phasei.pause;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.Date;

/**
 * Created by Younsuk on 11/1/2015.
 */
public class Memento {

    private File mFile;
    private Bitmap mThumbnail;
    private String mTitle;
    private Date mDate;
    private double mLatitude;
    private double mLongitude;
    private String mAddress;

    public Memento(File file){
        mFile = file;
        mDate = new Date();
    }

    public Bitmap getThumbnail() { return mThumbnail; }

    public void setThumbnail(Bitmap thumbnail) { mThumbnail = thumbnail; }

    public String getAddress() { return mAddress; }

    public void setAddress(String address) { mAddress = address; }

    public void setLatitude(double latitude) { mLatitude = latitude; }

    public double getLatitude() { return mLatitude; }

    public void setLongitude(double longitude) { mLongitude = longitude; }

    public double getLongitude() { return mLongitude; }

    public File getFile() { return mFile; }

    public Uri getUri() { return Uri.fromFile(mFile); }

    public String getPath(){ return mFile.toString(); }

    public String getTitle() { return mTitle; }

    public void setTitle(String title) { mTitle = title; }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }
}