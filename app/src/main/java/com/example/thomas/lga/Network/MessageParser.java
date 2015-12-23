package com.example.thomas.lga.Network;

import com.example.thomas.lga.Network.Messages.ExpensesMessage;
import com.example.thomas.lga.Network.Messages.RequestSyncMessage;
import com.example.thomas.lga.Network.Messages.SyncResultMessage;

import java.nio.ByteBuffer;

/**
 * Created by Thomas on 13.12.2015.
 */
public class MessageParser
{
    public static final int Request_Sync1 = 1;
    public static final int Request_Sync2 = 2;
    public static final int Expenses = 3;
    public static final int Sync_Result = 4;

    public static Message parse(ByteBuffer buffer)
    {
        int id = buffer.getInt();

        Message msg = null;
        switch (id)
        {
            case Request_Sync1:
                msg = new RequestSyncMessage(buffer);
                break;
            case Expenses:
                msg = new ExpensesMessage(buffer);
                break;
            case Sync_Result:
                msg = new SyncResultMessage(buffer);
                break;
        }

        return msg;
    }
}
