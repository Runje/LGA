package com.example.thomas.lga.Views.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.Finances.Expenses;
import com.example.thomas.lga.Finances.FinanceUtilities;
import com.example.thomas.lga.Finances.OverviewItem;
import com.example.thomas.lga.LGA;
import com.example.thomas.lga.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Thomas on 12.09.2015.
 */
public class OverviewAdapter extends BaseAdapter
{
    private final Context context;
    List<OverviewItem> items;
    Mode mode = Mode.Expenses;

    public OverviewAdapter(Context context)
    {
        this.context = context;
        update();
    }

    public Mode getMode()
    {
        return mode;
    }

    public void setMode(Mode mode)
    {
        this.mode = mode;
        update();
    }

    public void update()
    {
        synchronized (this)
        {
            switch (mode)
            {

                case Expenses:
                    List<Expenses> expenses = SQLiteFinanceHandler.getExpenses(context);
                    final OverviewItem thisMonth = FinanceUtilities.getThisMonthOverview(expenses, LGA.getSingleton().getNames());
                    final OverviewItem lastMonth = FinanceUtilities.getLastMonthOverview(expenses, LGA.getSingleton().getNames());
                    final OverviewItem averageMonth = FinanceUtilities.getAverageMonthOverview(expenses, LGA.getSingleton().getNames(), LGA.getSingleton().getStartDate());
                    final OverviewItem debts = FinanceUtilities.getDebtsOverview(expenses, LGA.getSingleton().getNames());

                    ((Activity) context).runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            items = new ArrayList<>();
                            items.add(thisMonth);
                            items.add(lastMonth);
                            items.add(averageMonth);
                            items.add(debts);
                            notifyDataSetChanged();
                        }
                    });
                    break;
                case StandingOrder:
                    final OverviewItem fixedCosts = FinanceUtilities.getFixedCostsOverview(SQLiteFinanceHandler.getStandingOrders(context), LGA.getSingleton().getNames());

                    ((Activity) context).runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            items = new ArrayList<>();
                            items.add(fixedCosts);
                            notifyDataSetChanged();
                        }
                    });
                    break;
                case BankAccount:
                    expenses = SQLiteFinanceHandler.getExpenses(context);
                    //final OverviewItem overall = FinanceUtilities.getOverallOverview(expenses, LGA.getSingleton().getNames());
                    //final OverviewItem thisYear = FinanceUtilities.getThisYearOverview(expenses, LGA.getSingleton().getNames());
                    //final OverviewItem lastYear = FinanceUtilities.getLastYearOverview(expenses, LGA.getSingleton().getNames());
                    final OverviewItem ownings = FinanceUtilities.getOwningsOverview(context, LGA.getSingleton().getNames());
                    final OverviewItem owningsLastMonth = FinanceUtilities.getOwningsLastMonthOverview(context, LGA.getSingleton().getNames());

                    ((Activity) context).runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            items = new ArrayList<>();
                            items.add(ownings);
                            items.add(owningsLastMonth);
                            //items.add(overall);
                            //items.add(thisYear);
                            //items.add(lastYear);
                            notifyDataSetChanged();
                        }
                    });
                    break;
            }
        }
    }

    @Override
    public int getCount()
    {
        return items.size();
    }

    @Override
    public Object getItem(int position)
    {
        return items.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.overview_base, null);
        }

        TextView time = (TextView) convertView.findViewById(R.id.text_time);
        TextView all = (TextView) convertView.findViewById(R.id.text_all);
        TextView p1 = (TextView) convertView.findViewById(R.id.text_p1);
        TextView p2 = (TextView) convertView.findViewById(R.id.text_p2);
        synchronized (this)
        {
            OverviewItem item = (OverviewItem) getItem(position);
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMAN);
            DecimalFormat df = (DecimalFormat) nf;
            df.applyPattern("0.00");
            time.setText(FinanceUtilities.TimeFrameToString(context, item.getTime()));
            all.setText(df.format(item.getAll()));
            if (item.getAll() >= 0)
            {
                all.setTextColor(ContextCompat.getColor(context, R.color.positive_highlight));
            } else
            {
                all.setTextColor(ContextCompat.getColor(context, R.color.negative_highlight));
            }


            p1.setText(df.format(item.getP1()));
            if (item.getP1() >= 0)
            {
                p1.setTextColor(ContextCompat.getColor(context, R.color.positive_highlight));
            } else
            {
                p1.setTextColor(ContextCompat.getColor(context, R.color.negative_highlight));
            }
            p2.setText(df.format(item.getP2()));
            if (item.getP2() >= 0)
            {
                p2.setTextColor(ContextCompat.getColor(context, R.color.positive_highlight));
            } else
            {
                p2.setTextColor(ContextCompat.getColor(context, R.color.negative_highlight));
            }
        }
        return convertView;
    }

    public enum Mode
    {
        Expenses, StandingOrder, BankAccount
    }
}
