package younsuk.memento.phasei.pause;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import younsuk.memento.phasei.pause.database.MementoCursorWrapper;
import younsuk.memento.phasei.pause.database.MementoDbHelper;
import younsuk.memento.phasei.pause.database.MementoDbSchema;

/**
 * A singleton class: allows only one instance of itself to be created.
 * Created by Younsuk on 11/12/2015.
 */
public class MementoLab {

    private static MementoLab sMementoLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    /** Used by other classes to call upon singleton */
    public static MementoLab get(Context context){
        if (sMementoLab == null){
            sMementoLab = new MementoLab(context);
        }
        return sMementoLab;
    }

    /** This used to be mMementos = new ArrayList<>(). This is where list of mementos are retrieved from DB */
    private MementoLab(Context context){
        mContext = context.getApplicationContext();
        mDatabase = new MementoDbHelper(mContext).getWritableDatabase();
    }

    /** Returns the list of memento saved up in DB */
    public List<Memento> getMementos() {
        List<Memento> mementos = new ArrayList<>();

        MementoCursorWrapper cursor = queryMementos(null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                mementos.add(cursor.getMemento());
                cursor.moveToNext();
            }
        }
        finally {
            cursor.close();
        }

        return mementos;
    }

    /** Returns the memento with the specified file */
    public Memento getMemento(File file){
        MementoCursorWrapper cursor = queryMementos(
                MementoDbSchema.MementoTable.Columns.FILE + " = ?",
                new String[]{ file.toString() }
        );

        try {
            if (cursor.getCount() == 0)
                return null;

            cursor.moveToFirst();
            return cursor.getMemento();
        }
        finally {
            cursor.close();
        }
    }

    /** Updates video file */
    public void updateMemento(Memento memento){
        //3rd param: where clause, which row to update. 4th param: new value input
        mDatabase.update(MementoDbSchema.MementoTable.NAME, getContentValues(memento), MementoDbSchema.MementoTable.Columns.FILE + " = ?", new String[]{ memento.getPath() });
    }

    /** Add a new memento; used to be mMementos.add(memento); */
    public void addMemento(Memento memento){
        ContentValues values = getContentValues(memento);
        mDatabase.insert(MementoDbSchema.MementoTable.NAME, null, values);
    }

    /** Remove a memento; used to be mMementos.remove(memento); */
    public void removeMemento(Memento memento){
        mDatabase.delete(MementoDbSchema.MementoTable.NAME, MementoDbSchema.MementoTable.Columns.FILE + " = ?", new String[]{memento.getPath()});
    }

    /** Remove multiple mementos at the same time using contextual action */
    public void removeMementos(List<Memento> mementos){
        for (Memento memento: mementos)
            removeMemento(memento);
    }

    /** Get contents of the given memento */
    private static ContentValues getContentValues(Memento memento){
        ContentValues values = new ContentValues();
        values.put(MementoDbSchema.MementoTable.Columns.FILE, memento.getPath());
        values.put(MementoDbSchema.MementoTable.Columns.THUMBNAIL, bitmapToString(memento.getThumbnail()));
        values.put(MementoDbSchema.MementoTable.Columns.TITLE, memento.getTitle());
        values.put(MementoDbSchema.MementoTable.Columns.DATE, memento.getDate().getTime());
        values.put(MementoDbSchema.MementoTable.Columns.LOCATION_LATITUDE, memento.getLatitude());
        values.put(MementoDbSchema.MementoTable.Columns.LOCATION_LONGITUDE, memento.getLongitude());
        values.put(MementoDbSchema.MementoTable.Columns.LOCATION_ADDRESS, memento.getAddress());

        return values;
    }

    /** Reading data from SQLite */
    private MementoCursorWrapper queryMementos(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                MementoDbSchema.MementoTable.NAME,
                null, //Columns - null selects all columns
                whereClause,
                whereArgs,
                null, //groupBy
                null, //having
                null //orderBy
        );

        return new MementoCursorWrapper(cursor);
    }

    private static String bitmapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
}
