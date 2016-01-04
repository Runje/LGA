package com.example.thomas.lga.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.thomas.lga.Finances.Frequency;
import com.example.thomas.lga.Finances.StandingOrder;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 06.09.2015.
 */
public class StandingOrderTable
{
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_FIRST_DATE = "first_date";
    private static final String KEY_LAST_DATE = "last_date";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_USER = "user";
    private static final String KEY_COSTS = "costs";
    private static final String KEY_WHO = "who";
    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_NUMBER = "number";
    private static final String TABLE_NAME = "standing_order";
    private static final String KEY_INSERT_DATE = "insert_date";
    private static final String KEY_MODIFIED_DATE = "modified_date";
    private static final String KEY_INSERT_ID = "insert_id";
    private static final String KEY_MODIFIED_ID = "modified_id";
    private static final String KEY_DELETED = "deleted";

    protected static String getCreateTableString()
    {
        return "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_FIRST_DATE + " LONG,"
                + KEY_LAST_DATE + " LONG,"
                + KEY_CATEGORY + " TEXT,"
                + KEY_USER + " TEXT,"
                + KEY_COSTS + " INT,"
                + KEY_WHO + " TEXT,"
                + KEY_FREQUENCY + " TEXT,"
                + KEY_NUMBER + " INT,"
                + KEY_INSERT_DATE + " LONG,"
                + KEY_MODIFIED_DATE + " LONG,"
                + KEY_DELETED + " INT,"
                + KEY_INSERT_ID + " TEXT,"
                + KEY_MODIFIED_ID + " TEXT"
                + ");";
    }

    public static void create(SQLiteDatabase db)
    {
        db.execSQL(getCreateTableString());
    }

    public static void add(StandingOrder standingOrder, SQLiteDatabase db)
    {
        ContentValues values = standingOrderToValues(standingOrder);
        // Inserting Row
        db.insert(TABLE_NAME, null, values);
    }

