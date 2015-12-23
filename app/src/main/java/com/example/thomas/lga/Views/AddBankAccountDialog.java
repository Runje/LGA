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
import android.widget.TextView;
import android.widget.Toast;

import com.example.thomas.lga.Activities.Utilities;
import com.example.thomas.lga.Finances.BankAccount;
import com.example.thomas.lga.Installation;
import com.example.thomas.lga.LGA;
import com.example.thomas.lga.R;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by Thomas on 12.09.2015.
 */
public class AddBankAccountDialog
{
    private final Context context;
    private BankAccount bankAccount;
    private ConfirmListener confirmListener;
    private String LogKey = "ExpensesDialog";
    private boolean backToOverview = false;

    public AddBankAccountDialog(Context context)
    {
        this.context = context;
        bankAccount = new BankAccount(Installation.id(context));
    }

    public AddBankAccountDialog(Context context, BankAccount bankAccount)
    {
        // Show overview
        this(context);
        this.bankAccount = bankAccount;
    }

    public void show()
    {
        showOverview(true);
    }

    private void showMonthlyCosts()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_costs, null);
        final EditText editCosts = (EditText) layout.findViewById(R.id.edit_costs);
        editCosts.setText(Float.toString(bankAccount.getMonthly_costs()));
        Utilities.clickOn(editCosts);
        builder.setView(layout);
        builder.setTitle(R.string.monthly_costs);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                try
                {
                    float c = Float.parseFloat((editCosts).getText().toString());
                    bankAccount.setMonthly_costs(c);
                    if (backToOverview)
                    {
                        showOverview(false);
                    } else
                    {
                        showOwner();
                    }
                } catch (Exception e)
                {
                    Toast.makeText(context, R.string.empty_not_allowed, Toast.LENGTH_SHORT).show();
                    showMonthlyCosts();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void showBalance()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_costs, null);
        final EditText editCosts = (EditText) layout.findViewById(R.id.edit_costs);
        if (bankAccount.getBalance() != 0)
        {
            editCosts.setText(Float.toString(bankAccount.getBalance()));
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
                    bankAccount.setBalance(c);
                    if (backToOverview)
                    {
                        showOverview(false);
                    } else
                    {
                        showInterest();
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

    private void showInterest()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_costs, null);
        TextView sign = (TextView) layout.findViewById(R.id.text_sign);
        sign.setText("%");
        final EditText editCosts = (EditText) layout.findViewById(R.id.edit_costs);
        editCosts.setText(Float.toString(bankAccount.getInterest()));
        Utilities.clickOn(editCosts);
        builder.setView(layout);
        builder.setTitle(R.string.interest);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                try
                {
                    float c = Float.parseFloat((editCosts).getText().toString());
                    bankAccount.setInterest(c);
                    if (backToOverview)
                    {
                        showOverview(false);
                    } else
                    {
                        showMonthlyCosts();
                    }
                } catch (Exception e)
                {
                    Toast.makeText(context, R.string.empty_not_allowed, Toast.LENGTH_SHORT).show();
                    showInterest();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }


    private void showOwner()
    {
        int title = R.string.question_owner_bank_account;
        showListNames(title, new NameListener()
        {
            @Override
            public void onNameSelected(String name)
            {
                bankAccount.setOwner(name);
                if (backToOverview)
                {
                    showOverview(false);
                } else
                {
                    showOverview(false);
                }
            }
        });

    }

    private void showListNames(int title, final NameListener runnable)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_list_names, null);
        builder.setView(layout);
        builder.setTitle(title);
        builder.setNegativeButton(R.string.cancel, null);
        final Dialog dialog = builder.create();
        ListView listView = (ListView) layout.findViewById(R.id.listView_names);
        final List<String> names = LGA.getSingleton().getNames();
        listView.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, names));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                runnable.onNameSelected(names.get(position));
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void showLastDate()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_date, null);
        builder.setView(layout);
        builder.setTitle(R.string.last_update);
        builder.setNegativeButton(R.string.cancel, null);
        final DatePicker datePicker = (DatePicker) layout.findViewById(R.id.datePicker);
        if (bankAccount.getDate() != null)
        {
            Utilities.setDateToDatePicker(datePicker, bankAccount.getDate());
        }
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                DateTime date = Utilities.getDateFromDatePicker(datePicker);
                bankAccount.setDate(date);
                showOverview(false);
            }
        });

        builder.create().show();
    }


    private void showOverview(boolean editMode)
    {
        backToOverview = true;
        if (bankAccount.getDate() == null)
        {
            bankAccount.setDate(DateTime.now());
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
                confirmListener.onConfirm(bankAccount);
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
        final EditText editInterest = (EditText) layout.findViewById(R.id.edit_Interest);
        final EditText editMonthlyCost = (EditText) layout.findViewById(R.id.edit_MonthlyCosts);


        String name = editAccountName.getText().toString();
        bankAccount.setName(name);
        bankAccount.setBank(editBankName.getText().toString());
        float balance = Float.parseFloat(editBalance.getText().toString());
        float interest = Float.parseFloat(editInterest.getText().toString());
        float monthlyCosts = Float.parseFloat(editMonthlyCost.getText().toString());
        bankAccount.setBalance(balance);
        bankAccount.setMonthly_costs(monthlyCosts);
        bankAccount.setInterest(interest);
    }

    private void updateLayout(final View layout, final Dialog dialog, boolean editMode)
    {
        final EditText editBankName = (EditText) layout.findViewById(R.id.editBank);
        final EditText editAccountName = (EditText) layout.findViewById(R.id.edit_name);
        final EditText editBalance = (EditText) layout.findViewById(R.id.edit_balance);
        final EditText editOwner = (EditText) layout.findViewById(R.id.edit_owner);
        final EditText editInterest = (EditText) layout.findViewById(R.id.edit_Interest);
        final EditText editMonthlyCost = (EditText) layout.findViewById(R.id.edit_MonthlyCosts);
        final EditText editDate = (EditText) layout.findViewById(R.id.edit_last_date);

        if (editMode)
        {
            editAccountName.setEnabled(false);
            editBankName.setEnabled(false);
        }
        editOwner.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.cancel();
                updateFromLayout(layout);
                showOwner();
            }
        });

        editDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.cancel();
                updateFromLayout(layout);
                showLastDate();
            }
        });


        editBankName.setText(bankAccount.getBank());
        editBalance.setText(Float.toString(bankAccount.getBalance()));
        editInterest.setText(Float.toString(bankAccount.getInterest()));
        editMonthlyCost.setText(Float.toString(bankAccount.getMonthly_costs()));
        editOwner.setText(bankAccount.getOwner());
        editAccountName.setText(bankAccount.getName());
        editDate.setText(bankAccount.getDate().toString("dd.MM.yy"));

    }


    public void setConfirmListener(ConfirmListener confirmListener)
    {
        this.confirmListener = confirmListener;
    }

    public void start()
    {
        bankName();
    }

    private void name(int titleId, final NameListener runnable)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleId);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_name, null);
        final EditText editName = (EditText) layout.findViewById(R.id.edit_name);
        Utilities.clickOn(editName);
        builder.setView(layout);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String name = (editName).getText().toString();
                runnable.onNameSelected(name);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void bankName()
    {
        name(R.string.bank_name, new NameListener()
        {
            @Override
            public void onNameSelected(String name)
            {
                bankAccount.setBank(name);
                accountName();
            }
        });
    }

    private void accountName()
    {
        name(R.string.account_name, new NameListener()
        {
            @Override
            public void onNameSelected(String name)
            {
                bankAccount.setName(name);
                showBalance();
            }
        });
    }

    private interface NameListener
    {
        void onNameSelected(String name);
    }

    public interface ConfirmListener
    {
        void onConfirm(BankAccount bankAccount);
    }
}
