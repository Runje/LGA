package com.example.thomas.lga.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.thomas.lga.Finances.Expenses;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 06.09.2015.
 */
public class ExpensesTable
{
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DATE = "date";
    private static final String KEY_INSERT_DATE = "insert_date";
    private static final String KEY_MODIFIED_DATE = "modified_date";
    private static final String KEY_INSERT_ID = "insert_id";
    private static final String KEY_MODIFIED_ID = "modified_id";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_USER = "user";
    private static final String KEY_COSTS = "costs";
    private static final String KEY_WHO = "who";
    private static final String KEY_STANDING_ORDER = "standing_order";
    private static final String KEY_DELETED = "deleted";
    private static final String TABLE_NAME = "expenses";
    private static String LogKey = "ExpensesTable";


    protected static String getCreateTableString()
    {
        return "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_DATE + " LONG,"
                + KEY_CATEGORY + " TEXT,"
                + KEY_USER + " TEXT,"
                + KEY_COSTS + " INT,"
                + KEY_WHO + " TEXT,"
                + KEY_STANDING_ORDER + " INT,"
                + KEY_INSERT_DATE + " LONG,"
                + KEY_MODIFIED_DATE + " LONG,"
                + KEY_DELETED + " INT,"
                + KEY_INSERT_ID + " TEXT,"
                + KEY_MODIFIED_ID + " TEXT"
                + ");";
    }

    public static void create(SQLiteDatabase db)
    {
        Log.d(LogKey, "Creating Table");
        db.execSQL(getCreateTableString());
    }

    public static void add(Expenses expenses, SQLiteDatabase db)
    {
        ContentValues values = expensesToValues(expenses);
        // Inserting Row
        db.insert(TABLE_NAME, null, values);
    }

    private static ContentValues expensesToValues(Expenses expenses)
    {
        ContentValues values = new ContentValues();
        if (expenses.getId() != 0)
        {
            values.put(KEY_ID, expenses.getId());
        }
        values.put(KEY_NAME, expenses.getName().trim());
        values.put(KEY_DATE, expenses.getDate().getMillis());
        values.put(KEY_INSERT_DATE, expenses.getInsertDate().getMillis());
        values.put(KEY_MODIFIED_DATE, expenses.getLastModifiedDate().getMillis());
        values.put(KEY_CATEGORY, expenses.getCategory().toString().trim());
        values.put(KEY_USER, expenses.getUser().trim());
        values.put(KEY_COSTS, (int) (expenses.getCosts() * 100));
        values.put(KEY_WHO, expenses.getWho().trim());
        values.put(KEY_STANDING_ORDER, expenses.isStandingOrder());
        values.put(KEY_DELETED, expenses.isDeleted());
        values.put(KEY_INSERT_ID, expenses.getCreatedFrom());
        values.put(KEY_MODIFIED_ID, expenses.getLastChangeFrom());
        return values;
    }

