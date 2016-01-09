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
import com.example.thomas.lga.Finances.FinanceUtilities;
import com.example.thomas.lga.Finances.StandingOrder;
import com.example.thomas.lga.Installation;
import com.example.thomas.lga.R;
import com.example.thomas.lga.Views.Adapter.ExpensesDBAdapter;
import com.example.thomas.lga.Views.Adapter.StandingOrderAdapter;
import com.example.thomas.lga.Views.ChangeStandingOrderDialog;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class StandingOrderFragment extends Fragment implements ExpensesDBAdapter.ExpensesDeleteListener
{

    private StandingOrderAdapter adapter;
    private String LogKey = "StandingOrderFragment";
    private StandingOrderListener callback;

    public StandingOrderFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity context)
    {
        Log.d(LogKey, "On Attach Activity");
        super.onAttach(context);
        callback = (StandingOrderListener) context;
    }

    @Override
    public void onAttach(Context context)
    {
        Log.d(LogKey, "On Attach Context");
        super.onAttach(context);
        callback = (StandingOrderListener) context;
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
        View view = inflater.inflate(R.layout.fragment_standing_order, container, false);

        ListView listView = (ListView) view.findViewById(R.id.list_standing_order);

        adapter = new StandingOrderAdapter(getActivity(), this);
        listView.setAdapter(adapter);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(LogKey, "Long click on " + position);
                final StandingOrder standingOrder = (StandingOrder) adapter.getItem(position);
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setPositiveButton(R.string.change_standing_order, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        ChangeStandingOrderDialog changeStandingOrderDialog = new ChangeStandingOrderDialog(getContext(), standingOrder);
                        changeStandingOrderDialog.setConfirmListener(new ChangeStandingOrderDialog.ConfirmListener()
                        {
                            @Override
                            public void onConfirm(StandingOrder standingOrder)
                            {
                                SQLiteFinanceHandler.updateStandingOrder(getActivity(), standingOrder, Installation.id(getContext()));
                                FinanceUtilities.synchronizeOrder(getContext(), standingOrder);
                                updateFromStandingOrders();
                            }
                        });
                        changeStandingOrderDialog.show();
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);

                Dialog changeDialog = builder.create();
                changeDialog.show();

                return false;
            }
        });
        return view;
    }


    public void updateStandingOrders()
    {
        if (adapter != null)
        {
            adapter.updateStandingOrders();
        }
    }

    public void updateFromStandingOrders()
    {
        updateStandingOrders();
        if (callback != null)
        {
            callback.updateFromStandingOrders();
        }
    }

    @Override
    public void onDelete()
    {
        callback.updateFromStandingOrders();
    }

    public void setFilter(Filter filter)
    {
        adapter.setFilter(filter);
    }

    public interface StandingOrderListener
    {
        void updateFromStandingOrders();
    }
}
