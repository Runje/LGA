package com.example.thomas.lga.Views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.example.thomas.lga.Activities.Utilities;
import com.example.thomas.lga.Finances.FinanceUtilities;
import com.example.thomas.lga.Finances.StandingOrder;
import com.example.thomas.lga.R;

import org.joda.time.DateTime;


/**
 * Created by Thomas on 12.09.2015.
 */
public class ChangeStandingOrderDialog
{
    private final Context context;
    private ConfirmListener confirmListener;
    private String LogKey = "ChangeStandingOrderDialog";
    private StandingOrder standingOrder;

    public ChangeStandingOrderDialog(Context context, StandingOrder order)
    {
        this.context = context;
        standingOrder = order;

    }

    public void show()
    {
        showOverview();
    }

    private void showFirstDate()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_date, null);
        final DatePicker datePicker = (DatePicker) layout.findViewById(R.id.datePicker);
        builder.setView(layout);
        builder.setTitle(R.string.firstDate);
        builder.setNegativeButton(R.string.cancel, null);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                DateTime date = Utilities.getDateFromDatePicker(datePicker);
                standingOrder.setFirstDate(date);
                showOverview();
            }
        });

        builder.create().show();
    }

    private void showLastDate()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_date, null);
        builder.setView(layout);
        builder.setTitle(R.string.last_date);
        builder.setNegativeButton(R.string.cancel, null);
        final DatePicker datePicker = (DatePicker) layout.findViewById(R.id.datePicker);
        if (standingOrder.getLastDate() != null)
        {
            Utilities.setDateToDatePicker(datePicker, standingOrder.getLastDate());
        }
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                DateTime date = Utilities.getDateFromDatePicker(datePicker);
                standingOrder.setLastDate(date);
                showOverview();
            }
        });

        builder.setNeutralButton(R.string.unlimited, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                standingOrder.setLastDate(StandingOrder.Unlimited);
                showOverview();
            }
        });

        builder.create().show();
    }


    private void showOverview()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_overview, null);
        builder.setView(layout);

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                updateFromLayout(layout);
                confirmListener.onConfirm(standingOrder);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        Dialog dialog = builder.create();
        dialog.show();

        updateLayout(layout, dialog);
    }

    private void updateFromLayout(View layout)
    {
        final EditText editName = (EditText) layout.findViewById(R.id.edit_name);
        final EditText editCosts = (EditText) layout.findViewById(R.id.edit_costs);

        String name = editName.getText().toString();
        standingOrder.setName(name);
        float costs = Float.parseFloat(editCosts.getText().toString());
        standingOrder.setCosts(costs);
    }

    private void updateLayout(final View layout, final Dialog dialog)
    {
        final EditText editName = (EditText) layout.findViewById(R.id.edit_name);
        final EditText editCosts = (EditText) layout.findViewById(R.id.edit_costs);
        final EditText editExecutor = (EditText) layout.findViewById(R.id.edit_executor);
        final EditText editUser = (EditText) layout.findViewById(R.id.edit_user);
        final EditText editCategory = (EditText) layout.findViewById(R.id.edit_category);
        final CheckBox checkBoxStandingOrder = (CheckBox) layout.findViewById(R.id.checkBox_standing_order);
        final EditText editFirstDate = (EditText) layout.findViewById(R.id.edit_first_date);
        final EditText editLastDate = (EditText) layout.findViewById(R.id.edit_last_date);
        final EditText editFrequency = (EditText) layout.findViewById(R.id.edit_frequency);

        editExecutor.setEnabled(false);
        editName.setEnabled(false);

        editUser.setEnabled(false);

        editCategory.setEnabled(false);

        checkBoxStandingOrder.setVisibility(View.GONE);

        View textStandingOrder = layout.findViewById(R.id.text_standing_order);
        textStandingOrder.setVisibility(View.GONE);

        editFirstDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.cancel();
                showFirstDate();
            }
        });

        editLastDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.cancel();
                showLastDate();
            }
        });

        editFrequency.setEnabled(false);

        editName.setText(standingOrder.getName());
        editCosts.setText(Float.toString(standingOrder.getCosts()));
        editExecutor.setText(standingOrder.getWho());
        editUser.setText(standingOrder.getUser());
        editCategory.setText(standingOrder.getCategory().toString());

        View textFrequency = layout.findViewById(R.id.text_frequency);
        TextView textDate = (TextView) layout.findViewById(R.id.text_date);
        View textLastDate = layout.findViewById(R.id.text_last_date);

        editLastDate.setVisibility(View.VISIBLE);
        editFrequency.setVisibility(View.VISIBLE);
        textFrequency.setVisibility(View.VISIBLE);
        textLastDate.setVisibility(View.VISIBLE);
        textDate.setText(R.string.firstDate);
        editFirstDate.setText(standingOrder.getFirstDate().toString("dd.MM.yy"));

        String lastDate = standingOrder.getLastDate().equals(StandingOrder.Unlimited) ? context.getResources().getString(R.string.unlimited) : standingOrder.getLastDate().toString("dd.MM.yy");
        editLastDate.setText(lastDate);
        String frequency = standingOrder.getNumber() == 1 ? standingOrder.getFrequency().toString() : standingOrder.getNumber() + "-" + FinanceUtilities.FrequencyToString(context, standingOrder.getFrequency());
        editFrequency.setText(frequency);


    }


    public void setConfirmListener(ConfirmListener confirmListener)
    {
        this.confirmListener = confirmListener;
    }

    public interface ConfirmListener
    {
        void onConfirm(StandingOrder standingOrder);
    }

}
