package com.example.thomas.lga.Activities;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.thomas.lga.R;
import com.example.thomas.lga.Views.Adapter.BalanceAdapter;

import java.util.Observer;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class BalanceFragment extends Fragment
{

    private BalanceAdapter adapter;
    private String LogKey = "ExpensesFragment";
    private Observer callback;

    public BalanceFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity context)
    {
        Log.d(LogKey, "On Attach Activity");
        super.onAttach(context);
        callback = (Observer) context;
    }

    @Override
    public void onAttach(Context context)
    {
        Log.d(LogKey, "On Attach Context");
        super.onAttach(context);
        callback = (Observer) context;
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
        View view = inflater.inflate(R.layout.balances_dialog, container, false);
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

    }


    public void update()
    {
        if (adapter != null)
        {
            adapter.updateBalance();
        }

        if (callback != null)
        {
            callback.update(null, null);
        }
    }


}
