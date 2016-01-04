package com.example.thomas.lga.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.Finances.Balance;
import com.example.thomas.lga.Finances.BankAccount;
import com.example.thomas.lga.Finances.Expenses;
import com.example.thomas.lga.Finances.FinanceUtilities;
import com.example.thomas.lga.Finances.StandingOrder;
import com.example.thomas.lga.LGA;
import com.example.thomas.lga.Network.Message;
import com.example.thomas.lga.Network.MessageHandler;
import com.example.thomas.lga.Network.MessageParser;
import com.example.thomas.lga.Network.Messages.BalancesMessage;
import com.example.thomas.lga.Network.Messages.BankAccountsMessage;
import com.example.thomas.lga.Network.Messages.ExpensesMessage;
import com.example.thomas.lga.Network.Messages.RequestMessage;
import com.example.thomas.lga.Network.Messages.StandingOrdersMessage;
import com.example.thomas.lga.Network.Messages.SyncResultMessage;
import com.example.thomas.lga.Network.SyncConnection;
import com.example.thomas.lga.R;
import com.example.thomas.lga.Views.ConflictedDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by Thomas on 02.12.2015.
 */
@EFragment(R.layout.sync_fragment)
public class SyncFragment extends DialogFragment
{

    public static final String LogKey = "SyncFragment";
    public static final String LastSyncSendDemoKey = "LastSyncSend";
    public static final String LastSyncReceiveDemoKey = "LastSyncReceive";
    public static final String LastSyncSendKey = "LastSyncRealSend";
    public static final String LastSyncReceiveKey = "LastSyncRealReceive";
    public static final DateTime Never = new DateTime(0, 1, 1, 1, 1);

    @ViewById
    public ListView listView;

    @ViewById
    public EditText edit_ip;

    @ViewById
    public TextView text_last_sync;

    @ViewById
    public TextView text_connection;

    @ViewById
    public LinearLayout header;

    @ViewById
    public TextView text_status;

    private boolean checking;
    private ConflictedDialog conflictedDialog;
    private SyncListener callback;

    public SyncFragment()
    {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context)
    {
        Log.i(LogKey, "On Attach Context");
        super.onAttach(context);
        callback = (SyncListener) context;
    }

    @AfterViews
    public void init()
    {
        header.findViewById(R.id.text_delete).setVisibility(View.GONE);
        SyncConnection.getInstance().startListening();
        SyncConnection.getInstance().setMessageHandler(new MessageHandler()
        {
            @Override
            public void handleMessage(final Message msg)
            {
                receivedMessage(msg);
            }
        });

        boolean authorized = LGA.getSingleton().isAuthorized();
        DateTime lastSync = lastSyncDate(authorized ? LastSyncSendKey : LastSyncSendDemoKey);
        String text = "";
        if (lastSync.equals(Never))
        {
            text = "Noch nie synchronisiert";
        } else
        {
            text = lastSync.toString();
        }
        text_last_sync.setText(text);
        checkConnectionStatus();
    }

