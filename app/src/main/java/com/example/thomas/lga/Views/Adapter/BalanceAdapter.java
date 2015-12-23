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

import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.Finances.Balance;
import com.example.thomas.lga.Finances.BankAccount;
import com.example.thomas.lga.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Thomas on 08.09.2015.
 */
public class BalanceAdapter extends BaseAdapter
{
    private final Context context;
    private final BankAccount account;
    private List<Balance> balances;


    public BalanceAdapter(Context context, BankAccount account)
    {
        this.context = context;
        this.account = account;
        updateBalance();
    }

    public void updateBalance()
    {
        List<Balance> balanceList = SQLiteFinanceHandler.getBalances(context, account);

        Collections.sort(balanceList, new Comparator<Balance>()
        {
            @Override
            public int compare(Balance lhs, Balance rhs)
            {
                return rhs.getDate().compareTo(lhs.getDate());
            }
        });
        setBalances(balanceList);
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
        return balances.size();
    }

    @Override
    public Object getItem(int position)
    {
        return balances.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return balances.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final Balance balance = balances.get(position);
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.balance_item, null);
            convertView.setLongClickable(true);
            convertView.setClickable(true);
        }

        final Button delete = (Button) convertView.findViewById(R.id.button_delete);

        delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                AlertDialog.Builder builder = new AlertDialog.Builder(delete.getContext());
                String name = Float.toString(balance.getBalance());
                builder.setMessage(context.getString(R.string.confirm_delete_bank_account, name));
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        SQLiteFinanceHandler.deleteBalance(delete.getContext(), balance);
                        updateBalance();
                    }
                });
                builder.setNegativeButton(R.string.no, null);
                builder.create().show();
            }
        });


        TextView textBalance = (TextView) convertView.findViewById(R.id.text_balance);
        TextView date = (TextView) convertView.findViewById(R.id.text_date);

        textBalance.setText(Float.toString(balance.getBalance()));
        if (balance.getBalance() >= 0)
        {
            textBalance.setTextColor(ContextCompat.getColor(context, R.color.positive_highlight));
        } else
        {
            textBalance.setTextColor(ContextCompat.getColor(context, R.color.negative_highlight));
        }

        date.setText(balance.getDate().toString("dd.MM.yy"));

        return convertView;
    }

    public void setBalances(List<Balance> balances)
    {
        this.balances = balances;
    }
}
