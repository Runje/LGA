package com.example.thomas.lga.Views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.thomas.lga.Activities.Utilities;
import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.Finances.Balance;
import com.example.thomas.lga.Finances.BankAccount;
import com.example.thomas.lga.Installation;
import com.example.thomas.lga.R;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 12.09.2015.
 */
public class AddBalanceDialog
{
    private final Context context;
    private Balance balance;
    private ConfirmListener confirmListener;
    private String LogKey = "ExpensesDialog";
    private boolean backToOverview = false;

    public AddBalanceDialog(Context context, Balance balance)
    {
        // Show overview
        this.context = context;
        this.balance = balance;
    }

    public AddBalanceDialog(Context context, BankAccount account)
    {
        this.context = context;
        balance = new Balance(Installation.id(context));
        balance.setBankAccountName(account.getName());
        balance.setBankName(account.getBank());
    }

    public void show()
    {
        showOverview(true);
    }

    private void showBalance()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_costs, null);
        final EditText editCosts = (EditText) layout.findViewById(R.id.edit_costs);
        if (balance.getBalance() != 0)
        {
            editCosts.setText(Float.toString(balance.getBalance()));
        }
        Utilities.clickOn(editCosts);
        builder.setView(layout);
        builder.setTitle(R.string.balance);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                try
                {
                    float c = Float.parseFloat((editCosts).getText().toString());
                    balance.setBalance(c);
                    if (backToOverview)
                    {
                        showOverview(false);
                    } else
                    {
                        showDate();
                    }
                } catch (Exception e)
                {
                    Toast.makeText(context, R.string.empty_not_allowed, Toast.LENGTH_SHORT).show();
                    showBalance();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void showListBankAccounts()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_list_names, null);
        builder.setView(layout);
        builder.setTitle(R.string.bank_account);
        builder.setNegativeButton(R.string.cancel, null);
        final Dialog dialog = builder.create();
        ListView listView = (ListView) layout.findViewById(R.id.listView_names);
        final List<BankAccount> bankAccounts = SQLiteFinanceHandler.getBankAccounts(context);
        final List<String> names = new ArrayList<>();
        for (BankAccount bankAccount : bankAccounts)
        {
            names.add(bankAccount.toUserString());
        }

        listView.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, names));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                BankAccount bankAccount = bankAccounts.get(position);
                balance.setBankAccountName(bankAccount.getName());
                balance.setBankName(bankAccount.getBank());
                showBalance();
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void showDate()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_date, null);
        builder.setView(layout);
        builder.setTitle(R.string.date);
        builder.setNegativeButton(R.string.cancel, null);
        final DatePicker datePicker = (DatePicker) layout.findViewById(R.id.datePicker);
        if (balance.getDate() != null)
        {
            Utilities.setDateToDatePicker(datePicker, balance.getDate());
        }
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                DateTime date = Utilities.getDateFromDatePicker(datePicker);
                balance.setDate(date);
                showOverview(false);
            }
        });

        builder.create().show();
    }


    private void showOverview(boolean editMode)
    {
        backToOverview = true;
        if (balance.getDate() == null)
        {
            balance.setDate(DateTime.now());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.bank_account_dialog_overview, null);
        builder.setView(layout);

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                updateFromLayout(layout);
                confirmListener.onConfirm(balance);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        Dialog dialog = builder.create();
        dialog.show();

        updateLayout(layout, dialog, editMode);
    }

    private void updateFromLayout(View layout)
    {
        final EditText editBankName = (EditText) layout.findViewById(R.id.editBank);
        final EditText editAccountName = (EditText) layout.findViewById(R.id.edit_name);
        final EditText editBalance = (EditText) layout.findViewById(R.id.edit_balance);


        String name = editAccountName.getText().toString();
        balance.setBankAccountName(name);
        balance.setBankName(editBankName.getText().toString());
        float balance = Float.parseFloat(editBalance.getText().toString());
        this.balance.setBalance(balance);
    }

    private void updateLayout(final View layout, final Dialog dialog, boolean editMode)
    {
        final EditText editBankName = (EditText) layout.findViewById(R.id.editBank);
        final EditText editAccountName = (EditText) layout.findViewById(R.id.edit_name);
        final EditText editBalance = (EditText) layout.findViewById(R.id.edit_balance);
        final EditText editDate = (EditText) layout.findViewById(R.id.edit_last_date);

        final EditText editOwner = (EditText) layout.findViewById(R.id.edit_owner);
        final EditText editInterest = (EditText) layout.findViewById(R.id.edit_Interest);
        final EditText editMonthlyCost = (EditText) layout.findViewById(R.id.edit_MonthlyCosts);

        editOwner.setVisibility(View.GONE);
        editInterest.setVisibility(View.GONE);
        editMonthlyCost.setVisibility(View.GONE);

        if (editMode)
        {
            editAccountName.setEnabled(false);
            editBankName.setEnabled(false);
        }


        editDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.cancel();
                updateFromLayout(layout);
                showDate();
            }
        });


        editBankName.setText(balance.getBankName());
        editBalance.setText(Float.toString(balance.getBalance()));
        editAccountName.setText(balance.getBankAccountName());
        editDate.setText(balance.getDate().toString("dd.MM.yy"));
    }

    public void setConfirmListener(ConfirmListener confirmListener)
    {
        this.confirmListener = confirmListener;
    }

    public void start()
    {
        showBalance();
    }

    public interface ConfirmListener
    {
        void onConfirm(Balance balance);
    }
}