    private void receivedMessage(final Message msg)
    {
        if (isAdded())
        {
            getActivity().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    text_status.setText("Received: " + msg.toString());
                    boolean authorized = LGA.getSingleton().isAuthorized();
                    switch (msg.getId())
                    {
                        case MessageParser.Request_Expenses:
                        case MessageParser.Request_StandingOrders:
                        case MessageParser.Request_BankAccounts:
                        case MessageParser.Request_Balances:

                            RequestMessage requestSyncMessage = (RequestMessage) msg;
                            if (requestSyncMessage.isAuthorized())
                            {
                                if (!LGA.getSingleton().isAuthorized())
                                {
                                    Log.i(LogKey, "Not Authorized!");
                                    Toast.makeText(getContext(), R.string.not_authorized_for_sync, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else
                            {
                                if (LGA.getSingleton().isAuthorized())
                                {
                                    Log.i(LogKey, "Authorized!");
                                    LGA.getSingleton().setAuthorized(false);
                                }
                            }

                            switch (requestSyncMessage.getId())
                            {
                                case MessageParser.Request_Expenses:
                                    List<Expenses> expensesList = SQLiteFinanceHandler.getExpensesToSync(getActivity(), requestSyncMessage.getDate());

                                    SyncConnection.getInstance().sendMessage(new ExpensesMessage(expensesList));
                                    Log.i(LogKey, "Sent Expenses: ");
                                    for (Expenses expenses : expensesList)
                                    {
                                        Log.i(LogKey, expenses.toString());
                                    }
                                    break;

                                case MessageParser.Request_StandingOrders:
                                    List<StandingOrder> standingOrders = SQLiteFinanceHandler.getStandingOrdersToSync(getActivity(), requestSyncMessage.getDate());

                                    SyncConnection.getInstance().sendMessage(new StandingOrdersMessage(standingOrders));
                                    Log.i(LogKey, "Sent StandingOrders: ");
                                    for (StandingOrder standingOrder : standingOrders)
                                    {
                                        Log.i(LogKey, standingOrder.toString());
                                    }
                                    break;
                                case MessageParser.Request_BankAccounts:
                                    List<BankAccount> bankAccounts = SQLiteFinanceHandler.getBankAccountsToSync(getActivity(), requestSyncMessage.getDate());

                                    SyncConnection.getInstance().sendMessage(new BankAccountsMessage(bankAccounts));
                                    Log.i(LogKey, "Sent BankAccounts: ");
                                    for (BankAccount bankAccount : bankAccounts)
                                    {
                                        Log.i(LogKey, bankAccount.toString());
                                    }
                                    break;

                                case MessageParser.Request_Balances:
                                    List<Balance> balances = SQLiteFinanceHandler.getBalancesToSync(getActivity(), requestSyncMessage.getDate());

                                    SyncConnection.getInstance().sendMessage(new BalancesMessage(balances));
                                    Log.i(LogKey, "Sent Balances: ");
                                    for (Balance balance : balances)
                                    {
                                        Log.i(LogKey, balance.toString());
                                    }
                                    break;
                            }

                            break;

                        case MessageParser.Expenses:
                            ExpensesMessage expensesMessage = (ExpensesMessage) msg;
                            Log.i(LogKey, "Received Expenses: ");
                            for (Expenses expenses : expensesMessage.getExpenses())
                            {
                                Log.i(LogKey, expenses.toString());
                            }

                            List<Expenses> conflicted = FinanceUtilities.syncExpenses(getContext(), expensesMessage.getExpenses(), lastSyncDate(authorized ? LastSyncReceiveKey : LastSyncReceiveDemoKey));

                            SyncConnection.getInstance().sendMessage(new RequestMessage(lastSyncDate(LGA.getSingleton().isAuthorized() ? LastSyncReceiveKey : LastSyncReceiveDemoKey), LGA.getSingleton().isAuthorized(), MessageParser.Request_StandingOrders));
                            handleConflictedExpenses(conflicted);
                            break;

                        case MessageParser.StandingOrders:
                            StandingOrdersMessage standingOrdersMessage = (StandingOrdersMessage) msg;

                            Log.i(LogKey, "Received StandingOrders: ");
                            for (StandingOrder standingOrder : standingOrdersMessage.getStandingOrders())
                            {
                                Log.i(LogKey, standingOrder.toString());
                            }

                            List<StandingOrder> conflictedStandingOrders = FinanceUtilities.syncStandingOrders(getContext(), standingOrdersMessage.getStandingOrders(), lastSyncDate(authorized ? LastSyncReceiveKey : LastSyncReceiveDemoKey));

                            SyncConnection.getInstance().sendMessage(new RequestMessage(lastSyncDate(LGA.getSingleton().isAuthorized() ? LastSyncReceiveKey : LastSyncReceiveDemoKey), LGA.getSingleton().isAuthorized(), MessageParser.Request_BankAccounts));
                            handleConflictedStandingOrders(conflictedStandingOrders);
                            break;

                        case MessageParser.BankAccounts:
                            BankAccountsMessage bankAccountsMessage = (BankAccountsMessage) msg;
                            Log.i(LogKey, "Received Expenses: ");
                            for (BankAccount bankAccount : bankAccountsMessage.getBankAccounts())
                            {
                                Log.i(LogKey, bankAccount.toString());
                            }

                            List<BankAccount> conflictedBankAccounts = FinanceUtilities.syncBankAccounts(getContext(), bankAccountsMessage.getBankAccounts(), lastSyncDate(authorized ? LastSyncReceiveKey : LastSyncReceiveDemoKey));
                            SyncConnection.getInstance().sendMessage(new RequestMessage(lastSyncDate(LGA.getSingleton().isAuthorized() ? LastSyncReceiveKey : LastSyncReceiveDemoKey), LGA.getSingleton().isAuthorized(), MessageParser.Request_Balances));
                            handleConflictedBankAccounts(conflictedBankAccounts);
                            break;

                        case MessageParser.Balances:
                            BalancesMessage balancesMessage = (BalancesMessage) msg;
                            Log.i(LogKey, "Received Balances: ");
                            for (Balance ba : balancesMessage.getBalances())
                            {
                                Log.i(LogKey, ba.toString());
                            }

                            List<Balance> conflictedBalances = FinanceUtilities.syncBalances(getContext(), balancesMessage.getBalances(), lastSyncDate(authorized ? LastSyncReceiveKey : LastSyncReceiveDemoKey));
                            setSyncDate(authorized ? LastSyncReceiveKey : LastSyncReceiveDemoKey, DateTime.now());
                            SyncConnection.getInstance().sendMessage(new SyncResultMessage(true));
                            handleConflictedBalances(conflictedBalances);
                            conflictedDialog.show();
                            setSyncDate(authorized ? LastSyncReceiveKey : LastSyncReceiveDemoKey, DateTime.now());
                            updateAdapters();
                            break;

                        case MessageParser.Sync_Result:
                            SyncResultMessage resultMessage = (SyncResultMessage) msg;

                            if (resultMessage.isResult())
                            {
                                setSyncDate(authorized ? LastSyncSendKey : LastSyncSendDemoKey, DateTime.now());
                            }
                    }

                }
            });
        }
    }

