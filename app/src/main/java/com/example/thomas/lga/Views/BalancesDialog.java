package com.example.thomas.lga.Views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.Finances.Balance;
import com.example.thomas.lga.Finances.BankAccount;
import com.example.thomas.lga.Finances.FinanceUtilities;
import com.example.thomas.lga.Installation;
import com.example.thomas.lga.R;
import com.example.thomas.lga.Views.Adapter.BalanceAdapter;

/**
 * Created by Thomas on 02.11.2015.
 */
public class BalancesDialog
{
    private final Context context;
    private final Runnable onClose;
    private BankAccount account;
    private String LogKey = "BalancesDialog";

    public BalancesDialog(Context context, BankAccount account, Runnable onClose)
    {
        this.account = account;
        this.context = context;
        this.onClose = onClose;
    }


    public void show()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(account.toUserString());
        final View layout = LayoutInflater.from(context).inflate(R.layout.balances_dialog, null);
        builder.setView(layout);

        builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                onClose.run();
            }
        });

        ListView listView = (ListView) layout.findViewById(R.id.list_expenses);
        final BalanceAdapter adapter = new BalanceAdapter(context, account, new BalanceAdapter.BalancesListener()
        {
            @Override
            public void balanceDeleted()
            {

                FinanceUtilities.updateBankAccountFromBalances(context, account);
            }
        });
        listView.setAdapter(adapter);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(LogKey, "Long click on " + position);
                final Balance balance = (Balance) adapter.getItem(position);
                final AddBalanceDialog dialog = new AddBalanceDialog(context, balance);
                dialog.setConfirmListener(new AddBalanceDialog.ConfirmListener()
                {
                    @Override
                    public void onConfirm(Balance balance1)
                    {
                        SQLiteFinanceHandler.updateBalance(context, balance1, Installation.id(context));
                        adapter.updateBalance();
                        FinanceUtilities.updateBankAccountFromBalances(context, account);
                    }
                });

                dialog.show();
                return false;
            }
        });
        layout.findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AddBalanceDialog addDialog = new AddBalanceDialog(context, account);
                addDialog.setConfirmListener(new AddBalanceDialog.ConfirmListener()
                {
                    @Override
                    public void onConfirm(Balance balance)
                    {
                        SQLiteFinanceHandler.addBalance(context, balance);
                        adapter.updateBalance();

                        FinanceUtilities.updateBankAccountFromBalances(context, account);
                    }
                });
                addDialog.start();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }
}
