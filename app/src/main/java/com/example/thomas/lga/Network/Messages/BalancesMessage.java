package com.example.thomas.lga.Network.Messages;

import com.example.thomas.lga.Finances.Balance;
import com.example.thomas.lga.Network.ByteConverter;
import com.example.thomas.lga.Network.Message;
import com.example.thomas.lga.Network.MessageParser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 13.12.2015.
 */
public class BalancesMessage extends Message
{
    private final List<Balance> balances;

    public BalancesMessage(ByteBuffer buffer)
    {
        id = MessageParser.Balances;
        int number = buffer.getInt();
        balances = new ArrayList<>(number);
        for (int i = 0; i < number; i++)
        {
            balances.add(ByteConverter.byteToBalance(buffer));
        }
    }

    public BalancesMessage(List<Balance> balances)
    {
        id = MessageParser.Balances;
        this.balances = balances;
    }

    public List<Balance> getBalances()
    {
        return balances;
    }

    @Override
    public void updateContent()
    {
        ByteBuffer buffer = ByteBuffer.allocate(4 + ByteConverter.Balances_Max_Size * balances.size());
        buffer.putInt(balances.size());
        for (Balance balance : balances)
        {
            buffer.put(ByteConverter.balanceToByte(balance));
        }

        int pos = buffer.position();
        buffer.position(0);
        content = new byte[pos];
        buffer.get(content);
    }

    @Override
    public String toString()
    {
        return "Balances Message: " + balances.size();
    }
}
