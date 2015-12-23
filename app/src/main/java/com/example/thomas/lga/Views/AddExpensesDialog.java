package com.example.thomas.lga.Views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thomas.lga.Activities.Utilities;
import com.example.thomas.lga.Finances.Expenses;
import com.example.thomas.lga.Finances.FinanceUtilities;
import com.example.thomas.lga.Finances.Frequency;
import com.example.thomas.lga.Finances.StandingOrder;
import com.example.thomas.lga.Installation;
import com.example.thomas.lga.LGA;
import com.example.thomas.lga.R;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by Thomas on 12.09.2015.
 */
public class AddExpensesDialog
{
    private final Context context;
    private Expenses expenses;
    private ConfirmListener confirmListener;
    private String LogKey = "ExpensesDialog";
    private boolean costs;
    private boolean isStandingOrder;
    private StandingOrder standingOrder;
    private boolean backToOverview = false;

    public AddExpensesDialog(Context context)
    {
        this.context = context;
        expenses = new Expenses(Installation.id(context));
        standingOrder = new StandingOrder();
    }

    public AddExpensesDialog(Context context, Expenses expenses)
    {
        // Show overview
        this(context);
        this.expenses = expenses;
        standingOrder = new StandingOrder();
    }

    public void show(boolean allowOnlyCostChange)
    {
        showOverview(false, allowOnlyCostChange);
    }

