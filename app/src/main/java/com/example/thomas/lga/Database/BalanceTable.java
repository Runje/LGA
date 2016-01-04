package com.example.thomas.lga.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.thomas.lga.Finances.Balance;
import com.example.thomas.lga.Finances.BankAccount;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 06.09.2015.
 */
public class BalanceTable
{
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DATE = "date";
    private static final String KEY_INSERT_DATE = "insert_date";
    private static final String KEY_MODIFIED_DATE = "modified_date";
    private static final String KEY_INSERT_ID = "insert_id";
    private static final String KEY_MODIFIED_ID = "modified_id";
    private static final String KEY_BANK = "bank";
    private static final String KEY_BALANCE = "balance";
    private static final String KEY_DELETED = "deleted";
    private static final String TABLE_NAME = "BalanceTable";
    private static String LogKey = "ExpensesTable";


    protected static String getCreateTableString()
    {
        return "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_DATE + " LONG,"
                + KEY_BANK + " TEXT,"
                + KEY_BALANCE + " INT,"
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

    public static void add(Balance balance, SQLiteDatabase db)
    {
        ContentValues values = balanceToValues(balance);
        // Inserting Row
        db.insert(TABLE_NAME, null, values);
    }

    private static ContentValues balanceToValues(Balance balance)
    {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, balance.getBankAccountName().trim());
        values.put(KEY_DATE, balance.getDate().getMillis());
        values.put(KEY_INSERT_DATE, balance.getInsertDate().getMillis());
        values.put(KEY_MODIFIED_DATE, balance.getLastModifiedDate().getMillis());
        values.put(KEY_BANK, balance.getBankName().trim());
        values.put(KEY_BALANCE, (int) (balance.getBalance() * 100));
        values.put(KEY_DELETED, balance.isDeleted());
        values.put(KEY_INSERT_ID, balance.getCreatedFrom());
        values.put(KEY_MODIFIED_ID, balance.getLastChangeFrom());
        return values;
    }

    public static ArrayList<Balance> getAllV11(SQLiteDatabase db, String id)
    {
        ArrayList<Balance> balances = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            do
            {
                Balance balance = createBalanceFromCursorV11(cursor, id);
                balances.add(balance);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return balances;
    }

    public static ArrayList<Balance> getAll(SQLiteDatabase db)
    {
        ArrayList<Balance> balances = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_DELETED + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{"0"});

        if (cursor.moveToFirst())
        {
            do
            {
                Balance balance = createBalanceFromCursor(cursor);
                balances.add(balance);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return balances;
    }


    private static Balance createBalanceFromCursor(Cursor cursor)
    {
        return new Balance(cursor.getInt(0), cursor.getInt(4) / 100f, new DateTime(cursor.getLong(2)), cursor.getString(3), cursor.getString(1), new DateTime(cursor.getLong(5)), new DateTime(cursor.getLong(6)), cursor.getInt(7) != 0, cursor.getString(8), cursor.getString(9));
    }

    private static Balance createBalanceFromCursorV11(Cursor cursor, String id)
    {
        return new Balance(cursor.getInt(0), cursor.getInt(4) / 100f, new DateTime(cursor.getLong(2)), cursor.getString(3), cursor.getString(1), new DateTime(cursor.getLong(5)), new DateTime(cursor.getLong(6)), cursor.getInt(7) != 0, id, id);
    }

    public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion, String id)
    {
        if (oldVersion == 11 && newVersion == 12)
        {
            List<Balance> balances = getAllV11(db, id);
            drop(db);
            create(db);
            for (Balance balance : balances)
            {
                add(balance, db);
            }
        }
    }

    public static void drop(SQLiteDatabase db)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public static void update(Balance balance, SQLiteDatabase db, String myId)
    {
        balance.setLastModifiedDate(DateTime.now());
        balance.setLastChangeFrom(myId);
        ContentValues values = balanceToValues(balance);
        int i = db.update(TABLE_NAME, values, KEY_ID + " = " + balance.getId(), null);
    }

    public static void delete(Balance balance, SQLiteDatabase db)
    {
        balance.setDeleted(true);
        balance.setLastModifiedDate(DateTime.now());
        ContentValues values = balanceToValues(balance);
        int i = db.update(TABLE_NAME, values, KEY_ID + " = " + balance.getId(), null);
    }

    public static void deleteForReal(List<Balance> balances, SQLiteDatabase db)
    {
        for (Balance balance : balances)
        {
            db.delete(TABLE_NAME, KEY_ID + " = ?", new String[]{Integer.toString(balance.getId())});
        }
    }

    public static Balance find(Balance balance, SQLiteDatabase db)
    {
        ArrayList<Balance> balances = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_DELETED + " = ? AND " + KEY_NAME + " = ? AND " + KEY_BANK + " = ? AND " + KEY_DATE + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{"0", balance.getBankAccountName(), balance.getBankName(), Long.toString(balance.getDate().getMillis())});

        if (cursor.moveToFirst())
        {
            do
            {
                Balance b = createBalanceFromCursor(cursor);
                balances.add(b);
            } while (cursor.moveToNext());
        }

        cursor.close();

        if (balances.size() == 0)
        {
            return null;
        } else if (balances.size() == 1)
        {
            return balances.get(0);
        } else
        {
            Log.e(LogKey, "More than one entry for " + balance.toString());
            return balances.get(0);
        }
    }

    public static List<Balance> get(SQLiteDatabase db, BankAccount account)
    {
        ArrayList<Balance> balances = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_DELETED + " = ? AND " + KEY_NAME + " = ? AND " + KEY_BANK + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{"0", account.getName(), account.getBank()});

        if (cursor.moveToFirst())
        {
            do
            {
                Balance b = createBalanceFromCursor(cursor);
                balances.add(b);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return balances;
    }

    public static List<Balance> getDeleted(SQLiteDatabase db)
    {
        ArrayList<Balance> balances = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_DELETED + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{"1"});

        if (cursor.moveToFirst())
        {
            do
            {
                Balance b = createBalanceFromCursor(cursor);
                balances.add(b);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return balances;
    }

    public static void overwrite(Balance balance, SQLiteDatabase db)
    {
        ContentValues values = balanceToValues(balance);
        int i = db.update(TABLE_NAME, values, KEY_ID + " = " + balance.getId(), null);
    }

    public static Balance getById(SQLiteDatabase db, int id)
    {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{Integer.toString(id)});

        Balance result = null;
        if (cursor.moveToFirst())
        {
            result = createBalanceFromCursor(cursor);
        }

        cursor.close();
        return result;
    }

    public static List<Balance> getChangedSince(SQLiteDatabase db, DateTime dateTime)
    {
        ArrayList<Balance> balances = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_MODIFIED_DATE + " > ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{Long.toString(dateTime.getMillis())});

        if (cursor.moveToFirst())
        {
            do
            {
                Balance balance = createBalanceFromCursor(cursor);
                balances.add(balance);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return balances;
    }

    public static void trim(List<Balance> balances, SQLiteDatabase db)
    {
        for (Balance balance : balances)
        {
            ContentValues values = balanceToValues(balance);
            int i = db.update(TABLE_NAME, values, KEY_ID + " = " + balance.getId(), null);
        }
    }
}
