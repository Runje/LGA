package com.example.thomas.lga.Network.Messages;

import com.example.thomas.lga.Network.ByteConverter;
import com.example.thomas.lga.Network.Message;
import com.example.thomas.lga.Network.MessageParser;

import java.nio.ByteBuffer;

/**
 * Created by Thomas on 13.12.2015.
 */
public class SyncResultMessage extends Message
{
    private boolean result;

    public SyncResultMessage(boolean result)
    {
        id = MessageParser.Sync_Result;
        this.result = result;
    }

    public SyncResultMessage(ByteBuffer buffer)
    {
        id = MessageParser.Sync_Result;
        result = ByteConverter.byteToBoolean(buffer.get());
    }

    public boolean isResult()
    {
        return result;
    }

    @Override
    public void updateContent()
    {
        content = new byte[]{ByteConverter.booleanToByte(result)};
    }

    @Override
    public String toString()
    {
        return "Sync Result Message";
    }
}
