package app.stepwin.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Shree krishna on 8/21/2017.
 */

public class SqliteDatabase {

    public static final String DATABASE_NAME = "stepWin.db";
    public static final int DATABASE_VERSION = 1;

    /// **** TABLE FOR USER STATUS PREF
    private static final String TABLE_USER_PROGRESS = "table_user_progress";

    public static final String ID = "id";

    public static final String STEPS = "steps";
    public static final String DISTANCE = "distance";
    public static final String CALS = "cals";
    public static final String POINTS = "points";
    public static final String MINUTES = "minutes";
    public static final String TIME_MILLI = "time_milli";
    public static final String UNLOCK_COUNTER = "unlock_conter";
    public static final String DATE = "date";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

   /* public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String MERCHANT_ACCOUNT = "merchant_account";
    public static final String MERCHANT_QUESTION = "merchant_question";
    public static final String USER_FEEDBACK = "user_feedback";*/

    /*steps, distance, cals, points, minutes, time_milli, unlock_conter, date, username, email*/


    public static final String DATABASE_CREATE_TABLE_STATUS = "create table " + TABLE_USER_PROGRESS +
            "( " + ID + " text " + "integer primary key," + STEPS + " text, " + DISTANCE + " text, " +
            CALS + " text, " + POINTS + " text, " + MINUTES + " text, " + TIME_MILLI + " text, " + UNLOCK_COUNTER +
            " text, " + DATE + " text, " + USERNAME + " text, " + EMAIL + " text); ";


    public SQLiteDatabase db;
    private final Context context;
    private DatabaseHelper dbHelper;

    public SqliteDatabase(Context _context) {
        context = _context;
        dbHelper = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public SqliteDatabase open() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        db.close();
    }

    public SQLiteDatabase getDatabaseInstance() {
        return db;
    }

    /*public long insertMerchant(String username, String password) {
        long result = 0;
        String where = USERNAME + " = ?";
        Cursor c = db.query(TABLE_USER_PROGRESS, new String[]{USERNAME}, where, new String[]{username},
                null, null, null);

        if (c.getCount() == 0) {
            ContentValues value = new ContentValues();
            value.put(USERNAME, username);
            value.put(PASSWORD, password);
            value.put(MERCHANT_ACCOUNT, username + "-Account");
            value.put(MERCHANT_QUESTION, username + "-Question");
            value.put(USER_FEEDBACK, username + "-Feedback");

            result = db.insert(TABLE_USER_PROGRESS, null, value);
        }
        c.close();
        return result;
    }*/

    public long insertUserProgress(String steps, String distance, String cals, String points, String minutes,
                                   long time_milli, String unlock_conter, String date, String username, String email){
        long result = 0;


        ContentValues value = new ContentValues();
            value.put(STEPS, steps);
            value.put(USERNAME, distance);
            value.put(USERNAME, cals);
            value.put(USERNAME, points);
            value.put(USERNAME, minutes);
            value.put(USERNAME, time_milli);
            value.put(USERNAME, unlock_conter);
            value.put(USERNAME, date);
            value.put(USERNAME, username);
            value.put(USERNAME, email);

        result = db.insert(TABLE_USER_PROGRESS, null, value);
        db.close();

        return result;
    }

    public Cursor getMerchantData(String username) {
        String where = USERNAME + " = ?";
        return db.query(TABLE_USER_PROGRESS, null, where, new String[]{username}, null, null, null);
    }


    public long deleteMerchant(String username) {
        long result;
        String where = USERNAME + " = ?";
        result = db.delete(TABLE_USER_PROGRESS, where, new String[]{username});
        return result;
    }

}