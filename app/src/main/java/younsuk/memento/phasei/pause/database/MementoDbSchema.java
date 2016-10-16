package younsuk.memento.phasei.pause.database;

/**
 * Created by Younsuk on 11/19/2015.
 */
public class MementoDbSchema {

    public static final class MementoTable {
        public static final String NAME = "mementos";

        public static final class Columns {
            public static final String FILE = "file";
            public static final String THUMBNAIL = "thumbnail";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String LOCATION_LATITUDE = "latitude";
            public static final String LOCATION_LONGITUDE = "longitude";
            public static final String LOCATION_ADDRESS = "address";
        }
    }

}
