package com.example.thomas.lga.Views.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.example.thomas.lga.Database.Filter;
import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.Finances.Expenses;
import com.example.thomas.lga.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Thomas on 08.09.2015.
 */
public class ExpensesDBAdapter extends ExpensesAdapter
{
    private Filter filter;

    public ExpensesDBAdapter(Context context, ExpensesDeleteListener listener)
    {
        super(context, listener);
        showDeleteButton = true;
    }

    public void updateExpenses()
    {
        List<Expenses> expensesList = SQLiteFinanceHandler.getExpenses(context, filter);

        Collections.sort(expensesList, new Comparator<Expenses>()
        {
            @Override
            public int compare(Expenses lhs, Expenses rhs)
            {
                return rhs.getDate().compareTo(lhs.getDate());
            }
        });
        setExpenses(expensesList);
        ((Activity) context).runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                notifyDataSetChanged();
            }
        });
    }

    protected void delete(final Expenses ex)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String string;
        String name = ex.getName();
        if (ex.isStandingOrder())
        {
            string = context.getString(R.string.confirm_standing_order_and_entrys_delete, name);
            builder.setNeutralButton(context.getString(R.string.delete_only_this, name), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    SQLiteFinanceHandler.deleteExpenses(context, ex);
                    updateExpenses();
                    if (callback != null)
                    {
                        callback.onDelete();
                    }
                }
            });
        } else
        {
            string = context.getString(R.string.confirm_delete, name);
        }
        builder.setMessage(string);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (ex.isStandingOrder())
                {
                    SQLiteFinanceHandler.deleteStandingOrderAndEntrys(context, ex);
                } else
                {
                    SQLiteFinanceHandler.deleteExpenses(context, ex);
                }
                updateExpenses();
                if (callback != null)
                {
                    callback.onDelete();
                }
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.create().show();
    }

    public Filter getFilter()
    {
        return filter;
    }

    public void setFilter(Filter filter)
    {
        this.filter = filter;
        updateExpenses();
    }
}