    private void updateAdapters()
    {
        if (callback != null)
        {
            callback.updateAll();
        }
    }

    private void handleConflictedExpenses(List<Expenses> conflicted)
    {
        if (conflictedDialog == null)
        {
            conflictedDialog = new ConflictedDialog(getContext());
            conflictedDialog.setExpenses(conflicted);
        } else
        {
            conflictedDialog.setExpenses(conflicted);
        }
    }

    private void handleConflictedStandingOrders(List<StandingOrder> conflicted)
    {
        if (conflictedDialog == null)
        {
            conflictedDialog = new ConflictedDialog(getContext());
            conflictedDialog.setStandingOrders(conflicted);
        } else
        {
            conflictedDialog.setStandingOrders(conflicted);
        }
    }

    private void handleConflictedBankAccounts(List<BankAccount> conflicted)
    {
        if (conflictedDialog == null)
        {
            conflictedDialog = new ConflictedDialog(getContext());
            conflictedDialog.setBankAccounts(conflicted);
        } else
        {
            conflictedDialog.setBankAccounts(conflicted);
        }
    }

    private void handleConflictedBalances(List<Balance> conflicted)
    {
        if (conflictedDialog == null)
        {
            conflictedDialog = new ConflictedDialog(getContext());
            conflictedDialog.setBalances(conflicted);
        } else
        {
            conflictedDialog.setBalances(conflicted);
        }
    }

    public DateTime lastSyncDate(String key)
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MainActivity.SharedPreferencesName, Context.MODE_PRIVATE);
        long millis = sharedPreferences.getLong(key, Never.getMillis());

        return new DateTime(millis);
    }

    public void setSyncDate(String key, DateTime date)
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MainActivity.SharedPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, date.getMillis());
        editor.commit();
    }

    @Click(R.id.button_sync)
    public void synchronize()
    {
        SyncConnection.getInstance().sendMessage(new RequestMessage(lastSyncDate(LGA.getSingleton().isAuthorized() ? LastSyncReceiveKey : LastSyncReceiveDemoKey), LGA.getSingleton().isAuthorized(), MessageParser.Request_Expenses));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        checking = false;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Utilities.adaptDialogSize(getDialog(), 6f / 7, 4f / 5);

        //getDialog().getWindow().setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT);
        checkConnectionStatus();
    }

    private void checkConnectionStatus()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                checking = true;
                while (checking)
                {
                    String text = "";
                    if (SyncConnection.getInstance().isConnected())
                    {
                        text = "Connected";
                    } else
                    {
                        text = "Not Connected";
                    }

                    final String finalText = text;
                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (text_connection != null)
                            {
                                text_connection.setText(finalText);
                            }
                        }
                    });

                    try
                    {
                        Thread.sleep(500);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Click(R.id.button_connect)
    public void tryConnect()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String ip = edit_ip.getText().toString();
                String text = "Not Connected";
                try
                {
                    boolean success = SyncConnection.getInstance().connect(ip);
                    if (success)
                    {
                        text = "Connected";
                    }
                } catch (Exception e)
                {
                    Log.i(LogKey, "Could not connect: " + e.getMessage());
                    text = e.getMessage();
                } finally
                {
                    final String finalText = text;
                    if (isAdded())
                    {
                        getActivity().runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                text_connection.setText(finalText);
                            }
                        });
                    }
                }
            }
        }).start();
    }

}
