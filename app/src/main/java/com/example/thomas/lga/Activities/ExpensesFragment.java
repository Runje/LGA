package com.example.thomas.lga.Activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.thomas.lga.Database.Filter;
import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.Finances.Expenses;
import com.example.thomas.lga.Finances.FinanceUtilities;
import com.example.thomas.lga.Finances.StandingOrder;
import com.example.thomas.lga.Installation;
import com.example.thomas.lga.R;
import com.example.thomas.lga.Views.Adapter.ExpensesDBAdapter;
import com.example.thomas.lga.Views.AddExpensesDialog;
import com.example.thomas.lga.Views.ChangeStandingOrderDialog;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ExpensesFragment extends Fragment implements ExpensesDBAdapter.ExpensesDeleteListener
{

    private ExpensesDBAdapter adapter;
    private String LogKey = "ExpensesFragment";
    private ExpensesListener callback;

    public ExpensesFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity context)
    {
        Log.d(LogKey, "On Attach Activity");
        super.onAttach(context);
        callback = (ExpensesListener) context;
    }

    @Override
    public void onAttach(Context context)
    {
        Log.d(LogKey, "On Attach Context");
        super.onAttach(context);
        callback = (ExpensesListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);
        init(view);
        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (adapter == null)
        {
            init(getView());
        }
    }

    private void init(View view)
    {
        ListView listView = (ListView) view.findViewById(R.id.list_expenses);
        adapter = new ExpensesDBAdapter(getActivity(), this);
        listView.setAdapter(adapter);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(LogKey, "Long click on " + position);
                final Expenses expenses = (Expenses) adapter.getItem(position);
                final AddExpensesDialog dialog = new AddExpensesDialog(getActivity(), expenses);
                dialog.setConfirmListener(new AddExpensesDialog.ConfirmListener()
                {
                    @Override
                    public void onConfirm(Expenses expenses)
                    {
                        SQLiteFinanceHandler.updateExpenses(getActivity(), expenses, Installation.id(getContext()));
                        updateFromExpenses();
                    }

                    @Override
                    public void onConfirm(StandingOrder standingOrder, Expenses expenses)
                    {
                        // shouldn't be called
                    }
                });

                if (expenses.isStandingOrder())
                {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setPositiveButton(R.string.change_standing_order, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            ChangeStandingOrderDialog changeStandingOrderDialog = new ChangeStandingOrderDialog(getContext(), SQLiteFinanceHandler.getStandingOrderFrom(getContext(), expenses));
                            changeStandingOrderDialog.setConfirmListener(new ChangeStandingOrderDialog.ConfirmListener()
                            {
                                @Override
                                public void onConfirm(StandingOrder standingOrder)
                                {
                                    SQLiteFinanceHandler.updateStandingOrder(getActivity(), standingOrder, Installation.id(getContext()));
                                    FinanceUtilities.synchronizeOrder(getContext(), standingOrder);
                                    updateFromExpenses();
                                }
                            });
                            changeStandingOrderDialog.show();
                        }
                    });
                    builder.setNeutralButton(R.string.change_only_entry, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog1, int which)
                        {
                            dialog.show(true);
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, null);

                    Dialog changeDialog = builder.create();
                    changeDialog.show();

                } else
                {
                    dialog.show(false);
                }
                return false;
            }
        });
    }


    public void updateExpenses()
    {
        if (adapter != null)
        {
            adapter.updateExpenses();
        }
    }

    public void updateFromExpenses()
    {
        updateExpenses();
        if (callback != null)
        {
            callback.updateFromExpenses();
        }
    }

    @Override
    public void onDelete()
    {
        updateFromExpenses();
    }

    public void setFilter(Filter filter)
    {
        adapter.setFilter(filter);
    }

    public interface ExpensesListener
    {
        void updateFromExpenses();
    }
}