    private void incomeOrCosts()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_income, null);
        builder.setView(layout);
        final Dialog dialog = builder.create();
        builder.setNegativeButton(R.string.cancel, null);
        layout.findViewById(R.id.button_income).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                costs = false;
                if (backToOverview)
                {
                    showOverview(true, false);
                } else
                {
                    showCosts();
                }
                dialog.cancel();
            }
        });

        layout.findViewById(R.id.button_costs).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                costs = true;
                if (backToOverview)
                {
                    showOverview(true, false);
                } else
                {

                    showCosts();
                }

                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void showCosts()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_costs, null);
        final EditText editCosts = (EditText) layout.findViewById(R.id.edit_costs);
        Utilities.clickOn(editCosts);
        builder.setView(layout);
        builder.setTitle(costs ? R.string.costs : R.string.income);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                try
                {
                    float c = Float.parseFloat((editCosts).getText().toString());
                    expenses.setCosts(costs ? -1 * c : c);
                    standingOrder.setCosts(costs ? -1 * c : c);
                    if (backToOverview)
                    {
                        showOverview(true, false);
                    } else
                    {
                        showExecutor();
                    }
                } catch (Exception e)
                {
                    Toast.makeText(context, R.string.empty_not_allowed, Toast.LENGTH_SHORT).show();
                    showCosts();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void showExecutor()
    {
        int title = costs ? R.string.question_executor_costs : R.string.question_executor_income;
        showListNames(title, new OnNameClickListener()
        {
            @Override
            public void onNameClick(String name)
            {
                expenses.setWho(name);
                standingOrder.setWho(name);
                if (backToOverview)
                {
                    showOverview(true, false);
                } else
                {
                    showUser();
                }
            }
        });

    }

    private void showUser()
    {
        int title = costs ? R.string.question_user_costs : R.string.question_user_income;
        showListNames(title, new OnNameClickListener()
        {
            @Override
            public void onNameClick(String name)
            {
                expenses.setUser(name);
                standingOrder.setUser(name);

                if (backToOverview)
                {
                    showOverview(true, false);
                } else
                {
                    showCategories();
                }
            }
        });

    }

    private void showListNames(int title, final OnNameClickListener runnable)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_list_names, null);
        builder.setView(layout);
        builder.setTitle(title);
        builder.setNegativeButton(R.string.cancel, null);
        final Dialog dialog = builder.create();
        ListView listView = (ListView) layout.findViewById(R.id.listView_names);
        final List<String> names = LGA.getSingleton().getNames();
        listView.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, names));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                runnable.onNameClick(names.get(position));
                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void showCategories()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_list_names, null);
        layout.findViewById(R.id.add_layout).setVisibility(View.VISIBLE);
        Button buttonAdd = (Button) layout.findViewById(R.id.button_add);
        final EditText CategoryToAdd = (EditText) layout.findViewById(R.id.edit_category);

        builder.setView(layout);
        builder.setTitle(R.string.category);
        builder.setNegativeButton(R.string.cancel, null);
        final Dialog dialog = builder.create();
        final ListView listView = (ListView) layout.findViewById(R.id.listView_names);
        final List<String> categories = FinanceUtilities.getCategorys(context);
        final ArrayAdapter adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, categories);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.question_delete_category);
                builder.setNegativeButton(R.string.cancel, null);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        FinanceUtilities.deleteCategory(context, (String) adapter.getItem(position));
                        adapter.clear();
                        adapter.addAll(FinanceUtilities.getCategorys(context));
                        adapter.notifyDataSetChanged();
                    }
                });
                builder.create().show();
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                expenses.setCategory(categories.get(position));
                standingOrder.setCategory(categories.get(position));
                dialog.cancel();
                if (backToOverview)
                {
                    showOverview(true, false);
                } else
                {
                    showStandingOrder();
                }
            }
        });
        buttonAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String newCategory = CategoryToAdd.getText().toString();
                List<String> categorys = FinanceUtilities.getCategorys(context);
                boolean exists = false;
                for (String category : categorys)
                {
                    if (category.equals(newCategory))
                    {
                        Toast.makeText(context, R.string.category_already_exists, Toast.LENGTH_SHORT).show();
                        exists = true;
                    }
                }

                if (!exists)
                {
                    FinanceUtilities.addNewCategory(context, newCategory);
                    adapter.clear();
                    adapter.addAll(FinanceUtilities.getCategorys(context));
                }
            }
        });
        dialog.show();
    }

    private void showStandingOrder()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_income, null);
        builder.setView(layout);
        final Dialog dialog = builder.create();
        builder.setNegativeButton(R.string.cancel, null);
        final Button stOr = (Button) layout.findViewById(R.id.button_income);
        stOr.setText(R.string.standing_order);

        final Button exp = (Button) layout.findViewById(R.id.button_costs);
        exp.setText(R.string.once);
        exp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                isStandingOrder = false;
                expenses.setStandingOrder(false);
                if (backToOverview)
                {
                    showOverview(true, false);
                } else
                {
                    showFirstDate();
                }
                dialog.cancel();
            }
        });

        stOr.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                isStandingOrder = true;
                expenses.setStandingOrder(true);
                if (backToOverview)
                {
                    showOverview(true, false);
                } else
                {
                    showFirstDate();
                }

                dialog.cancel();
            }
        });

        dialog.show();
    }

    private void showFirstDate()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_date, null);
        final DatePicker datePicker = (DatePicker) layout.findViewById(R.id.datePicker);
        if (expenses.getDate() != null)
        {
            Utilities.setDateToDatePicker(datePicker, expenses.getDate());
        }
        builder.setView(layout);
        builder.setTitle(isStandingOrder ? R.string.firstDate : R.string.date);
        builder.setNegativeButton(R.string.cancel, null);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                DateTime date = Utilities.getDateFromDatePicker(datePicker);
                expenses.setDate(date);
                standingOrder.setFirstDate(date);
                if (!isStandingOrder)
                {
                    showOverview(true, false);
                } else
                {
                    if (backToOverview)
                    {
                        showOverview(true, false);
                    } else
                    {
                        showFrequency();
                    }
                }
            }
        });

        builder.create().show();
    }

    private void showFrequency()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_frequency, null);
        final NumberPicker numberPicker = (NumberPicker) layout.findViewById(R.id.numberPicker);
        final RadioGroup radioGroup = (RadioGroup) layout.findViewById(R.id.radiogroup_frequency);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(12);
        numberPicker.setValue(standingOrder.getNumber() == 0 ? 1 : standingOrder.getNumber());

        if (standingOrder.getFrequency() != null)
        {
            int id = Frequency.FrequencyToIndex(standingOrder.getFrequency());
            ((RadioButton) radioGroup.getChildAt(id)).setChecked(true);
        }
        builder.setView(layout);
        builder.setTitle(R.string.frequency);
        builder.setNegativeButton(R.string.cancel, null);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                View radioButton = radioGroup.findViewById(radioButtonID);
                int idx = radioGroup.indexOfChild(radioButton);

                Frequency frequency = Frequency.indexToFrequency(idx);
                standingOrder.setFrequency(frequency);
                standingOrder.setNumber(numberPicker.getValue());
                if (backToOverview)
                {
                    showOverview(true, false);
                } else
                {
                    showLastDate();
                }
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
        if (standingOrder.getLastDate() != StandingOrder.Unlimited && standingOrder.getLastDate() != null)
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
                showOverview(true, false);
            }
        });

        builder.setNeutralButton(R.string.unlimited, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                standingOrder.setLastDate(StandingOrder.Unlimited);
                showOverview(true, false);
            }
        });

        builder.create().show();
    }


    private void showOverview(boolean showStandingOrder, boolean allowOnlyCostChange)
    {
        backToOverview = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_overview, null);
        builder.setView(layout);

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                updateFromLayout(layout);
                if (isStandingOrder)
                {
                    // TODO check if standing order already exists (Name)
                    // TODO check if last date is not before first date
                    confirmListener.onConfirm(standingOrder, expenses);
                } else
                {
                    confirmListener.onConfirm(expenses);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        Dialog dialog = builder.create();
        dialog.show();

        updateLayout(layout, dialog, showStandingOrder, allowOnlyCostChange);
    }

    private void updateFromLayout(View layout)
    {
        final EditText editName = (EditText) layout.findViewById(R.id.edit_name);
        final EditText editCosts = (EditText) layout.findViewById(R.id.edit_costs);

        String name = editName.getText().toString();
        expenses.setName(name);
        standingOrder.setName(name);
        float costs = Float.parseFloat(editCosts.getText().toString());
        expenses.setCosts(costs);
        standingOrder.setCosts(costs);
    }

    private void updateLayout(final View layout, final Dialog dialog, final boolean showStandingOrder, final boolean allowOnlyCostChange)
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

        if (allowOnlyCostChange)
        {
            editName.setEnabled(false);
            editExecutor.setEnabled(false);
            editUser.setEnabled(false);
            editCategory.setEnabled(false);
            checkBoxStandingOrder.setEnabled(false);
            editFirstDate.setEnabled(false);
        }
        editExecutor.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.cancel();
                updateFromLayout(layout);
                showExecutor();
            }
        });

        editUser.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.cancel();
                updateFromLayout(layout);
                showUser();
            }
        });

        editCategory.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.cancel();
                updateFromLayout(layout);
                showCategories();
            }
        });

        checkBoxStandingOrder.setVisibility(showStandingOrder ? View.VISIBLE : View.GONE);
        View textStandingOrder = layout.findViewById(R.id.text_standing_order);
        textStandingOrder.setVisibility(showStandingOrder ? View.VISIBLE : View.GONE);
        checkBoxStandingOrder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                isStandingOrder = isChecked;
                expenses.setStandingOrder(isChecked);
                updateLayout(layout, dialog, showStandingOrder, allowOnlyCostChange);
            }
        });

        editFirstDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.cancel();
                updateFromLayout(layout);
                showFirstDate();
            }
        });

        editLastDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.cancel();
                updateFromLayout(layout);
                showLastDate();
            }
        });

        editFrequency.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.cancel();
                updateFromLayout(layout);
                showFrequency();
            }
        });

        editName.setText(expenses.getName());
        editCosts.setText(Float.toString(expenses.getCosts()));
        editExecutor.setText(expenses.getWho());
        editUser.setText(expenses.getUser());
        editCategory.setText(expenses.getCategory().toString());
        checkBoxStandingOrder.setChecked(isStandingOrder);

        View textFrequency = layout.findViewById(R.id.text_frequency);
        TextView textDate = (TextView) layout.findViewById(R.id.text_date);
        View textLastDate = layout.findViewById(R.id.text_last_date);

        if (!isStandingOrder)
        {
            editLastDate.setVisibility(View.GONE);
            editFrequency.setVisibility(View.GONE);
            textFrequency.setVisibility(View.GONE);
            textLastDate.setVisibility(View.GONE);
            textDate.setText(R.string.date);
            editFirstDate.setText(expenses.getDate().toString("dd.MM.yy"));
        } else
        {
            editLastDate.setVisibility(View.VISIBLE);
            editFrequency.setVisibility(View.VISIBLE);
            textFrequency.setVisibility(View.VISIBLE);
            textLastDate.setVisibility(View.VISIBLE);
            textDate.setText(R.string.firstDate);
            editFirstDate.setText(standingOrder.getFirstDate().toString("dd.MM.yy"));

            String lastDate = standingOrder.getLastDate().equals(StandingOrder.Unlimited) ? context.getResources().getString(R.string.unlimited) : standingOrder.getLastDate().toString("dd.MM.yy");
            editLastDate.setText(lastDate);
            String frequency = standingOrder.getNumber() == 1 ? FinanceUtilities.FrequencyToString(context, standingOrder.getFrequency()) : standingOrder.getNumber() + "-" + FinanceUtilities.FrequencyToString(context, standingOrder.getFrequency());
            editFrequency.setText(frequency);

        }

    }


    public void setConfirmListener(ConfirmListener confirmListener)
    {
        this.confirmListener = confirmListener;
    }

    public void start()
    {
        transactionName();
    }

    private void transactionName()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.transaction_name);
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
                expenses.setName(name);
                standingOrder.setName(name);
                incomeOrCosts();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    public interface ConfirmListener
    {
        void onConfirm(Expenses expenses);

        void onConfirm(StandingOrder standingOrder, Expenses expenses);
    }

    private interface OnNameClickListener
    {
        void onNameClick(String name);
    }

}
