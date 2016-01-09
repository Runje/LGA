package com.example.thomas.lga.Views.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.thomas.lga.Activities.BankAccountFragment;
import com.example.thomas.lga.Activities.ExpensesFragment;
import com.example.thomas.lga.Activities.StandingOrderFragment;
import com.example.thomas.lga.Activities.StatisticsFragment_;
import com.example.thomas.lga.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 20.08.2015.
 */
public class FinanceFragmentPagerAdapter extends FragmentStatePagerAdapter
{
    private final Context context;
    List<Fragment> fragments;

    public FinanceFragmentPagerAdapter(Context context, FragmentManager fm)
    {
        super(fm);
        this.context = context;
        fragments = new ArrayList<>();
        fragments.add(new ExpensesFragment());

        fragments.add(new StandingOrderFragment());
        fragments.add(new BankAccountFragment());
        fragments.add(new StatisticsFragment_());

    }

    @Override
    public Fragment getItem(int position)
    {
        return fragments.get(position);
    }

    @Override
    public int getCount()
    {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return context.getResources().getString(R.string.expenses);
            case 1:
                return context.getResources().getString(R.string.standing_order);
            case 2:
                return context.getResources().getString(R.string.bank_account);
            case 3:
                return context.getResources().getString(R.string.statistics);
        }

        return super.getPageTitle(position);
    }

    public ExpensesFragment getExpensesFragment()
    {
        return (ExpensesFragment) fragments.get(0);
    }

    public StandingOrderFragment getStandingOrderFragment()
    {
        return (StandingOrderFragment) fragments.get(1);
    }

    public BankAccountFragment getBankAccountFragment()
    {
        return (BankAccountFragment) fragments.get(2);
    }


}
