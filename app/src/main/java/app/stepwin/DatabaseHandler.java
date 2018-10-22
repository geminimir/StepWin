package app.stepwin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by user on 31/08/2017.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "LocationManager";

    // Contacts table name
    private static final String TABLE_LOCATION = "LocationHistoryTable";

    // Contacts Table Columns names
    private static final String KEY_LONGITUDE = "Longitude";
    private static final String KEY_LATITUDE = "Latitude";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_LOCATION + "("
                + KEY_LONGITUDE + " REAL ," + KEY_LATITUDE + " REAL)";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);

        // Create tables again
        onCreate(db);
    }
    public void AddLocation(double longitude, double latitude) {
        if(longitude !=  0.0 && latitude != 0.0) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_LONGITUDE, longitude);
            values.put(KEY_LATITUDE, latitude);
            db.insert(TABLE_LOCATION, null, values);
            db.close();
        }
    }
    public Cursor RetrieveLocations() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_LOCATION, new String[] {KEY_LONGITUDE, KEY_LATITUDE},
                null, null, null, null, null);
    }
    public void DeleteTable() {
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("delete from "+ TABLE_LOCATION);

    }
}
