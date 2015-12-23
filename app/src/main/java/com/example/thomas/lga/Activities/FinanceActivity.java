package com.example.thomas.lga.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.devpaul.filepickerlibrary.FilePickerActivity;
import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.Finances.Balance;
import com.example.thomas.lga.Finances.BankAccount;
import com.example.thomas.lga.Finances.Expenses;
import com.example.thomas.lga.Finances.FinanceUtilities;
import com.example.thomas.lga.Finances.StandingOrder;
import com.example.thomas.lga.Installation;
import com.example.thomas.lga.LGA;
import com.example.thomas.lga.R;
import com.example.thomas.lga.Views.Adapter.FinanceFragmentPagerAdapter;
import com.example.thomas.lga.Views.Adapter.OverviewAdapter;
import com.example.thomas.lga.Views.AddBankAccountDialog;
import com.example.thomas.lga.Views.AddExpensesDialog;
import com.example.thomas.lga.Views.PINDialog;

import org.joda.time.DateTime;

import java.io.File;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Thomas on 06.09.2015.
 */
public class FinanceActivity extends AppCompatActivity implements Observer
{
    public static final String DATABASE_NAME = "DB_NAME";
    private String LogKey = "FinanceActivity";
    private OverviewAdapter adapter;
    private FinanceFragmentPagerAdapter pageAdapter;
    private ViewPager pager;
    private int page;
    private boolean dialogIsVisible;
    private ListView listView;
    private boolean overViewVisible = true;

    @Override
    protected void onResume()
    {
        super.onResume();
        LGA.getSingleton().init(this);
        final View all = findViewById(R.id.activity_finance);
        all.setVisibility(View.INVISIBLE);

        PINDialog dialog = new PINDialog(this, "1234", new PINDialog.onExitListener()
        {
            @Override
            public void onExit()
            {
                dialogIsVisible = false;
                updateAll();
                all.setVisibility(View.VISIBLE);
                init();
            }

            @Override
            public void onUseDemoData()
            {
                dialogIsVisible = false;
                updateExpenses();
                all.setVisibility(View.VISIBLE);
                init();
            }

            @Override
            public void onCancel()
            {
                dialogIsVisible = false;
                updateExpenses();
                all.setVisibility(View.VISIBLE);
                init();
            }
        });

        if (!dialogIsVisible)
        {
            dialogIsVisible = true;
            dialog.show();
        }
    }

    private void updateAll()
    {
        updateExpenses();
        pageAdapter.getBankAccountFragment().update();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        LGA.init(this);

        setContentView(R.layout.activity_finance);


        init();
    }

