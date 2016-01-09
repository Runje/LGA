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

import com.example.thomas.lga.Activities.BankAccountListener;
import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.Finances.Balance;
import com.example.thomas.lga.Finances.BankAccount;
import com.example.thomas.lga.LGA;
import com.example.thomas.lga.R;
import com.example.thomas.lga.Views.BalancesDialog;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Thomas on 08.09.2015.
 */
public class BankAccountAdapter extends BaseAdapter
{
    private final Context context;
    private final BankAccountListener listener;
    private List<BankAccount> accounts;


    public BankAccountAdapter(Context context, BankAccountListener listener)
    {
        this.context = context;
        this.listener = listener;
        updateAccounts();
    }

    public void updateAccounts()
    {
        List<BankAccount> bankAccountList = SQLiteFinanceHandler.getBankAccounts(context);

        Collections.sort(bankAccountList, new Comparator<BankAccount>()
        {
            @Override
            public int compare(BankAccount lhs, BankAccount rhs)
            {
                return rhs.getDate().compareTo(lhs.getDate());
            }
        });
        setAccounts(bankAccountList);
        updateBalancesOfAccounts();
        ((Activity) context).runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                notifyDataSetChanged();
            }
        });
    }

    private void updateBalancesOfAccounts()
    {
        List<Balance> balances = SQLiteFinanceHandler.getBalances(context);
        for (Balance balance : balances)
        {
            for (BankAccount bankAccount : accounts)
            {
                if (balance.getDate().isAfter(bankAccount.getDate()) && balance.getBankName().equals(bankAccount.getBank()) && balance.getBankAccountName().equals(bankAccount.getName()))
                {
                    bankAccount.setDate(balance.getDate());
                    bankAccount.setBalance(balance.getBalance());
                }
            }
        }

        for (BankAccount bankAccount : accounts)
        {
            SQLiteFinanceHandler.overwriteBankAccount(context, bankAccount);
        }
    }

    @Override
    public int getCount()
    {
        return accounts.size();
    }

    @Override
    public Object getItem(int position)
    {
        return accounts.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return accounts.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final BankAccount bankAccount = accounts.get(position);
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.bank_account_item, null);
            convertView.setLongClickable(true);
        }

        final Button delete = (Button) convertView.findViewById(R.id.button_delete);

        delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                AlertDialog.Builder builder = new AlertDialog.Builder(delete.getContext());
                String name = bankAccount.getName();
                builder.setMessage(context.getString(R.string.confirm_delete_bank_account, name));
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        SQLiteFinanceHandler.deleteBankAccount(delete.getContext(), bankAccount);
                        // TODO: delete its balances
                        updateAccounts();
                        listener.updateFromBankAccounts();
                    }
                });
                builder.setNegativeButton(R.string.no, null);
                builder.create().show();
            }
        });

        final Button addBalance = (Button) convertView.findViewById(R.id.button_add_balance);
        addBalance.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BalancesDialog dialog = new BalancesDialog(context, bankAccount, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateAccounts();
                        listener.updateFromBankAccounts();

                    }
                });
                dialog.show();
            }
        });

        TextView name = (TextView) convertView.findViewById(R.id.text_name);
        TextView owner = (TextView) convertView.findViewById(R.id.text_owner);
        TextView balance = (TextView) convertView.findViewById(R.id.text_balance);
        TextView date = (TextView) convertView.findViewById(R.id.text_last_update);
        TextView bankName = (TextView) convertView.findViewById(R.id.text_bankname);

        bankName.setText(bankAccount.getBank());
        name.setText(bankAccount.getName());
        owner.setText(LGA.getSingleton().getAbbreviation(bankAccount.getOwner()));
        balance.setText(Float.toString(bankAccount.getBalance()));
        if (bankAccount.getBalance() >= 0)
        {
            balance.setTextColor(ContextCompat.getColor(context, R.color.positive_highlight));
        } else
        {
            balance.setTextColor(ContextCompat.getColor(context, R.color.negative_highlight));
        }

        date.setText(bankAccount.getDate().toString("dd.MM.yy"));

        return convertView;
    }

    public void setAccounts(List<BankAccount> accounts)
    {
        this.accounts = accounts;
    }
}