    private static ContentValues standingOrderToValues(StandingOrder standingOrder)
    {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, standingOrder.getName().trim());
        values.put(KEY_FIRST_DATE, standingOrder.getFirstDate().getMillis());
        values.put(KEY_LAST_DATE, standingOrder.getLastDate().getMillis());
        values.put(KEY_INSERT_DATE, standingOrder.getInsertDate().getMillis());
        values.put(KEY_MODIFIED_DATE, standingOrder.getLastModifiedDate().getMillis());
        values.put(KEY_CATEGORY, standingOrder.getCategory().toString().trim());
        values.put(KEY_USER, standingOrder.getUser().trim());
        values.put(KEY_COSTS, (int) (standingOrder.getCosts() * 100));
        values.put(KEY_WHO, standingOrder.getWho().trim());
        values.put(KEY_FREQUENCY, standingOrder.getFrequency().toString().trim());
        values.put(KEY_NUMBER, standingOrder.getNumber());
        values.put(KEY_DELETED, standingOrder.isDeleted());
        values.put(KEY_INSERT_ID, standingOrder.getCreatedFrom());
        values.put(KEY_MODIFIED_ID, standingOrder.getLastChangeFrom());
        return values;
    }

    public static ArrayList<StandingOrder> getAll(SQLiteDatabase db)
    {
        ArrayList<StandingOrder> points = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_DELETED + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{"0"});

        if (cursor.moveToFirst())
        {
            do
            {
                StandingOrder standingOrder = createStandingOrderFromCursor(cursor);
                points.add(standingOrder);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return points;
    }

    public static ArrayList<StandingOrder> getAllV11(SQLiteDatabase db, String id)
    {
        ArrayList<StandingOrder> points = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_DELETED + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{"0"});

        if (cursor.moveToFirst())
        {
            do
            {
                StandingOrder standingOrder = createStandingOrderFromCursorV11(cursor, id);
                points.add(standingOrder);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return points;
    }

    private static StandingOrder createStandingOrderFromCursor(Cursor cursor)
    {
        return new StandingOrder(cursor.getInt(0), cursor.getString(7), cursor.getInt(6) / 100f, cursor.getString(1), String.valueOf(cursor.getString(4)), cursor.getString(5), new DateTime(cursor.getLong(2)), new DateTime(cursor.getLong(3)), Frequency.valueOf(cursor.getString(8)), cursor.getInt(9), new DateTime(cursor.getLong(10)), new DateTime(cursor.getLong(11)), cursor.getInt(12) != 0, cursor.getString(13), cursor.getString(14));
    }

    private static StandingOrder createStandingOrderFromCursorV11(Cursor cursor, String id)
    {
        return new StandingOrder(cursor.getInt(0), cursor.getString(7), cursor.getInt(6) / 100f, cursor.getString(1), String.valueOf(cursor.getString(4)), cursor.getString(5), new DateTime(cursor.getLong(2)), new DateTime(cursor.getLong(3)), Frequency.valueOf(cursor.getString(8)), cursor.getInt(9), new DateTime(cursor.getLong(8)), new DateTime(cursor.getLong(9)), cursor.getInt(10) != 0, id, id);
    }


    public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion, String id)
    {
        if (oldVersion == 11 && newVersion == 12)
        {
            List<StandingOrder> standingOrders = getAllV11(db, id);
            drop(db);
            create(db);
            for (StandingOrder standingOrder : standingOrders)
            {
                add(standingOrder, db);
            }
        }
    }

    public static void drop(SQLiteDatabase db)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public static void update(StandingOrder standingOrder, SQLiteDatabase db, String myId)
    {
        standingOrder.setLastModifiedDate(DateTime.now());
        standingOrder.setLastChangeFrom(myId);
        ContentValues values = standingOrderToValues(standingOrder);
        int i = db.update(TABLE_NAME, values, KEY_ID + " = " + standingOrder.getId(), null);
    }

    public static void delete(StandingOrder standingOrder, SQLiteDatabase db)
    {
        standingOrder.setLastModifiedDate(DateTime.now());
        standingOrder.setDeleted(true);
        ContentValues values = standingOrderToValues(standingOrder);
        int i = db.update(TABLE_NAME, values, KEY_ID + " = " + standingOrder.getId(), null);
    }

    public static void deleteForReal(List<StandingOrder> standingOrders, SQLiteDatabase db)
    {
        for (StandingOrder standingOrder : standingOrders)
        {
            db.delete(TABLE_NAME, KEY_ID + " = ?", new String[]{Integer.toString(standingOrder.getId())});
        }
    }

    public static List<StandingOrder> getDeleted(SQLiteDatabase db)
    {
        ArrayList<StandingOrder> standingOrders = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_DELETED + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{"1"});

        if (cursor.moveToFirst())
        {
            do
            {
                StandingOrder b = createStandingOrderFromCursor(cursor);
                standingOrders.add(b);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return standingOrders;
    }

    public static void overwrite(StandingOrder standingOrder, SQLiteDatabase db)
    {
        ContentValues values = standingOrderToValues(standingOrder);
        int i = db.update(TABLE_NAME, values, KEY_ID + " = " + standingOrder.getId(), null);
    }

    public static StandingOrder getById(SQLiteDatabase db, int id)
    {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{Integer.toString(id)});

        StandingOrder result = null;
        if (cursor.moveToFirst())
        {
            result = createStandingOrderFromCursor(cursor);
        }

        cursor.close();
        return result;
    }

    public static List<StandingOrder> getChangedSince(SQLiteDatabase db, DateTime dateTime, String compensation)
    {
        ArrayList<StandingOrder> standingOrders = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_MODIFIED_DATE + " > ? AND " + KEY_NAME + " != ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{Long.toString(dateTime.getMillis()), compensation});

        if (cursor.moveToFirst())
        {
            do
            {
                StandingOrder standingOrder = createStandingOrderFromCursor(cursor);
                standingOrders.add(standingOrder);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return standingOrders;
    }

    public static void trim(List<StandingOrder> standingOrders, SQLiteDatabase db)
    {
        for (StandingOrder standingOrder : standingOrders)
        {
            ContentValues values = standingOrderToValues(standingOrder);
            int i = db.update(TABLE_NAME, values, KEY_ID + " = " + standingOrder.getId(), null);
        }
    }

    public static List<StandingOrder> getAllWithDeleted(SQLiteDatabase db)
    {
        ArrayList<StandingOrder> points = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            do
            {
                StandingOrder standingOrder = createStandingOrderFromCursor(cursor);
                points.add(standingOrder);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return points;
    }
}
