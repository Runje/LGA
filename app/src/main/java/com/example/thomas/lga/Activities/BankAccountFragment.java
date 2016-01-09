package com.example.thomas.lga.Activities;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.Finances.BankAccount;
import com.example.thomas.lga.Finances.FinanceUtilities;
import com.example.thomas.lga.Installation;
import com.example.thomas.lga.R;
import com.example.thomas.lga.Views.Adapter.BankAccountAdapter;
import com.example.thomas.lga.Views.AddBankAccountDialog;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class BankAccountFragment extends Fragment implements BankAccountListener
{

    private BankAccountAdapter adapter;
    private String LogKey = "ExpensesFragment";
    private BankAccountListener callback;

    public BankAccountFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity context)
    {
        Log.d(LogKey, "On Attach Activity");
        super.onAttach(context);
        callback = (BankAccountListener) context;
    }

    @Override
    public void onAttach(Context context)
    {
        Log.d(LogKey, "On Attach Context");
        super.onAttach(context);
        callback = (BankAccountListener) context;
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
        View view = inflater.inflate(R.layout.fragment_bank_accounts, container, false);
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
        adapter = new BankAccountAdapter(getActivity(), this);
        listView.setAdapter(adapter);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(LogKey, "Long click on " + position);
                final BankAccount bankAccount = (BankAccount) adapter.getItem(position);
                final AddBankAccountDialog dialog = new AddBankAccountDialog(getActivity(), bankAccount);
                dialog.setConfirmListener(new AddBankAccountDialog.ConfirmListener()
                {
                    @Override
                    public void onConfirm(BankAccount bankAccount)
                    {
                        String myId = Installation.id(getContext());
                        SQLiteFinanceHandler.updateBankAccount(getActivity(), bankAccount, myId);
                        FinanceUtilities.updateBalancesFromAccount(getContext(), bankAccount);

                        updateFromBankAccounts();
                    }
                });

                dialog.show();
                return false;
            }
        });
    }


    public void updateBankAccounts()
    {
        if (adapter != null)
        {
            adapter.updateAccounts();
        }
    }

    @Override
    public void updateFromBankAccounts()
    {
        updateBankAccounts();
        if (callback != null)
        {
            callback.updateFromBankAccounts();
        }
    }


}

