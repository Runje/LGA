package com.example.thomas.lga.Database;

/**
 * Created by Thomas on 06.09.2015.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.thomas.lga.Finances.Balance;
import com.example.thomas.lga.Finances.BankAccount;
import com.example.thomas.lga.Finances.Expenses;
import com.example.thomas.lga.Finances.StandingOrder;
import com.example.thomas.lga.Installation;
import com.example.thomas.lga.LGA;
import com.example.thomas.lga.R;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class SQLiteFinanceHandler extends SQLiteOpenHelper
{
    // Database Name
    public static final String DATABASE_NAME = "lga_finance.db";
    public static final String DEMO_DATABASE_NAME = "lga_finance_demo_db";
    // Database Version
    private static final int DATABASE_VERSION = 12;
    private Context context;

    public SQLiteFinanceHandler(Context context)
    {
        this(context, getActiveDatabaseName(), null, DATABASE_VERSION);
        this.context = context;
    }

    public SQLiteFinanceHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, name, factory, version);
        this.context = context;
    }

    public static String getActiveDatabaseName()
    {
        return LGA.getSingleton().isAuthorized() ? DATABASE_NAME : DEMO_DATABASE_NAME;
    }

    public static void addExpenses(Context context, Expenses expenses)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();
        ExpensesTable.add(expenses, db);
        db.close();
    }

    public static List<Expenses> getExpenses(Context context)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        List<Expenses> expenses = ExpensesTable.getAll(db);
        db.close();
        return expenses;
    }

    public static void updateExpenses(Context context, Expenses expenses, String myId)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();
        ExpensesTable.update(expenses, db, myId);
        db.close();
    }

    public static void overwriteExpenses(Context context, Expenses expenses)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();
        ExpensesTable.overwrite(expenses, db);
        db.close();
    }

    public static void deleteExpenses(Context context, Expenses ex)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();
        ExpensesTable.delete(ex, db);
        db.close();
    }

    public static void addStandingOrder(Context context, StandingOrder standingOrder)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();
        StandingOrderTable.add(standingOrder, db);
        db.close();
    }

    public static List<StandingOrder> getStandingOrders(Context context)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        List<StandingOrder> standingOrders = StandingOrderTable.getAll(db);
        db.close();
        return standingOrders;
    }

    public static void updateStandingOrder(Context context, StandingOrder standingOrder, String myId)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();
        StandingOrderTable.update(standingOrder, db, myId);
        db.close();
    }

    public static void deleteStandingOrder(Context context, StandingOrder standingOrder)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();
        StandingOrderTable.delete(standingOrder, db);
        db.close();
    }

    public static void deleteStandingOrderAndEntrys(Context context, Expenses ex)
    {
        StandingOrder standingOrder = getStandingOrderFrom(context, ex);
        if (standingOrder == null)
        {
            // TODO: make own delete dialog for standing order
            Toast.makeText(context, "Dauerauftrag wurde bereits gelöscht", Toast.LENGTH_SHORT).show();
            return;
        }
        deleteStandingOrder(context, standingOrder);

        List<Expenses> expenses = getExpensesFromStandingOrder(context, standingOrder);

        for (Expenses exp : expenses)
        {
            if (ex.getDate().isBefore(exp.getDate()))
            {
                deleteExpenses(context, exp);
            }
        }

        deleteExpenses(context, ex);
    }

    private static List<Expenses> getExpensesFromStandingOrder(Context context, StandingOrder order)
    {
        List<Expenses> all = getExpenses(context);
        List<Expenses> expenses = new ArrayList<>();
        // TODO: make db query
        for (Expenses ex : all)
        {
            if (order.getCategory().equals(ex.getCategory()) && order.getName().equals(ex.getName()) && order.getUser().equals(ex.getUser()) && order.getWho().equals(ex.getWho()) && ex.isStandingOrder())
            {
                expenses.add(ex);
            }
        }

        return expenses;
    }

    public static StandingOrder getStandingOrderFrom(Context context, Expenses ex)
    {
        List<StandingOrder> orders = getStandingOrders(context);
        // TODO: make db query
        for (StandingOrder order : orders)
        {
            if (order.getCategory().equals(ex.getCategory()) && order.getName().equals(ex.getName()) && order.getUser().equals(ex.getUser()) && order.getWho().equals(ex.getWho()) && !ex.getDate().isAfter(order.getLastDate()) && !ex.getDate().isBefore(order.getFirstDate()))
            {
                return order;
            }
        }

        return null;
    }

    public static boolean exists(Context context, Expenses expenses)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        boolean result = ExpensesTable.exists(db, expenses);
        db.close();
        return result;
    }

    public static boolean existsIgnoreCosts(Context context, Expenses expenses)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        boolean result = ExpensesTable.existsIgnoreCosts(db, expenses);
        db.close();
        return result;
    }

    public static List<BankAccount> getBankAccounts(Context context)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        List<BankAccount> bankAccounts = BankAccountTable.getAll(db);
        db.close();
        return bankAccounts;
    }

    public static void deleteBankAccount(Context context, BankAccount bankAccount)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();
        BankAccountTable.delete(bankAccount, db);
        db.close();
    }

    public static void addBankAccount(Context context, BankAccount bankAccount)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();
        BankAccountTable.add(bankAccount, db);
        db.close();
    }

    public static void updateBankAccount(Context context, BankAccount bankAccount, String myId)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();
        BankAccountTable.update(bankAccount, db, myId);
        db.close();
    }

    public static List<Balance> getBalances(Context context)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        List<Balance> balances = BalanceTable.getAll(db);
        db.close();
        return balances;
    }

    public static List<Balance> getBalances(Context context, BankAccount account)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        List<Balance> balances = BalanceTable.get(db, account);
        db.close();
        return balances;
    }

    public static void deleteBalance(Context context, Balance balance)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();
        BalanceTable.delete(balance, db);
        db.close();
    }

    public static void updateBalance(Context context, Balance balance, String myId)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();
        BalanceTable.update(balance, db, myId);
        db.close();
    }

    public static void addBalance(Context context, Balance balance)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();
        BalanceTable.add(balance, db);
        db.close();
    }

    public static Balance findBalance(Context context, Balance balance)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        Balance dbBalance = BalanceTable.find(balance, db);
        db.close();
        return dbBalance;
    }

    public static Expenses getExpenses(Context context, Expenses expenses)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        Expenses expenses1 = ExpensesTable.get(db, expenses);
        db.close();
        return expenses1;
    }

    public static Expenses getExpensesById(Context context, int id)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        Expenses expenses1 = ExpensesTable.getById(db, id);
        db.close();
        return expenses1;
    }

    public static List<Expenses> getExpensesToSync(Context context, DateTime dateTime)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        List<Expenses> expenses = ExpensesTable.getChangedSince(db, dateTime, context.getString(R.string.compensation));
        db.close();
        return expenses;
    }

    public static List<String> getAllCategorys(Context context)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        List<String> categorys = ExpensesTable.getCategorys(db);
        db.close();
        return categorys;
    }

    public static void cleanDatabase(Context context)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();

        // delete deleted for real!
        List<Balance> deletedBalances = BalanceTable.getDeleted(db);
        BalanceTable.deleteForReal(deletedBalances, db);
        List<BankAccount> deletedBankAccounts = BankAccountTable.getDeleted(db);
        BankAccountTable.deleteForReal(deletedBankAccounts, db);
        List<Expenses> deletedExpenses = ExpensesTable.getDeleted(db);
        ExpensesTable.deleteForReal(deletedExpenses, db);
        List<StandingOrder> deletedStandingOrders = StandingOrderTable.getDeleted(db);
        StandingOrderTable.deleteForReal(deletedStandingOrders, db);

        // trim all entrys(name, category)
        /*List<Balance> balances = BalanceTable.getAll(db);
        BalanceTable.trim(balances, db);
        List<BankAccount> bankAccounts = BankAccountTable.getAll(db);
        BankAccountTable.trim(bankAccounts, db);
        List<Expenses> expenses = ExpensesTable.getAll(db);
        ExpensesTable.trim(expenses, db);
        List<StandingOrder> standingOrders = StandingOrderTable.getAll(db);
        StandingOrderTable.trim(standingOrders, db);*/

        // rename categorys
        /*List<String> filter = new ArrayList<String>();
        filter.add("Other");
        filter.add("Living");

        List<String> replace = new ArrayList<String>();
        replace.add("Sonstiges");
        replace.add("Leben");
        List<StandingOrder> expensesList = SQLiteFinanceHandler.getStandingOrders(context, filter);

        for(StandingOrder expenses : expensesList)
        {
            if(expenses.getCategory().equals("Other"))
            {
                expenses.setCategory("Sonstiges");
                SQLiteFinanceHandler.updateStandingOrder(context, expenses, Installation.id(context));
            }

            if(expenses.getCategory().equals("Living"))
            {
                expenses.setCategory("Leben");
                SQLiteFinanceHandler.updateStandingOrder(context, expenses, Installation.id(context));
            }
        }*/

        // make all standingOrders mine

        // delete all expenses from standing orders
        /*Filter filter = new Filter();
        filter.setStandingOrder(Filter.YES);
        List<Expenses> expensesList = SQLiteFinanceHandler.getExpenses(context, filter);
        String myId = Installation.id(context);
        ExpensesTable.deleteForReal(expensesList, db);*/


    }

    public static List<StandingOrder> getStandingOrdersToSync(Context context, DateTime date)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        List<StandingOrder> standingOrders = StandingOrderTable.getChangedSince(db, date, context.getString(R.string.compensation));
        db.close();
        return standingOrders;
    }

    public static List<BankAccount> getBankAccountsToSync(Context context, DateTime date)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        List<BankAccount> bankAccounts = BankAccountTable.getChangedSince(db, date);
        db.close();
        return bankAccounts;
    }

    public static List<Balance> getBalancesToSync(Context context, DateTime date)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        List<Balance> balances = BalanceTable.getChangedSince(db, date);
        db.close();
        return balances;
    }

    public static StandingOrder getStandingOrderById(Context context, int id)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        StandingOrder standingOrder = StandingOrderTable.getById(db, id);
        db.close();
        return standingOrder;
    }

    public static BankAccount getBankAccountById(Context context, int id)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        BankAccount bankAccount = BankAccountTable.getById(db, id);
        db.close();
        return bankAccount;
    }

    public static Balance getBalanceById(Context context, int id)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        Balance balance = BalanceTable.getById(db, id);
        db.close();
        return balance;
    }

    public static void overwriteStandingOrder(Context context, StandingOrder standingOrder)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();
        StandingOrderTable.overwrite(standingOrder, db);
        db.close();
    }

    public static void overwriteBankAccount(Context context, BankAccount bankAccount)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();
        BankAccountTable.overwrite(bankAccount, db);
        db.close();
    }

    public static void overwriteBalance(Context context, Balance balance)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getWritableDatabase();
        BalanceTable.overwrite(balance, db);
        db.close();
    }

    public static List<StandingOrder> getStandingOrders(Context context, Filter filter)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        List<StandingOrder> standingOrders = StandingOrderTable.getFiltered(db, filter);
        db.close();
        return standingOrders;
    }

    public static List<Expenses> getExpenses(Context context, Filter filter)
    {
        if (filter == null)
        {
            return getExpenses(context);
        }

        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        List<Expenses> expenses = ExpensesTable.getFiltered(db, filter);
        db.close();
        return expenses;
    }

    public static String makePlaceholders(int len)
    {
        if (len < 1)
        {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else
        {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++)
            {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    public static Expenses getCompensationFrom(Context context, String name, DateTime dateTime)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        Expenses expenses = ExpensesTable.getCompensationFrom(db, name, dateTime, context.getResources().getString(R.string.compensation));
        db.close();
        return expenses;
    }

    public static List<Expenses> getExpensesFromCategoryAndMonth(Context context, String category, DateTime date)
    {
        SQLiteFinanceHandler handler = new SQLiteFinanceHandler(context);
        SQLiteDatabase db = handler.getReadableDatabase();
        List<Expenses> expenses = ExpensesTable.getFromCategoryAndMonth(db, category, date);
        db.close();
        return expenses;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.d("DB", "On Create");
        ExpensesTable.create(db);
        StandingOrderTable.create(db);
        BankAccountTable.create(db);
        BalanceTable.create(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
        String id = Installation.id(context);
        Log.d("DB", "On Upgrade: " + oldVersion + " --> " + newVersion);
        BalanceTable.upgrade(sqLiteDatabase, oldVersion, newVersion, id);
        BankAccountTable.upgrade(sqLiteDatabase, oldVersion, newVersion, id);
        ExpensesTable.upgrade(sqLiteDatabase, oldVersion, newVersion, id);
        StandingOrderTable.upgrade(sqLiteDatabase, oldVersion, newVersion, id);
    }
}
