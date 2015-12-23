package com.example.thomas.lga.Finances;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.thomas.lga.Activities.Utilities;
import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.Installation;
import com.example.thomas.lga.LGA;
import com.example.thomas.lga.R;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.Years;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Thomas on 06.09.2015.
 */
public class FinanceUtilities
{
    private static final String KEY_CATEGORYS = "CATEGORYS";
    private static String LogKey = "FinanceUtilities";

    public static String FrequencyToString(Context context, Frequency frequency)
    {
        switch (frequency)
        {
            case weekly:
                return context.getResources().getText(R.string.weekly).toString();
            case Monthly:
                return context.getResources().getText(R.string.monthly).toString();
            case Yearly:
                return context.getResources().getText(R.string.yearly).toString();
        }

        return null;
    }

    public static String TimeFrameToString(Context context, TimeFrame timeFrame)
    {
        switch (timeFrame)
        {
            case Overall:
                return context.getResources().getText(R.string.overall).toString();
            case LastMonth:
                return context.getResources().getText(R.string.last_month).toString();
            case LastYear:
                return context.getResources().getText(R.string.last_year).toString();
            case AverageMonth:
                return context.getResources().getText(R.string.average_month).toString();
            case ThisMonth:
                return context.getResources().getText(R.string.this_month).toString();
            case ThisYear:
                return context.getResources().getText(R.string.this_year).toString();
            case Debts:
                return context.getResources().getText(R.string.debts).toString();
            case FixedCosts:
                return context.getResources().getText(R.string.fixed_costs).toString();
            case Ownings:
                return context.getResources().getText(R.string.ownings).toString();
            case OwningsLastMonth:
                return context.getResources().getText(R.string.ownings_last_month).toString();
        }

        return null;
    }


