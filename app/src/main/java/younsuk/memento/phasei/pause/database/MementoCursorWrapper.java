package younsuk.memento.phasei.pause.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.File;
import java.util.Date;

import younsuk.memento.phasei.pause.Memento;

/**
 * Created by Younsuk on 11/21/2015.
 */
public class MementoCursorWrapper extends CursorWrapper {

    /** Wraps a Cursor from another place and add new methods on top of it */
    public MementoCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    /**  */
    public Memento getMemento(){
        String file = getString(getColumnIndex(MementoDbSchema.MementoTable.Columns.FILE));
        String thumbnail = getString(getColumnIndex(MementoDbSchema.MementoTable.Columns.THUMBNAIL));
        String title = getString(getColumnIndex(MementoDbSchema.MementoTable.Columns.TITLE));
        long date = getLong(getColumnIndex(MementoDbSchema.MementoTable.Columns.DATE));
        double latitude = getDouble(getColumnIndex(MementoDbSchema.MementoTable.Columns.LOCATION_LATITUDE));
        double longitude = getDouble(getColumnIndex(MementoDbSchema.MementoTable.Columns.LOCATION_LONGITUDE));
        String address = getString(getColumnIndex(MementoDbSchema.MementoTable.Columns.LOCATION_ADDRESS));

        Memento memento = new Memento(new File(file));
        memento.setThumbnail(stringToBitmap(thumbnail));
        memento.setTitle(title);
        memento.setDate(new Date(date));
        memento.setLatitude(latitude);
        memento.setLongitude(longitude);
        memento.setAddress(address);

        return memento;
    }

    private Bitmap stringToBitmap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
}