    public static ArrayList<Expenses> getAll(SQLiteDatabase db)
    {
        ArrayList<Expenses> points = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_DELETED + " = 0";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            do
            {
                Expenses expenses = createExpensesFromCursor(cursor);
                points.add(expenses);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return points;
    }

    public static ArrayList<Expenses> getAllV11(SQLiteDatabase db, String id)
    {
        ArrayList<Expenses> points = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            do
            {
                Expenses expenses = createExpensesFromCursorV11(cursor, id);
                points.add(expenses);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return points;
    }

    public static ArrayList<Expenses> getPoints(SQLiteDatabase db, String name)
    {
        ArrayList<Expenses> points = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_NAME + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{name});

        if (cursor.moveToFirst())
        {
            do
            {
                Expenses point = createExpensesFromCursor(cursor);
                points.add(point);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return points;
    }

    private static Expenses createExpensesFromCursor(Cursor cursor)
    {
        return new Expenses(cursor.getInt(0), cursor.getString(6), cursor.getInt(5) / 100f, cursor.getString(1), cursor.getString(3), cursor.getString(4), new DateTime(cursor.getLong(2)), cursor.getInt(7) != 0, new DateTime(cursor.getLong(8)), new DateTime(cursor.getLong(9)), cursor.getInt(10) != 0, cursor.getString(11), cursor.getString(12));
    }

    private static Expenses createExpensesFromCursorV11(Cursor cursor, String id)
    {
        return new Expenses(cursor.getInt(0), cursor.getString(6), cursor.getInt(5) / 100f, cursor.getString(1), cursor.getString(3), cursor.getString(4), new DateTime(cursor.getLong(2)), cursor.getInt(7) != 0, new DateTime(cursor.getLong(8)), new DateTime(cursor.getLong(9)), cursor.getInt(10) != 0, id, id);
    }


    public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion, String id)
    {
        if (oldVersion == 11 && newVersion == 12)
        {
            List<Expenses> expenses = getAllV11(db, id);
            drop(db);
            create(db);
            for (Expenses exp : expenses)
            {
                add(exp, db);
            }
        }
    }

    public static void drop(SQLiteDatabase db)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public static void update(Expenses expenses, SQLiteDatabase db, String myId)
    {
        expenses.setLastModifiedDate(DateTime.now());
        expenses.setLastChangeFrom(myId);
        ContentValues values = expensesToValues(expenses);
        int i = db.update(TABLE_NAME, values, KEY_ID + " = " + expenses.getId(), null);
    }

    public static void overwrite(Expenses expenses, SQLiteDatabase db)
    {
        ContentValues values = expensesToValues(expenses);
        int i = db.update(TABLE_NAME, values, KEY_ID + " = " + expenses.getId(), null);
    }

    public static void delete(Expenses ex, SQLiteDatabase db)
    {
        ex.setLastModifiedDate(DateTime.now());
        ex.setDeleted(true);
        ContentValues values = expensesToValues(ex);
        int i = db.update(TABLE_NAME, values, KEY_ID + " = " + ex.getId(), null);
    }

    public static boolean exists(SQLiteDatabase db, Expenses expenses)
    {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_DELETED + " = ? AND " + KEY_NAME + " = ? AND " + KEY_STANDING_ORDER + " = ? AND " + KEY_WHO + " = ? AND " + KEY_CATEGORY + " = ? AND " + KEY_COSTS + " = ? AND " + KEY_DATE + " = ? AND " + KEY_USER + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{"0", expenses.getName(), expenses.isStandingOrder() ? "1" : "0", expenses.getWho(), expenses.getCategory().toString(), Integer.toString((int) (expenses.getCosts() * 100)), Long.toString(expenses.getDate().getMillis()), expenses.getUser()});

        boolean result = false;
        if (cursor.moveToFirst())
        {
            result = cursor.getCount() > 0;
        }

        cursor.close();
        return result;
    }

    public static boolean existsIgnoreCosts(SQLiteDatabase db, Expenses expenses)
    {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_DELETED + " = ? AND " + KEY_NAME + " = ? AND " + KEY_STANDING_ORDER + " = ? AND " + KEY_WHO + " = ? AND " + KEY_CATEGORY + " = ? AND " + KEY_DATE + " = ? AND " + KEY_USER + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{"0", expenses.getName(), expenses.isStandingOrder() ? "1" : "0", expenses.getWho(), expenses.getCategory().toString(), Long.toString(expenses.getDate().getMillis()), expenses.getUser()});

        boolean result = false;
        if (cursor.moveToFirst())
        {
            result = cursor.getCount() > 0;
        }

        cursor.close();
        return result;
    }

    public static Expenses get(SQLiteDatabase db, Expenses expenses)
    {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_DELETED + " = ? AND " + KEY_NAME + " = ? AND " + KEY_STANDING_ORDER + " = ? AND " + KEY_WHO + " = ? AND " + KEY_CATEGORY + " = ? AND " + KEY_DATE + " = ? AND " + KEY_USER + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{"0", expenses.getName(), expenses.isStandingOrder() ? "1" : "0", expenses.getWho(), expenses.getCategory().toString(), Long.toString(expenses.getDate().getMillis()), expenses.getUser()});

        Expenses result = null;
        if (cursor.moveToFirst())
        {
            result = createExpensesFromCursor(cursor);
        }

        cursor.close();
        return result;
    }

    public static Expenses getById(SQLiteDatabase db, int id)
    {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{Integer.toString(id)});

        Expenses result = null;
        if (cursor.moveToFirst())
        {
            result = createExpensesFromCursor(cursor);
        }

        cursor.close();
        return result;
    }

    public static List<Expenses> getChangedSince(SQLiteDatabase db, DateTime dateTime, String compensation)
    {
        ArrayList<Expenses> expensesArrayList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_MODIFIED_DATE + " > ? AND " + KEY_NAME + " != ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{Long.toString(dateTime.getMillis()), compensation});

        if (cursor.moveToFirst())
        {
            do
            {
                Expenses expenses = createExpensesFromCursor(cursor);
                expensesArrayList.add(expenses);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return expensesArrayList;
    }

    public static List<Expenses> getDeleted(SQLiteDatabase db)
    {
        ArrayList<Expenses> expensesArrayList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_DELETED + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{"1"});

        if (cursor.moveToFirst())
        {
            do
            {
                Expenses b = createExpensesFromCursor(cursor);
                expensesArrayList.add(b);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return expensesArrayList;
    }

    public static List<String> getCategorys(SQLiteDatabase db)
    {
        ArrayList<String> categorys = new ArrayList<>();

        String selectQuery = "SELECT DISTINCT " + KEY_CATEGORY + " FROM " + TABLE_NAME + " WHERE " + KEY_DELETED + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{"0"});

        if (cursor.moveToFirst())
        {
            do
            {
                String c = cursor.getString(0);
                categorys.add(c);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return categorys;
    }
}