    public static void saveCategorys(Context context, List<String> categorys)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < categorys.size(); i++)
        {
            sb.append(categorys.get(i)).append(",");
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.finance_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CATEGORYS, sb.toString());
        editor.commit();
    }

    public static OverviewItem getOverallOverview(List<Expenses> expenses, List<String> names)
    {
        return getTimeFrameOverview(expenses, names, TimeFrame.Overall, new TimeFrameChecker()
        {
            @Override
            public boolean isValid(DateTime time)
            {
                return true;
            }
        });
    }

    public static OverviewItem getLastMonthOverview(List<Expenses> expenses, List<String> names)
    {
        return getTimeFrameOverview(expenses, names, TimeFrame.LastMonth, new TimeFrameChecker()
        {
            @Override
            public boolean isValid(DateTime time)
            {
                return isLastMonth(time);
            }
        });
    }

    public static OverviewItem getThisMonthOverview(List<Expenses> expenses, List<String> names)
    {
        return getTimeFrameOverview(expenses, names, TimeFrame.ThisMonth, new TimeFrameChecker()
        {
            @Override
            public boolean isValid(DateTime time)
            {
                return isThisMonth(time);
            }
        });
    }

    public static OverviewItem getThisYearOverview(List<Expenses> expenses, List<String> names)
    {
        return getTimeFrameOverview(expenses, names, TimeFrame.ThisYear, new TimeFrameChecker()
        {
            @Override
            public boolean isValid(DateTime time)
            {
                return isThisYear(time);
            }
        });
    }

    public static OverviewItem getLastYearOverview(List<Expenses> expenses, List<String> names)
    {
        return getTimeFrameOverview(expenses, names, TimeFrame.LastYear, new TimeFrameChecker()
        {
            @Override
            public boolean isValid(DateTime time)
            {
                return isLastYear(time);
            }
        });
    }

    public static OverviewItem getAverageMonthOverview(List<Expenses> expenses, List<String> names)
    {
        OverviewItem overall = getOverallOverview(expenses, names);
        DateTime first = getFirstDate(expenses);
        int months = Months.monthsBetween(first.withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0), DateTime.now()).getMonths() + 1;

        overall.divide(months);
        overall.setTime(TimeFrame.AverageMonth);
        return overall;
    }

    private static DateTime getFirstDate(List<Expenses> expenses)
    {
        DateTime first = DateTime.now();
        for (Expenses ex : expenses)
        {
            if (ex.getDate().isBefore(first))
            {
                first = ex.getDate();
            }
        }

        return first;
    }

    private static OverviewItem getExpensesBetween(Context context, List<String> names, final DateTime start, final DateTime stop)
    {
        return getTimeFrameOverview(SQLiteFinanceHandler.getExpenses(context), names, TimeFrame.LastMonth, new TimeFrameChecker()
        {
            @Override
            public boolean isValid(DateTime time)
            {
                return time.isBefore(stop) && !time.isBefore(start);
            }
        });
    }

    public static OverviewItem getTimeFrameOverview(List<Expenses> expenses, List<String> names, TimeFrame timeFrame, TimeFrameChecker checker)
    {
        OverviewItem time = new OverviewItem(timeFrame);
        for (Expenses ex : expenses)
        {
            float costs = ex.getCosts();
            DateTime date = ex.getDate();
            if (!checker.isValid(date))
            {
                continue;
            }
            if (ex.getUser().equals(names.get(0)))
            {
                time.addP1(costs);
            } else if (ex.getUser().equals(names.get(1)))
            {
                time.addP2(costs);
            } else
            {
                time.addP1(costs / 2);
                time.addP2(costs / 2);
            }
        }

        return time;
    }

    public static OverviewItem getDebtsOverview(List<Expenses> expenses, List<String> names)
    {
        OverviewItem debts = new OverviewItem(TimeFrame.Debts);
        for (Expenses ex : expenses)
        {
            float costs = ex.getCosts();
            String executor = ex.getWho();
            String user = ex.getUser();
            if (executor.equals(user))
            {
                continue;
            }
            if (executor.equals(names.get(0)))
            {
                if (user.equals(names.get(1)))
                {
                    debts.addP1(-costs);
                    debts.addP2(costs);
                } else
                {
                    debts.addP1(-costs / 2);
                    debts.addP2(costs / 2);
                }
            } else if (executor.equals(names.get(1)))
            {
                if (user.equals(names.get(0)))
                {
                    debts.addP2(-costs);
                    debts.addP1(costs);
                } else
                {
                    debts.addP2(-costs / 2);
                    debts.addP1(costs / 2);
                }
            } else
            {
                if (user.equals(names.get(0)))
                {
                    debts.addP1(costs / 2);
                    debts.addP2(-costs / 2);
                } else
                {
                    debts.addP1(-costs / 2);
                    debts.addP2(costs / 2);
                }
            }
        }

        return debts;
    }

    public static OverviewItem getDebtsOverviewBefore(DateTime date, List<Expenses> expenses, List<String> names)
    {
        OverviewItem debts = new OverviewItem(TimeFrame.Debts);
        for (Expenses ex : expenses)
        {
            if (ex.getDate().isAfter(date))
            {
                continue;
            }

            float costs = ex.getCosts();
            String executor = ex.getWho();
            String user = ex.getUser();
            if (executor.equals(user))
            {
                continue;
            }
            if (executor.equals(names.get(0)))
            {
                if (user.equals(names.get(1)))
                {
                    debts.addP1(-costs);
                    debts.addP2(costs);
                } else
                {
                    debts.addP1(-costs / 2);
                    debts.addP2(costs / 2);
                }
            } else if (executor.equals(names.get(1)))
            {
                if (user.equals(names.get(0)))
                {
                    debts.addP2(-costs);
                    debts.addP1(costs);
                } else
                {
                    debts.addP2(-costs / 2);
                    debts.addP1(costs / 2);
                }
            } else
            {
                if (user.equals(names.get(0)))
                {
                    debts.addP1(costs / 2);
                    debts.addP2(-costs / 2);
                } else
                {
                    debts.addP1(-costs / 2);
                    debts.addP2(costs / 2);
                }
            }
        }

        return debts;
    }


    public static boolean isLastMonth(DateTime time)
    {
        DateTime now = DateTime.now().withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        int diff = Months.monthsBetween(time.withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0), now).getMonths();
        return diff == 1;
    }

    private static boolean isThisMonth(DateTime time)
    {
        DateTime now = DateTime.now().withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        int diff = Months.monthsBetween(time.withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0), now).getMonths();
        return diff == 0;
    }

    private static boolean isLastYear(DateTime time)
    {
        DateTime now = DateTime.now().withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        int diff = Years.yearsBetween(time.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0), now).getYears();
        return diff == 1;
    }

    private static boolean isThisYear(DateTime time)
    {
        DateTime now = DateTime.now().withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        int diff = Years.yearsBetween(time.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0), now).getYears();
        return diff == 0;
    }

    public static List<DateTime> getDueDates(StandingOrder order)
    {
        List<DateTime> times = new ArrayList<>();
        DateTime time = order.getFirstDate();
        if (time.isBefore(DateTime.now()))
        {
            times.add(time);
        }

        while (true)
        {
            if (order.getNumber() == 0)
            {
                break;
            }

            switch (order.getFrequency())
            {

                case weekly:
                    time = time.plusWeeks(order.getNumber());
                    break;
                case Monthly:
                    time = time.plusMonths(order.getNumber());
                    break;
                case Yearly:
                    time = time.plusYears(order.getNumber());
                    break;
            }

            if (time.isBefore(DateTime.now()) && time.isBefore(order.getLastDate().plusMillis(1)))
            {
                times.add(time);
            } else
            {
                break;
            }
        }

        return times;
    }

    public static void calculateCompensationPayment(final Context context, final Runnable runAfter)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                DateTime start = LGA.getSingleton().getStartDate().withDayOfMonth(1);
                DateTime stop = DateTime.now().withDayOfMonth(1);
                int months = Months.monthsBetween(start, stop).getMonths();
                List<String> names = LGA.getSingleton().getNames();
                OverviewItem lastOwnings = getOwningsFrom(context, names, start, TimeFrame.Ownings);

                for (int i = 0; i < months; i++)
                {
                    DateTime date = start.plusMonths(1);
                    OverviewItem ownings = getOwningsFrom(context, names, date, TimeFrame.Ownings);
                    OverviewItem lastExpenses = getExpensesBetween(context, names, start, date);

                    float d1 = ownings.getP1() - lastOwnings.getP1() - lastExpenses.getP1();
                    float d2 = ownings.getP2() - lastOwnings.getP2() - lastExpenses.getP2();
                    String myId = Installation.id(context);
                    Expenses expenses = new Expenses(names.get(0), d1, context.getResources().getString(R.string.compensation), context.getResources().getString(R.string.misc), names.get(0), date.minusDays(1), false, myId);
                    if (SQLiteFinanceHandler.existsIgnoreCosts(context, expenses))
                    {
                        // update
                        Expenses expenses1 = SQLiteFinanceHandler.getExpenses(context, expenses);
                        expenses1.setCosts(expenses1.getCosts() + d1);
                        SQLiteFinanceHandler.updateExpenses(context, expenses1, Installation.id(context));
                    } else
                    {
                        SQLiteFinanceHandler.addExpenses(context, expenses);
                    }

                    Expenses expenses2 = new Expenses(names.get(1), d2, context.getResources().getString(R.string.compensation), context.getResources().getString(R.string.misc), names.get(1), date.minusDays(1), false, myId);
                    if (SQLiteFinanceHandler.existsIgnoreCosts(context, expenses2))
                    {
                        // update
                        Expenses expenses1 = SQLiteFinanceHandler.getExpenses(context, expenses2);
                        expenses1.setCosts(expenses1.getCosts() + d2);
                        SQLiteFinanceHandler.updateExpenses(context, expenses1, Installation.id(context));
                    } else
                    {
                        SQLiteFinanceHandler.addExpenses(context, expenses2);
                    }

                    start = date;
                    lastOwnings = ownings;
                }

                runAfter.run();
            }
        }).start();
    }


    public static void synchronizeWithStandingOrders(final Context context, final Runnable runAfterSync)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Log.d(LogKey, "Synchronizing");
                List<StandingOrder> orders = SQLiteFinanceHandler.getStandingOrders(context);
                for (StandingOrder order : orders)
                {
                    synchronizeOrder(context, order);
                }

                runAfterSync.run();
            }
        }).start();
    }

    public static void synchronizeOrder(final Context context, StandingOrder order)
    {
        List<DateTime> dates = FinanceUtilities.getDueDates(order);

        for (DateTime date : dates)
        {
            Log.d(LogKey, date.toString("dd.MM.yyyy"));
            if (!FinanceUtilities.standingOrderExecuted(context, order, date))
            {
                FinanceUtilities.executeStandingOrder(context, order, date);
            } else
            {
                Log.d(LogKey, "exists already");
            }
        }
    }

    public static boolean standingOrderExecuted(Context context, StandingOrder order, DateTime date)
    {
        Expenses expenses = Utilities.createExpensesFromStandingOrder(order, date, Installation.id(context));
        return SQLiteFinanceHandler.existsIgnoreCosts(context, expenses);
    }

    public static void executeStandingOrder(Context context, StandingOrder order, DateTime date)
    {
        Expenses expenses = Utilities.createExpensesFromStandingOrder(order, date, Installation.id(context));
        SQLiteFinanceHandler.addExpenses(context, expenses);
        Log.d(LogKey, "added: " + expenses);
    }

    public static OverviewItem getFixedCostsOverview(List<StandingOrder> standingOrders, List<String> names)
    {
        OverviewItem time = new OverviewItem(TimeFrame.FixedCosts);
        for (StandingOrder standingOrder : standingOrders)
        {
            float costs = standingOrder.getCosts();
            Frequency frequency = standingOrder.getFrequency();
            switch (frequency)
            {

                case weekly:
                    costs *= 4;
                    break;
                case Monthly:
                    break;
                case Yearly:
                    costs /= 12;
                    break;
            }

            if (standingOrder.getUser().equals(names.get(0)))
            {
                time.addP1(costs);
            } else if (standingOrder.getUser().equals(names.get(1)))
            {
                time.addP2(costs);
            } else
            {
                time.addP1(costs / 2);
                time.addP2(costs / 2);
            }
        }

        return time;
    }


    public static void addNewCategory(Context context, String newCategory)
    {
        List<String> categorys = getCategorys(context);
        newCategory = newCategory.trim();

        for (String category : categorys)
        {
            if (newCategory.equals(category.trim()))
            {
                return;
            }
        }

        categorys.add(newCategory);
        saveCategorys(context, categorys);
    }

    public static List<String> getCategorys(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.finance_file_key), Context.MODE_PRIVATE);
        String categoryString = sharedPreferences.getString(KEY_CATEGORYS, "");
        String[] categorys = categoryString.split(",");
        return new ArrayList<>(Arrays.asList(categorys));

    }

    public static void deleteCategory(Context context, String category)
    {

        List<String> categorys = getCategorys(context);
        int index = -1;
        for (int i = 0; i < categorys.size(); i++)
        {
            if (categorys.get(i).equals(category))
            {
                index = i;
            }
        }

        if (index != -1)
        {
            categorys.remove(index);
        }

        saveCategorys(context, categorys);
    }

    public static OverviewItem getOwningsOverview(Context context, List<String> names)
    {
        List<BankAccount> bankAccounts = SQLiteFinanceHandler.getBankAccounts(context);

        OverviewItem time = new OverviewItem(TimeFrame.Ownings);
        for (BankAccount bankAccount : bankAccounts)
        {
            float balance = bankAccount.getBalance();

            if (bankAccount.getOwner().equals(names.get(0)))
            {
                time.addP1(balance);
            } else if (bankAccount.getOwner().equals(names.get(1)))
            {
                time.addP2(balance);
            } else
            {
                time.addP1(balance / 2);
                time.addP2(balance / 2);
            }
        }

        OverviewItem debts = getDebtsOverview(SQLiteFinanceHandler.getExpenses(context), names);
        time.addP1(debts.getP1());
        time.addP2(debts.getP2());
        return time;
    }

    public static Balance createBalanceFromAccount(BankAccount account)
    {
        return new Balance(account.getBalance(), account.getDate(), account.getBank(), account.getName());
    }

    public static OverviewItem getOwningsLastMonthOverview(Context context, List<String> names)
    {
        DateTime lastMonth = DateTime.now().minusMonths(1).withDayOfMonth(1);
        return getOwningsFrom(context, names, lastMonth, TimeFrame.OwningsLastMonth);
    }

    public static OverviewItem getOwningsFrom(Context context, List<String> names, DateTime date, TimeFrame timeFrame)
    {
        List<BankAccount> bankAccounts = SQLiteFinanceHandler.getBankAccounts(context);

        OverviewItem time = new OverviewItem(timeFrame);

        for (BankAccount bankAccount : bankAccounts)
        {
            float balance = getBalance(context, bankAccount, date);

            if (bankAccount.getOwner().equals(names.get(0)))
            {
                time.addP1(balance);
            } else if (bankAccount.getOwner().equals(names.get(1)))
            {
                time.addP2(balance);
            } else
            {
                time.addP1(balance / 2);
                time.addP2(balance / 2);
            }
        }

        OverviewItem debts = getDebtsOverviewBefore(date, SQLiteFinanceHandler.getExpenses(context), names);
        time.addP1(debts.getP1());
        time.addP2(debts.getP2());
        return time;
    }

    private static float getBalance(Context context, BankAccount bankAccount, DateTime date)
    {
        List<Balance> balances = SQLiteFinanceHandler.getBalances(context, bankAccount);
        DateTime lastDateBefore = new DateTime(0, 1, 1, 1, 1);
        float balanceBefore = Float.MAX_VALUE;
        float balanceAfter = Float.MAX_VALUE;
        DateTime firstDateAfter = new DateTime(3000, 1, 1, 1, 1);
        for (Balance balance : balances)
        {
            DateTime balanceDate = balance.getDate();
            if (Utilities.areSameDay(balanceDate, date))
            {
                return balance.getBalance();
            }

            if (balanceDate.isBefore(date) && balanceDate.isAfter(lastDateBefore))
            {
                lastDateBefore = balanceDate;
                balanceBefore = balance.getBalance();
            } else if (balanceDate.isAfter(date) && balanceDate.isBefore(firstDateAfter))
            {
                firstDateAfter = balanceDate;
                balanceAfter = balance.getBalance();
            }
        }

        if (lastDateBefore.equals(new DateTime(0, 1, 1, 1, 1)) && firstDateAfter.equals(new DateTime(3000, 1, 1, 1, 1)))
        {
            return bankAccount.getBalance();
        }

        if (lastDateBefore.equals(new DateTime(0, 1, 1, 1, 1)))
        {
            return balanceAfter;
        }

        if (firstDateAfter.equals(new DateTime(3000, 1, 1, 1, 1)))
        {
            return balanceBefore;
        }

        int days = Days.daysBetween(lastDateBefore, firstDateAfter).getDays();
        int daysUntil = Days.daysBetween(lastDateBefore, date).getDays();

        return balanceBefore + daysUntil * (balanceAfter - balanceBefore) / days;
    }

    public static List<Expenses> syncExpenses(Context context, List<Expenses> expenses, DateTime lastSync)
    {
        String myId = Installation.id(context);
        Log.d(LogKey, "last Sync: " + lastSync.toString() + ", id: " + myId);
        List<Expenses> conflicts = new ArrayList<>();
        for (Expenses ex : expenses)
        {
            if (ex.getName().equals(context.getString(R.string.compensation)) || ex.getLastChangeFrom().equals(myId))
            {
                // don't synchronize compensations
                continue;
            }

            boolean conflict = syncExpenses(context, ex, lastSync);
            if (conflict)
            {
                conflicts.add(ex);
            }
        }

        return conflicts;
    }

    private static boolean syncExpenses(Context context, Expenses expenses, DateTime lastSync)
    {
        Log.d(LogKey, "Syncing " + expenses.toString());
        if (expenses.getInsertDate().isAfter(lastSync))
        {
            // add new entry
            Log.d(LogKey, "Adding new Entry");
            // check if entry exists
            Expenses ex = SQLiteFinanceHandler.getExpensesById(context, expenses.getId());


            if (ex != null)
            {
                Log.d(LogKey, "Id found");
                // overwrite old entry
                SQLiteFinanceHandler.overwriteExpenses(context, expenses);

                // set id to 0 to create a new id
                ex.setId(0);
                // readd old entry
                SQLiteFinanceHandler.addExpenses(context, ex);
                Log.d(LogKey, "Overwrote old with new and readded old");
            } else
            {
                List<Expenses> expenses1 = SQLiteFinanceHandler.getExpenses(context);
                SQLiteFinanceHandler.addExpenses(context, expenses);
                Log.d(LogKey, "Added new");
            }

            // add category
            FinanceUtilities.addNewCategory(context, expenses.getCategory());
        } else
        {
            // update old entry
            Expenses ex = SQLiteFinanceHandler.getExpensesById(context, expenses.getId());
            if (ex == null)
            {
                throw new RuntimeException("Entry doesn't exist: " + expenses.toString());
            }

            // check for conflict
            if (ex.getLastModifiedDate().isAfter(lastSync))
            {
                // conflict
                Log.d(LogKey, "Conflict with " + ex.toString());
                return true;
            }

            SQLiteFinanceHandler.overwriteExpenses(context, expenses);
            Log.d(LogKey, "Overwrote old with new");

        }

        return false;

    }


    private interface TimeFrameChecker
    {
        boolean isValid(DateTime time);
    }
}
