package com.example.thomas.lga.Finances;

import com.example.thomas.lga.Database.DatabaseItem;

import org.joda.time.DateTime;

/**
 * Created by Thomas on 23.10.2015.
 */
public class Balance extends DatabaseItem
{
    private float balance;
    private DateTime date;
    private String bankName;
    private String bankAccountName;

    public Balance(int id, float balance, DateTime date, String bank, String bankAccountName, DateTime insertDate, DateTime lastModified, boolean deleted, String createdFrom, String lastChangeFrom)
    {
        super(id, insertDate, lastModified, deleted, createdFrom, lastChangeFrom);
        this.balance = balance;
        this.date = date;
        this.bankName = bank;
        this.bankAccountName = bankAccountName;
    }

    public Balance(int id, float balance, DateTime date, String bank, String bankAccountName, String createdFrom)
    {
        super(id, createdFrom, createdFrom);
        this.balance = balance;
        this.date = date;
        this.bankName = bank;
        this.bankAccountName = bankAccountName;
    }

    public Balance(String myId)
    {
        super(myId, myId);
    }

    public Balance(float balance, DateTime date, String bank, String name, String myId)
    {
        this(0, balance, date, bank, name, DateTime.now(), DateTime.now(), false, myId, myId);
    }

    @Override
    public String toString()
    {
        return "Balance{" +
                "balance=" + balance +
                ", date=" + date.toString("yy-MM-dd HH:mm") +
                ", bankName='" + bankName + '\'' +
                ", bankAccountName='" + bankAccountName + '\'' +
                ", deleted='" + deleted + '\'' +
                ", modifiedDate=" + lastModifiedDate.toString("yy-MM-dd HH:mm") +
                ", insertDate=" + insertDate.toString("yy-MM-dd HH:mm") +
                '}';
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public float getBalance()
    {
        return balance;
    }

    public void setBalance(float balance)
    {
        this.balance = balance;
    }

    public DateTime getDate()
    {
        return date;
    }

    public void setDate(DateTime date)
    {
        this.date = date;
    }

    public String getBankName()
    {
        return bankName;
    }

    public void setBankName(String bankName)
    {
        this.bankName = bankName;
    }

    public String getBankAccountName()
    {
        return bankAccountName;
    }

    public void setBankAccountName(String bankAccountName)
    {
        this.bankAccountName = bankAccountName;
    }
}
