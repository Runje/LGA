package com.example.thomas.lga.Network;

import com.example.thomas.lga.Finances.Balance;
import com.example.thomas.lga.Finances.BankAccount;
import com.example.thomas.lga.Finances.Expenses;
import com.example.thomas.lga.Finances.Frequency;
import com.example.thomas.lga.Finances.StandingOrder;

import org.joda.time.DateTime;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by Thomas on 12.12.2015.
 */
public class ByteConverter
{

    public static final int String_Max_Size = 256;
    public static final int Expenses_Max_Size = 4 + 4 + 4 * 8 + 4 * String_Max_Size + 2;
    public static final int Frequency_Size = 4;
    public static final String encoding = "ISO-8859-1";
    public static final String LogKey = "ByteConverter";
    public static final int BankAccounts_Max_Size = 4 * 4 + 3 * 8 + 5 * String_Max_Size + 1;
    public static final int StandingOrders_Max_Size = 4 + 4 + 4 * 8 + Frequency_Size + 4 + 6 * String_Max_Size + 1;
    public static final int Balances_Max_Size = 2 * 4 + 3 * 8 + 4 * String_Max_Size + 1;

    public static byte[] expensesToByte(Expenses expenses)
    {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + 3 * 8 + stringLength(expenses.getCategory()) + stringLength(expenses.getName()) + stringLength(expenses.getWho()) + stringLength(expenses.getUser()) + 2 + stringLength(expenses.getCreatedFrom()) + stringLength(expenses.getLastChangeFrom()));
        buffer.putInt(expenses.getId());
        buffer.putFloat(expenses.getCosts());
        buffer.putLong(expenses.getDate().getMillis());
        buffer.putLong(expenses.getInsertDate().getMillis());
        buffer.putLong(expenses.getLastModifiedDate().getMillis());
        buffer.put(stringToByte(expenses.getCategory()));
        buffer.put(stringToByte(expenses.getName()));
        buffer.put(stringToByte(expenses.getWho()));
        buffer.put(stringToByte(expenses.getUser()));
        buffer.put(booleanToByte(expenses.isStandingOrder()));
        buffer.put(booleanToByte(expenses.isDeleted()));
        buffer.put(stringToByte(expenses.getCreatedFrom()));
        buffer.put(stringToByte(expenses.getLastChangeFrom()));
        return buffer.array();
    }

    public static Expenses byteToExpenses(ByteBuffer buffer)
    {
        int id = buffer.getInt();
        float costs = buffer.getFloat();
        DateTime date = new DateTime(buffer.getLong());
        DateTime insertDate = new DateTime(buffer.getLong());
        DateTime lastModified = new DateTime(buffer.getLong());
        String category = byteToString(buffer);
        String name = byteToString(buffer);
        String who = byteToString(buffer);
        String user = byteToString(buffer);
        boolean standingOrder = byteToBoolean(buffer.get());
        boolean deleted = byteToBoolean(buffer.get());
        String createdFrom = byteToString(buffer);
        String lastChangedFrom = byteToString(buffer);
        return new Expenses(id, who, costs, name, category, user, date, standingOrder, insertDate, lastModified, deleted, createdFrom, lastChangedFrom);
    }

    public static byte[] standingOrderToByte(StandingOrder standingOrder)
    {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + 4 * 8 + Frequency_Size + 4 + stringLength(standingOrder.getCategory()) + stringLength(standingOrder.getName())
                + stringLength(standingOrder.getWho()) + stringLength(standingOrder.getUser()) + 1 + stringLength(standingOrder.getCreatedFrom()) + stringLength(standingOrder.getLastChangeFrom()));
        buffer.putInt(standingOrder.getId());
        buffer.putFloat(standingOrder.getCosts());
        buffer.putLong(standingOrder.getFirstDate().getMillis());
        buffer.putLong(standingOrder.getLastDate().getMillis());
        buffer.putLong(standingOrder.getInsertDate().getMillis());
        buffer.putLong(standingOrder.getLastModifiedDate().getMillis());
        buffer.put(frequencyToByte(standingOrder.getFrequency()));
        buffer.putInt(standingOrder.getNumber());
        buffer.put(stringToByte(standingOrder.getCategory()));
        buffer.put(stringToByte(standingOrder.getName()));
        buffer.put(stringToByte(standingOrder.getWho()));
        buffer.put(stringToByte(standingOrder.getUser()));
        buffer.put(booleanToByte(standingOrder.isDeleted()));
        buffer.put(stringToByte(standingOrder.getCreatedFrom()));
        buffer.put(stringToByte(standingOrder.getLastChangeFrom()));
        return buffer.array();
    }

    public static StandingOrder byteToStandingOrder(ByteBuffer buffer)
    {
        int id = buffer.getInt();
        float costs = buffer.getFloat();
        DateTime firstDate = new DateTime(buffer.getLong());
        DateTime lastDate = new DateTime(buffer.getLong());
        DateTime insertDate = new DateTime(buffer.getLong());
        DateTime lastModified = new DateTime(buffer.getLong());
        Frequency frequency = byteToFrequency(buffer);
        int number = buffer.getInt();
        String category = byteToString(buffer);
        String name = byteToString(buffer);
        String who = byteToString(buffer);
        String user = byteToString(buffer);
        boolean deleted = byteToBoolean(buffer.get());
        String createdFrom = byteToString(buffer);
        String lastChangedFrom = byteToString(buffer);
        return new StandingOrder(id, who, costs, name, category, user, firstDate, lastDate, frequency, number, insertDate, lastModified, deleted, createdFrom, lastChangedFrom);
    }

    public static byte[] bankAccountToByte(BankAccount bankAccount)
    {
        ByteBuffer buffer = ByteBuffer.allocate(4 * 4 + 3 * 8 + stringLength(bankAccount.getBank()) + stringLength(bankAccount.getName()) + stringLength(bankAccount.getOwner())
                + 1 + stringLength(bankAccount.getCreatedFrom()) + stringLength(bankAccount.getLastChangeFrom()));
        buffer.putInt(bankAccount.getId());
        buffer.putFloat(bankAccount.getBalance());
        buffer.putFloat(bankAccount.getInterest());
        buffer.putFloat(bankAccount.getMonthly_costs());
        buffer.putLong(bankAccount.getDate().getMillis());
        buffer.putLong(bankAccount.getInsertDate().getMillis());
        buffer.putLong(bankAccount.getLastModifiedDate().getMillis());
        buffer.put(stringToByte(bankAccount.getBank()));
        buffer.put(stringToByte(bankAccount.getName()));
        buffer.put(stringToByte(bankAccount.getOwner()));
        buffer.put(booleanToByte(bankAccount.isDeleted()));
        buffer.put(stringToByte(bankAccount.getCreatedFrom()));
        buffer.put(stringToByte(bankAccount.getLastChangeFrom()));
        return buffer.array();
    }

    public static BankAccount byteToBankAccount(ByteBuffer buffer)
    {
        int id = buffer.getInt();
        float balance = buffer.getFloat();
        float interest = buffer.getFloat();
        float monthlyCosts = buffer.getFloat();
        DateTime date = new DateTime(buffer.getLong());
        DateTime insertDate = new DateTime(buffer.getLong());
        DateTime lastModified = new DateTime(buffer.getLong());
        String bank = byteToString(buffer);
        String name = byteToString(buffer);
        String owner = byteToString(buffer);
        boolean deleted = byteToBoolean(buffer.get());
        String createdFrom = byteToString(buffer);
        String lastChangedFrom = byteToString(buffer);
        return new BankAccount(id, name, bank, interest, monthlyCosts, owner, date, balance, insertDate, lastModified, deleted, createdFrom, lastChangedFrom);
    }

    public static byte[] balanceToByte(Balance balance)
    {
        ByteBuffer buffer = ByteBuffer.allocate(2 * 4 + 3 * 8 + stringLength(balance.getBankAccountName()) + stringLength(balance.getBankName())
                + 1 + stringLength(balance.getCreatedFrom()) + stringLength(balance.getLastChangeFrom()));
        buffer.putInt(balance.getId());
        buffer.putFloat(balance.getBalance());
        buffer.putLong(balance.getDate().getMillis());
        buffer.putLong(balance.getInsertDate().getMillis());
        buffer.putLong(balance.getLastModifiedDate().getMillis());
        buffer.put(stringToByte(balance.getBankAccountName()));
        buffer.put(stringToByte(balance.getBankName()));
        buffer.put(booleanToByte(balance.isDeleted()));
        buffer.put(stringToByte(balance.getCreatedFrom()));
        buffer.put(stringToByte(balance.getLastChangeFrom()));
        return buffer.array();
    }

    public static Balance byteToBalance(ByteBuffer buffer)
    {
        int id = buffer.getInt();
        float balance = buffer.getFloat();
        DateTime date = new DateTime(buffer.getLong());
        DateTime insertDate = new DateTime(buffer.getLong());
        DateTime lastModified = new DateTime(buffer.getLong());
        String bankAccountName = byteToString(buffer);
        String bankName = byteToString(buffer);
        boolean deleted = byteToBoolean(buffer.get());
        String createdFrom = byteToString(buffer);
        String lastChangedFrom = byteToString(buffer);
        return new Balance(id, balance, date, bankName, bankAccountName, insertDate, lastModified, deleted, createdFrom, lastChangedFrom);
    }


    private static Frequency byteToFrequency(ByteBuffer buffer)
    {
        return Frequency.indexToFrequency(buffer.getInt());
    }

    private static byte[] frequencyToByte(Frequency frequency)
    {
        int i = Frequency.FrequencyToIndex(frequency);
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(i);
        return buffer.array();
    }


    public static byte booleanToByte(boolean b)
    {
        return (byte) (b ? 1 : 0);
    }

    public static boolean byteToBoolean(Byte b)
    {
        return b == (byte) 1;
    }

    public static int stringLength(String string)
    {
        return 2 + string.length();
    }

    public static byte[] stringToByte(String string)
    {
        try
        {
            ByteBuffer buffer = ByteBuffer.allocate(2 + string.length());
            if (string.length() > Short.MAX_VALUE)
            {
                throw new RuntimeException("String longer than Short.Max_value");
            }
            buffer.putShort((short) string.length());
            buffer.put(string.getBytes(encoding));
            return buffer.array();
        } catch (UnsupportedEncodingException e)
        {

            e.printStackTrace();
            return null;
        }
    }

    public static String byteToString(ByteBuffer buffer)
    {
        short length = buffer.getShort();
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, Charset.forName(encoding)).trim();
    }
}
