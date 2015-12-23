package com.example.thomas.lga.Network;

import com.example.thomas.lga.Finances.Expenses;

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
    public static final String encoding = "ISO-8859-1";
    public static final String LogKey = "ByteConverter";

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
