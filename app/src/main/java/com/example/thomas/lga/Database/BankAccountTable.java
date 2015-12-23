package com.example.thomas.lga.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.thomas.lga.Finances.BankAccount;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 06.09.2015.
 */
public class BankAccountTable
{
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DATE = "date";
    private static final String KEY_INSERT_DATE = "insert_date";
    private static final String KEY_MODIFIED_DATE = "modified_date";
    private static final String KEY_INSERT_ID = "insert_id";
    private static final String KEY_MODIFIED_ID = "modified_id";
    private static final String KEY_BANK = "bank";
    private static final String KEY_OWNER = "owner";
    private static final String KEY_MONTHLY_COSTS = "monthly_costs";
    private static final String KEY_INTEREST = "interest";
    private static final String KEY_BALANCE = "balance";
    private static final String KEY_DELETED = "deleted";
    private static final String TABLE_NAME = "BankAccountTable";
    private static String LogKey = "ExpensesTable";


    protected static String getCreateTableString()
    {
        return "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_DATE + " LONG,"
                + KEY_BANK + " TEXT,"
                + KEY_OWNER + " TEXT,"
                + KEY_MONTHLY_COSTS + " INT,"
                + KEY_INTEREST + " INT,"
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

    public static void add(BankAccount bankAccount, SQLiteDatabase db)
    {
        ContentValues values = bankAccountToValues(bankAccount);
        // Inserting Row
        db.insert(TABLE_NAME, null, values);
    }

    private static ContentValues bankAccountToValues(BankAccount bankAccount)
    {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, bankAccount.getName().trim());
        values.put(KEY_DATE, bankAccount.getDate().getMillis());
        values.put(KEY_INSERT_DATE, bankAccount.getInsertDate().getMillis());
        values.put(KEY_MODIFIED_DATE, bankAccount.getLastModifiedDate().getMillis());
        values.put(KEY_BANK, bankAccount.getBank().trim());
        values.put(KEY_OWNER, bankAccount.getOwner().trim());
        values.put(KEY_MONTHLY_COSTS, (int) (bankAccount.getMonthly_costs() * 100));
        values.put(KEY_INTEREST, (int) (bankAccount.getInterest() * 100));
        values.put(KEY_BALANCE, (int) (bankAccount.getBalance() * 100));
        values.put(KEY_DELETED, bankAccount.isDeleted());
        values.put(KEY_INSERT_ID, bankAccount.getCreatedFrom());
        values.put(KEY_MODIFIED_ID, bankAccount.getLastChangeFrom());
        return values;
    }

    public static ArrayList<BankAccount> getAll(SQLiteDatabase db)
    {
        ArrayList<BankAccount> points = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_DELETED + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{"0"});

        if (cursor.moveToFirst())
        {
            do
            {
                BankAccount bankAccount = createBankAccountFromCursor(cursor);
                points.add(bankAccount);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return points;
    }

    public static ArrayList<BankAccount> getAllV11(SQLiteDatabase db, String id)
    {
        ArrayList<BankAccount> points = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            do
            {
                BankAccount bankAccount = createBankAccountFromCursorV11(cursor, id);
                points.add(bankAccount);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return points;
    }

    public static ArrayList<BankAccount> getPoints(SQLiteDatabase db, String name)
    {
        ArrayList<BankAccount> points = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_DELETED + " = ? AND " + KEY_NAME + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{"0", name});

        if (cursor.moveToFirst())
        {
            do
            {
                BankAccount bankAccount = createBankAccountFromCursor(cursor);
                points.add(bankAccount);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return points;
    }


    private static BankAccount createBankAccountFromCursor(Cursor cursor)
    {
        return new BankAccount(cursor.getInt(0), cursor.getString(1), cursor.getString(3), cursor.getInt(6) / 100f, cursor.getInt(5) / 100f, cursor.getString(4), new DateTime(cursor.getLong(2)), cursor.getInt(7) / 100f, new DateTime(cursor.getLong(8)), new DateTime(cursor.getLong(9)), cursor.getInt(10) != 0, cursor.getString(11), cursor.getString(12));
    }

    private static BankAccount createBankAccountFromCursorV11(Cursor cursor, String id)
    {
        return new BankAccount(cursor.getInt(0), cursor.getString(1), cursor.getString(3), cursor.getInt(6) / 100f, cursor.getInt(5) / 100f, cursor.getString(4), new DateTime(cursor.getLong(2)), cursor.getInt(7) / 100f, new DateTime(cursor.getLong(8)), new DateTime(cursor.getLong(9)), cursor.getInt(10) != 0, id, id);
    }

    public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion, String id)
    {
        if (oldVersion == 11 && newVersion == 12)
        {
            List<BankAccount> accounts = getAllV11(db, id);
            drop(db);
            create(db);
            for (BankAccount account : accounts)
            {
                add(account, db);
            }
        }
    }

    public static void drop(SQLiteDatabase db)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public static void update(BankAccount bankAccount, SQLiteDatabase db, String myId)
    {
        bankAccount.setLastModifiedDate(DateTime.now());
        bankAccount.setLastChangeFrom(myId);
        ContentValues values = bankAccountToValues(bankAccount);
        int i = db.update(TABLE_NAME, values, KEY_ID + " = " + bankAccount.getId(), null);
    }

    public static void delete(BankAccount bankAccount, SQLiteDatabase db)
    {
        bankAccount.setDeleted(true);
        bankAccount.setLastModifiedDate(DateTime.now());
        ContentValues values = bankAccountToValues(bankAccount);
        int i = db.update(TABLE_NAME, values, KEY_ID + " = " + bankAccount.getId(), null);
    }

    public static List<BankAccount> getDeleted(SQLiteDatabase db)
    {
        ArrayList<BankAccount> accounts = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_DELETED + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{"0"});

        if (cursor.moveToFirst())
        {
            do
            {
                BankAccount b = createBankAccountFromCursor(cursor);
                accounts.add(b);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return accounts;
    }

    public static List<BankAccount> getChangedSince(SQLiteDatabase db, DateTime dateTime)
    {
        ArrayList<BankAccount> bankAccounts = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_MODIFIED_DATE + " > ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{Long.toString(dateTime.getMillis())});

        if (cursor.moveToFirst())
        {
            do
            {
                BankAccount bankAccount = createBankAccountFromCursor(cursor);
                bankAccounts.add(bankAccount);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return bankAccounts;
    }

}
