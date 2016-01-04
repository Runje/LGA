package com.example.thomas.lga.Views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.Finances.Balance;
import com.example.thomas.lga.Finances.BankAccount;
import com.example.thomas.lga.Finances.Expenses;
import com.example.thomas.lga.Finances.StandingOrder;
import com.example.thomas.lga.Installation;
import com.example.thomas.lga.R;

import java.util.List;

/**
 * Created by Thomas on 22.12.2015.
 */
public class ConflictedDialog
{
    private final RadioGroup group;
    private final RadioButton radioOther;
    private final RadioButton radioMine;
    private final View layout;
    private Context context;
    private int index;
    private List<Expenses> expenses;
    private List<StandingOrder> standingOrders;
    private List<BankAccount> bankAccounts;
    private int item;
    private List<Balance> balances;

    public ConflictedDialog(Context context)
    {
        this.context = context;
        this.index = 0;
        layout = LayoutInflater.from(context).inflate(R.layout.choose_conflicted, null);
        group = (RadioGroup) layout.findViewById(R.id.radiogroup);
        radioMine = (RadioButton) layout.findViewById(R.id.radio_mine);
        radioOther = (RadioButton) layout.findViewById(R.id.radio_other);
    }

    public List<Balance> getBalances()
    {
        return balances;
    }

    public void setBalances(List<Balance> balances)
    {
        this.balances = balances;
    }

    public List<StandingOrder> getStandingOrders()
    {
        return standingOrders;
    }

    public void setStandingOrders(List<StandingOrder> standingOrders)
    {
        this.standingOrders = standingOrders;
    }

    public List<BankAccount> getBankAccounts()
    {
        return bankAccounts;
    }

    public void setBankAccounts(List<BankAccount> bankAccounts)
    {
        this.bankAccounts = bankAccounts;
    }

    public List<Expenses> getExpenses()
    {
        return expenses;
    }

    public void setExpenses(List<Expenses> expenses)
    {
        this.expenses = expenses;
    }

    public void show()
    {
        if (expenses.size() == 0 &&
                standingOrders.size() == 0
                && bankAccounts.size() == 0 &&
                balances.size() == 0)
        {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.conflict);
        builder.setMessage(R.string.which_conflicted);
        updateItem();
        updateText();
        final Button next = (Button) layout.findViewById(R.id.button_next);
        builder.setView(layout);
        final Dialog dialog = builder.create();
        next.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int radioButtonID = group.getCheckedRadioButtonId();
                View radioButton = group.findViewById(radioButtonID);
                int idx = group.indexOfChild(radioButton);
                // TODO: use item as index for Expenses, StandingOrder, ....
                if (idx == 0)
                {
                    // use mine
                    switch (item)
                    {
                        case 0:
                            Expenses mine = SQLiteFinanceHandler.getExpensesById(context, expenses.get(index).getId());
                            SQLiteFinanceHandler.updateExpenses(context, mine, Installation.id(context));
                            break;
                        case 1:
                            StandingOrder mineStandingOrder = SQLiteFinanceHandler.getStandingOrderById(context, standingOrders.get(index).getId());
                            SQLiteFinanceHandler.updateStandingOrder(context, mineStandingOrder, Installation.id(context));
                            break;
                        case 2:
                            BankAccount mineBankAccount = SQLiteFinanceHandler.getBankAccountById(context, bankAccounts.get(index).getId());
                            SQLiteFinanceHandler.updateBankAccount(context, mineBankAccount, Installation.id(context));
                            break;
                        case 3:
                            Balance mineBalance = SQLiteFinanceHandler.getBalanceById(context, balances.get(index).getId());
                            SQLiteFinanceHandler.updateBalance(context, mineBalance, Installation.id(context));
                            break;
                    }
                } else
                {
                    //use theirs
                    switch (item)
                    {
                        case 0:
                            SQLiteFinanceHandler.overwriteExpenses(context, expenses.get(index));
                            break;
                        case 1:
                            SQLiteFinanceHandler.overwriteStandingOrder(context, standingOrders.get(index));
                            break;
                        case 2:
                            SQLiteFinanceHandler.overwriteBankAccount(context, bankAccounts.get(index));
                            break;
                        case 3:
                            SQLiteFinanceHandler.overwriteBalance(context, balances.get(index));
                            break;
                    }
                }

                index++;
                updateItem();
                if (item == 3 && index == balances.size() - 1)
                {
                    next.setText("OK");
                }

                if (item == 4)
                {
                    dialog.cancel();
                    return;
                }

                updateText();
            }
        });

        dialog.show();
    }

    private void updateItem()
    {
        while (true)
        {
            switch (item)
            {
                case 0:
                    if (index >= expenses.size())
                    {
                        item++;
                        index = 0;
                        break;
                    } else
                    {
                        return;
                    }
                case 1:
                    if (index >= standingOrders.size())
                    {
                        item++;
                        index = 0;
                        break;
                    } else
                    {
                        return;
                    }
                case 2:
                    if (index >= bankAccounts.size())
                    {
                        item++;
                        index = 0;
                        break;
                    } else
                    {
                        return;
                    }
                case 3:
                    if (index >= balances.size())
                    {
                        item++;
                        index = 0;
                        return;
                    } else
                    {
                        return;
                    }
            }
        }
    }

    private void updateText()
    {
        String mineText = "";
        String otherText = "";
        switch (item)
        {
            case 0:
                Expenses other = expenses.get(index);
                Expenses mine = SQLiteFinanceHandler.getExpensesById(context, other.getId());
                mineText = mine.toReadableString();
                otherText = other.toReadableString();
                break;
            case 1:
                StandingOrder otherStandingOrder = standingOrders.get(index);
                StandingOrder mineStandingOrder = SQLiteFinanceHandler.getStandingOrderById(context, otherStandingOrder.getId());
                mineText = mineStandingOrder.toString();
                otherText = otherStandingOrder.toString();
                break;
            case 2:
                BankAccount otherBankAccount = bankAccounts.get(index);
                BankAccount mineBankAccount = SQLiteFinanceHandler.getBankAccountById(context, otherBankAccount.getId());
                mineText = mineBankAccount.toString();
                otherText = otherBankAccount.toString();
                break;
            case 3:
                Balance otherBalance = balances.get(index);
                Balance mineBalance = SQLiteFinanceHandler.getBalanceById(context, otherBalance.getId());
                mineText = mineBalance.toString();
                otherText = otherBalance.toString();
                break;
        }

        radioMine.setText(mineText);
        radioOther.setText(otherText);
    }
}
