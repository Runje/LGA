package com.example.thomas.lga.Network;

import com.example.thomas.lga.Network.Messages.BalancesMessage;
import com.example.thomas.lga.Network.Messages.BankAccountsMessage;
import com.example.thomas.lga.Network.Messages.ExpensesMessage;
import com.example.thomas.lga.Network.Messages.RequestMessage;
import com.example.thomas.lga.Network.Messages.StandingOrdersMessage;
import com.example.thomas.lga.Network.Messages.SyncResultMessage;

import java.nio.ByteBuffer;

/**
 * Created by Thomas on 13.12.2015.
 */
public class MessageParser
{
    public static final int Request_Expenses = 1;
    public static final int Request_StandingOrders = 2;
    public static final int Request_BankAccounts = 3;
    public static final int Request_Balances = 4;
    public static final int Expenses = 5;
    public static final int StandingOrders = 6;
    public static final int BankAccounts = 7;
    public static final int Balances = 8;
    public static final int Sync_Result = 9;

    public static Message parse(ByteBuffer buffer)
    {
        int id = buffer.getInt();

        Message msg = null;
        switch (id)
        {
            case Request_Expenses:
            case Request_StandingOrders:
            case Request_BankAccounts:
            case Request_Balances:
                msg = new RequestMessage(buffer, id);
                break;
            case Expenses:
                msg = new ExpensesMessage(buffer);
                break;
            case StandingOrders:
                msg = new StandingOrdersMessage(buffer);
                break;
            case BankAccounts:
                msg = new BankAccountsMessage(buffer);
                break;
            case Balances:
                msg = new BalancesMessage(buffer);
                break;
            case Sync_Result:
                msg = new SyncResultMessage(buffer);
                break;
        }

        return msg;
    }
}
