package com.example.thomas.lga.Views.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.thomas.lga.Database.Filter;
import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.Finances.StandingOrder;
import com.example.thomas.lga.LGA;
import com.example.thomas.lga.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Thomas on 08.09.2015.
 */
public class StandingOrderAdapter extends BaseAdapter
{
    private final Context context;
    private ExpensesDBAdapter.ExpensesDeleteListener callback;
    private List<StandingOrder> standingOrders;
    private Filter filter;


    public StandingOrderAdapter(Context context, ExpensesDBAdapter.ExpensesDeleteListener listener)
    {
        this.context = context;
        this.callback = listener;
        updateStandingOrders();
    }

    public void updateStandingOrders()
    {
        List<StandingOrder> standingOrderList = SQLiteFinanceHandler.getStandingOrders(context, filter);

        Collections.sort(standingOrderList, new Comparator<StandingOrder>()
        {
            @Override
            public int compare(StandingOrder lhs, StandingOrder rhs)
            {
                return rhs.getFirstDate().compareTo(lhs.getFirstDate());
            }
        });
        setStandingOrders(standingOrderList);
        ((Activity) context).runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount()
    {
        return standingOrders.size();
    }

    @Override
    public Object getItem(int position)
    {
        return standingOrders.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return standingOrders.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final StandingOrder standingOrder = standingOrders.get(position);
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expenses_item, null);
            convertView.setLongClickable(true);
        }

        final Button delete = (Button) convertView.findViewById(R.id.button_delete);

        delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(delete.getContext());
                int stringId;
                stringId = R.string.confirm_standing_order_delete;

                builder.setMessage(stringId);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        SQLiteFinanceHandler.deleteStandingOrder(context, standingOrder);
                        updateStandingOrders();
                        callback.onDelete();
                    }
                });
                builder.setNegativeButton(R.string.no, null);
                builder.create().show();
            }
        });


        TextView person = (TextView) convertView.findViewById(R.id.text_who);
        TextView name = (TextView) convertView.findViewById(R.id.text_name);
        TextView category = (TextView) convertView.findViewById(R.id.text_category);
        TextView costs = (TextView) convertView.findViewById(R.id.text_costs);
        TextView date = (TextView) convertView.findViewById(R.id.text_date);
        TextView user = (TextView) convertView.findViewById(R.id.text_user);

        person.setText(LGA.getSingleton().getAbbreviation(standingOrder.getWho()));
        name.setText(standingOrder.getName());
        category.setText(standingOrder.getCategory().toString());
        costs.setText(Float.toString(standingOrder.getCosts()));
        if (standingOrder.getCosts() >= 0)
        {
            costs.setTextColor(ContextCompat.getColor(context, R.color.positive_highlight));
        } else
        {
            costs.setTextColor(ContextCompat.getColor(context, R.color.negative_highlight));
        }

        date.setText(standingOrder.getFirstDate().toString("dd.MM.yy"));
        user.setText(LGA.getSingleton().getAbbreviation(standingOrder.getUser()));

        return convertView;
    }

    public void setStandingOrders(List<StandingOrder> standingOrders)
    {
        this.standingOrders = standingOrders;
    }

    public Filter getFilter()
    {
        return filter;
    }

    public void setFilter(Filter filter)
    {
        this.filter = filter;
        updateStandingOrders();
    }
}