    private void init()
    {
        listView = (ListView) findViewById(R.id.list_overview);

        addFoldPossibility();
        TextView p1 = (TextView) findViewById(R.id.text_p1);
        TextView p2 = (TextView) findViewById(R.id.text_p2);
        List<String> names = LGA.getSingleton().getNames();
        p1.setText(names.get(0));
        p2.setText(names.get(1));

        adapter = new OverviewAdapter(this);
        listView.setAdapter(adapter);
        FinanceUtilities.synchronizeWithStandingOrders(this, new Runnable()
        {
            @Override
            public void run()
            {
                FinanceUtilities.calculateCompensationPayment(FinanceActivity.this, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateAll();
                    }
                });
            }
        });

        listView.setClickable(false);
        listView.setEnabled(false);
        listView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return false;
            }
        });


        pageAdapter = new FinanceFragmentPagerAdapter(this, getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.statistics_pager);
        pager.setAdapter(pageAdapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                Log.d(LogKey, "Page Selected: " + position);
                page = position;
                getSupportActionBar().setSelectedNavigationItem(position);
                switch (position)
                {
                    case 0:
                        adapter.setMode(OverviewAdapter.Mode.Expenses);
                        break;
                    case 1:
                        adapter.setMode(OverviewAdapter.Mode.StandingOrder);
                        break;
                    case 2:
                        adapter.setMode(OverviewAdapter.Mode.BankAccount);
                        break;
                    case 3:
                        adapter.setMode(OverviewAdapter.Mode.BankAccount);
                        break;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });


        // Specify that tabs should be displayed in the action bar.
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener()
        {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft)
            {
                // show the given tab
                int i = tab.getPosition();
                pager.setCurrentItem(i);
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft)
            {
                // hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft)
            {
                // probably ignore this event
            }
        };


        // Add 3 tabs, specifying the tab's text and TabListener
        for (int i = 0; i < pageAdapter.getCount(); i++)
        {
            getSupportActionBar().addTab(
                    getSupportActionBar().newTab()
                            .setText(pageAdapter.getPageTitle(i))
                            .setTabListener(tabListener));
        }
    }


    private void addFoldPossibility()
    {
        findViewById(R.id.overview_header).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                overViewVisible = !overViewVisible;
                if (overViewVisible)
                {
                    listView.setVisibility(View.VISIBLE);
                } else
                {
                    listView.setVisibility(View.GONE);
                }
            }
        });
    }


    public void click_addCosts(View view)
    {
        if (page == 0 || page == 1)
        {
            AddExpensesDialog dialog = new AddExpensesDialog(this);
            dialog.setConfirmListener(new AddExpensesDialog.ConfirmListener()
            {

                @Override
                public void onConfirm(Expenses expenses)
                {
                    SQLiteFinanceHandler.addExpenses(FinanceActivity.this, expenses);
                    updateExpenses();
                }

                @Override
                public void onConfirm(StandingOrder standingOrder, Expenses expenses)
                {
                    if (expenses.getDate().isBefore(DateTime.now()))
                    {
                        SQLiteFinanceHandler.addExpenses(FinanceActivity.this, expenses);
                    }
                    SQLiteFinanceHandler.addStandingOrder(FinanceActivity.this, standingOrder);
                    FinanceUtilities.synchronizeWithStandingOrders(FinanceActivity.this, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            updateExpenses();
                        }
                    });
                }
            });
            dialog.start();
        } else if (page == 2)
        {
            AddBankAccountDialog dialog = new AddBankAccountDialog(this);
            dialog.setConfirmListener(new AddBankAccountDialog.ConfirmListener()
            {
                @Override
                public void onConfirm(BankAccount bankAccount)
                {
                    SQLiteFinanceHandler.addBankAccount(FinanceActivity.this, bankAccount);
                    String myId = Installation.id(FinanceActivity.this);
                    SQLiteFinanceHandler.addBalance(FinanceActivity.this, new Balance(bankAccount.getBalance(), bankAccount.getDate(), bankAccount.getBank(), bankAccount.getName(), myId));

                    pageAdapter.getBankAccountFragment().update();
                }
            });

            dialog.start();
        }
    }

    private void updateExpenses()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                ExpensesFragment fragment = pageAdapter.getExpensesFragment();
                fragment.updateExpenses();
                StandingOrderFragment standingOrderFragment = pageAdapter.getStandingOrderFragment();
                standingOrderFragment.updateExpenses();
                adapter.update();
            }
        }).start();
    }

    @Override
    public void update(Observable observable, Object data)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                adapter.update();
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_finance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_backup)
        {
            File backup = Utilities.exportDB(this, SQLiteFinanceHandler.getActiveDatabaseName(), LGA.getSingleton().isAuthorized() ? "finances_db_backup" : "finances_db_demo_backup");
            Utilities.sendFilePerMail(this, backup, "thomashorn87@gmail.com");
            return true;
        }

        if (id == R.id.action_import)
        {
            showFileChooser();

            return true;
        }

        if (id == R.id.action_sync)
        {
            SyncFragment_ syncFragment = new SyncFragment_();
            syncFragment.show(getSupportFragmentManager(), "sync_fragment");
        }

        return super.onOptionsItemSelected(item);
    }

    private void showFileChooser()
    {
        Intent filePickerIntent = new Intent(this, FilePickerActivity.class);
        filePickerIntent.putExtra(FilePickerActivity.REQUEST_CODE, FilePickerActivity.REQUEST_FILE);
        startActivityForResult(filePickerIntent, FilePickerActivity.REQUEST_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == FilePickerActivity.REQUEST_FILE && resultCode == RESULT_OK)
        {
            String filePath = data.getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH);
            if (filePath != null)
            {
                Utilities.importDB(this, new File(filePath), SQLiteFinanceHandler.getActiveDatabaseName());
                updateExpenses();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}
