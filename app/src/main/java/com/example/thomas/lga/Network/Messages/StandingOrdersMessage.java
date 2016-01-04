package com.example.thomas.lga.Network.Messages;

import com.example.thomas.lga.Finances.StandingOrder;
import com.example.thomas.lga.Network.ByteConverter;
import com.example.thomas.lga.Network.Message;
import com.example.thomas.lga.Network.MessageParser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 13.12.2015.
 */
public class StandingOrdersMessage extends Message
{
    private final List<StandingOrder> standingOrders;

    public StandingOrdersMessage(ByteBuffer buffer)
    {
        id = MessageParser.StandingOrders;
        int number = buffer.getInt();
        standingOrders = new ArrayList<>(number);
        for (int i = 0; i < number; i++)
        {
            standingOrders.add(ByteConverter.byteToStandingOrder(buffer));
        }
    }

    public StandingOrdersMessage(List<StandingOrder> standingOrders)
    {
        id = MessageParser.StandingOrders;
        this.standingOrders = standingOrders;
    }

    public List<StandingOrder> getStandingOrders()
    {
        return standingOrders;
    }

    @Override
    public void updateContent()
    {
        ByteBuffer buffer = ByteBuffer.allocate(4 + ByteConverter.StandingOrders_Max_Size * standingOrders.size());
        buffer.putInt(standingOrders.size());
        for (StandingOrder standingOrder : standingOrders)
        {
            buffer.put(ByteConverter.standingOrderToByte(standingOrder));
        }

        int pos = buffer.position();
        buffer.position(0);
        content = new byte[pos];
        buffer.get(content);
    }

    @Override
    public String toString()
    {
        return "StandingOrders Message: " + standingOrders.size();
    }
}
