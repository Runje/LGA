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
import com.example.thomas.lga.Finances.Expenses;
import com.example.thomas.lga.Finances.FinanceUtilities;
import com.example.thomas.lga.LGA;
import com.example.thomas.lga.Network.Message;
import com.example.thomas.lga.Network.MessageHandler;
import com.example.thomas.lga.Network.MessageParser;
import com.example.thomas.lga.Network.Messages.ExpensesMessage;
import com.example.thomas.lga.Network.Messages.RequestSyncMessage;
import com.example.thomas.lga.Network.Messages.SyncResultMessage;
import com.example.thomas.lga.Network.SyncConnection;
import com.example.thomas.lga.R;
import com.example.thomas.lga.Views.Adapter.ExpensesAdapter;
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

    public SyncFragment()
    {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context)
    {
        Log.d(LogKey, "On Attach Context");
        super.onAttach(context);
        //callback = (Observer) context;
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
                        case MessageParser.Request_Sync1:
                            RequestSyncMessage requestSyncMessage = (RequestSyncMessage) msg;
                            if (requestSyncMessage.isAuthorized())
                            {
                                if (!LGA.getSingleton().isAuthorized())
                                {
                                    Log.d(LogKey, "Not Authorized!");
                                    Toast.makeText(getContext(), R.string.not_authorized_for_sync, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else
                            {
                                if (LGA.getSingleton().isAuthorized())
                                {
                                    Log.d(LogKey, "Authorized!");
                                    LGA.getSingleton().setAuthorized(false);
                                }
                            }
                            List<Expenses> expensesList = SQLiteFinanceHandler.getExpensesToSync(getActivity(), requestSyncMessage.getDate());

                            SyncConnection.getInstance().sendMessage(new ExpensesMessage(expensesList));
                            Log.d(LogKey, "Sent Expenses: ");
                            for (Expenses expenses : expensesList)
                            {
                                Log.d(LogKey, expenses.toString());
                            }
                            break;

                        case MessageParser.Expenses:
                            ExpensesMessage expensesMessage = (ExpensesMessage) msg;
                            ExpensesAdapter adapter = new ExpensesAdapter(getContext());

                            Log.d(LogKey, "Received Expenses: ");
                            for (Expenses expenses : expensesMessage.getExpenses())
                            {
                                Log.d(LogKey, expenses.toString());
                            }

                            List<Expenses> conflicted = FinanceUtilities.syncExpenses(getContext(), expensesMessage.getExpenses(), lastSyncDate(authorized ? LastSyncReceiveKey : LastSyncReceiveDemoKey));
                            setSyncDate(authorized ? LastSyncReceiveKey : LastSyncReceiveDemoKey, DateTime.now());
                            SyncConnection.getInstance().sendMessage(new SyncResultMessage(true));
                            adapter.setExpenses(conflicted);
                            adapter.updateExpenses();
                            listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            handleConflicted(conflicted);
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

    private void handleConflicted(List<Expenses> conflicted)
    {
        if (conflicted.size() > 0)
        {
            new ConflictedDialog(getContext(), conflicted).show();
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
        SyncConnection.getInstance().sendMessage(new RequestSyncMessage(lastSyncDate(LGA.getSingleton().isAuthorized() ? LastSyncReceiveKey : LastSyncReceiveDemoKey), LGA.getSingleton().isAuthorized()));
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
                    Log.d(LogKey, "Could not connect: " + e.getMessage());
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
