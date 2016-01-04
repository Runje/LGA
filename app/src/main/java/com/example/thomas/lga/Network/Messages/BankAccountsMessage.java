package com.example.thomas.lga.Network.Messages;

import com.example.thomas.lga.Finances.BankAccount;
import com.example.thomas.lga.Network.ByteConverter;
import com.example.thomas.lga.Network.Message;
import com.example.thomas.lga.Network.MessageParser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 13.12.2015.
 */
public class BankAccountsMessage extends Message
{
    private final List<BankAccount> bankAccounts;

    public BankAccountsMessage(ByteBuffer buffer)
    {
        id = MessageParser.BankAccounts;
        int number = buffer.getInt();
        bankAccounts = new ArrayList<>(number);
        for (int i = 0; i < number; i++)
        {
            bankAccounts.add(ByteConverter.byteToBankAccount(buffer));
        }
    }

    public BankAccountsMessage(List<BankAccount> bankAccounts)
    {
        id = MessageParser.BankAccounts;
        this.bankAccounts = bankAccounts;
    }

    public List<BankAccount> getBankAccounts()
    {
        return bankAccounts;
    }

    @Override
    public void updateContent()
    {
        ByteBuffer buffer = ByteBuffer.allocate(4 + ByteConverter.BankAccounts_Max_Size * bankAccounts.size());
        buffer.putInt(bankAccounts.size());
        for (BankAccount bankAccount : bankAccounts)
        {
            buffer.put(ByteConverter.bankAccountToByte(bankAccount));
        }

        int pos = buffer.position();
        buffer.position(0);
        content = new byte[pos];
        buffer.get(content);
    }

    @Override
    public String toString()
    {
        return "BankAccounts Message: " + bankAccounts.size();
    }
}
