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
import com.example.thomas.lga.Finances.Expenses;
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

    public ConflictedDialog(Context context, List<Expenses> expenses)
    {
        this.context = context;
        this.expenses = expenses;
        this.index = 0;
        layout = LayoutInflater.from(context).inflate(R.layout.choose_conflicted, null);
        group = (RadioGroup) layout.findViewById(R.id.radiogroup);
        radioMine = (RadioButton) layout.findViewById(R.id.radio_mine);
        radioOther = (RadioButton) layout.findViewById(R.id.radio_other);
    }

    public void show()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.conflict);
        builder.setMessage(R.string.which_conflicted);

        updateExpenses();
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
                if (idx == 0)
                {
                    // use mine
                    Expenses mine = SQLiteFinanceHandler.getExpensesById(context, expenses.get(index).getId());
                    SQLiteFinanceHandler.updateExpenses(context, mine, Installation.id(context));
                } else
                {
                    //use theirs
                    SQLiteFinanceHandler.overwriteExpenses(context, expenses.get(index));
                }

                index++;
                if (index == expenses.size() - 1)
                {
                    next.setText("OK");
                }

                if (index == expenses.size())
                {
                    dialog.cancel();
                    return;
                }

                updateExpenses();
            }
        });

        dialog.show();
    }

    private void updateExpenses()
    {
        Expenses other = expenses.get(index);
        Expenses mine = SQLiteFinanceHandler.getExpensesById(context, other.getId());
        radioMine.setText(mine.toReadableString());
        radioOther.setText(other.toReadableString());
    }
}
