package com.example.thomas.lga;

import android.content.Context;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 06.09.2015.
 */
public class LGA
{
    private static LGA instance;
    private static boolean initialized;

    private List<Name> names = new ArrayList<>();
    private boolean authorized;
    private DateTime startDate;

    private LGA()
    {

    }

    public static LGA getSingleton()
    {
        if (instance == null)
        {
            instance = new LGA();
        }

        return instance;
    }

    public static void init(Context context)
    {
        if (initialized)
        {
            return;
        }

        getSingleton().addName("Thomas", "T");
        getSingleton().addName("Milena", "M");
        getSingleton().addName(context.getResources().getText(R.string.all).toString(), "A");
        // TODO:
        getSingleton().setStartDate(new DateTime(2015, 10, 1, 0, 0));
        initialized = true;
    }

    public void addName(String name, String abbreviation)
    {
        names.add(new Name(name, abbreviation));
    }

    public List<String> getNames()
    {
        List<String> n = new ArrayList<>();
        for (Name name : names)
        {
            n.add(name.getName());
        }
        return n;
    }

    public String getAbbreviation(String who)
    {
        for (Name name : names)
        {
            if (name.getName().equals(who))
            {
                return name.getAbbreviation();
            }
        }

        return null;
    }

    public boolean isAuthorized()
    {
        return authorized;
    }

    public void setAuthorized(boolean authorized)
    {
        this.authorized = authorized;
    }

    public DateTime getStartDate()
    {
        return startDate;
    }

    public void setStartDate(DateTime startDate)
    {
        this.startDate = startDate;
    }

    private class Name
    {
        private String name;
        private String abbreviation;

        public Name(String name, String abbreviation)
        {

            this.name = name;
            this.abbreviation = abbreviation;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getAbbreviation()
        {
            return abbreviation;
        }

        public void setAbbreviation(String abbreviation)
        {
            this.abbreviation = abbreviation;
        }
    }
}


