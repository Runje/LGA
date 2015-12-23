package com.example.thomas.lga.Network.Messages;

import com.example.thomas.lga.Network.ByteConverter;
import com.example.thomas.lga.Network.Message;
import com.example.thomas.lga.Network.MessageParser;

import org.joda.time.DateTime;

import java.nio.ByteBuffer;

/**
 * Created by Thomas on 13.12.2015.
 */
public class RequestSyncMessage extends Message
{
    protected DateTime date;
    protected boolean authorized;

    public RequestSyncMessage(DateTime date, boolean authorized)
    {
        id = MessageParser.Request_Sync1;
        this.date = date;
        this.authorized = authorized;
    }

    public RequestSyncMessage(ByteBuffer buffer)
    {
        id = MessageParser.Request_Sync1;
        this.date = new DateTime(buffer.getLong());
        this.authorized = ByteConverter.byteToBoolean(buffer.get());
    }

    public DateTime getDate()
    {
        return date;
    }

    public boolean isAuthorized()
    {
        return authorized;
    }

    @Override
    public void updateContent()
    {
        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.putLong(date.getMillis());
        buffer.put(ByteConverter.booleanToByte(authorized));

        content = buffer.array();
    }

    @Override
    public String toString()
    {
        return "Request Sync Message: " + date.toString();
    }
}
