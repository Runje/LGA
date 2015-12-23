package com.example.thomas.lga.Network.Messages;

import com.example.thomas.lga.Finances.Expenses;
import com.example.thomas.lga.Network.ByteConverter;
import com.example.thomas.lga.Network.Message;
import com.example.thomas.lga.Network.MessageParser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 13.12.2015.
 */
public class ExpensesMessage extends Message
{
    private final List<Expenses> expenses;

    public ExpensesMessage(ByteBuffer buffer)
    {
        id = MessageParser.Expenses;
        int number = buffer.getInt();
        expenses = new ArrayList<>(number);
        for (int i = 0; i < number; i++)
        {
            expenses.add(ByteConverter.byteToExpenses(buffer));
        }
    }

    public ExpensesMessage(List<Expenses> expenses)
    {
        id = MessageParser.Expenses;
        this.expenses = expenses;
    }

    public List<Expenses> getExpenses()
    {
        return expenses;
    }

    @Override
    public void updateContent()
    {
        ByteBuffer buffer = ByteBuffer.allocate(4 + ByteConverter.Expenses_Max_Size * expenses.size());
        buffer.putInt(expenses.size());
        for (Expenses exp : expenses)
        {
            buffer.put(ByteConverter.expensesToByte(exp));
        }

        int pos = buffer.position();
        buffer.position(0);
        content = new byte[pos];
        buffer.get(content);
    }

    @Override
    public String toString()
    {
        return "Expenses Message: " + expenses.size();
    }
}
