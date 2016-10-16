package younsuk.memento.phasei.pause.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Younsuk on 11/19/2015.
 */
public class MementoDbHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "mementoDbHelper.db";

    public MementoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + MementoDbSchema.MementoTable.NAME + "(" +
                        " _id integer primary key autoincrement, " +
                        MementoDbSchema.MementoTable.Columns.FILE + ", " +
                        MementoDbSchema.MementoTable.Columns.THUMBNAIL + ", " +
                        MementoDbSchema.MementoTable.Columns.TITLE + ", " +
                        MementoDbSchema.MementoTable.Columns.DATE + ", " +
                        MementoDbSchema.MementoTable.Columns.LOCATION_LATITUDE + ", " +
                        MementoDbSchema.MementoTable.Columns.LOCATION_LONGITUDE + ", " +
                        MementoDbSchema.MementoTable.Columns.LOCATION_ADDRESS + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  }
}
