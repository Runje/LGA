package com.example.thomas.lga.Finances;

/**
 * Created by Thomas on 06.09.2015.
 */
public enum Frequency
{
    weekly, Monthly, Yearly;

    public static Frequency indexToFrequency(int idx)
    {
        Frequency frequency = null;
        if (idx == 0)
        {
            frequency = Frequency.weekly;
        } else if (idx == 1)
        {
            frequency = Frequency.Monthly;
        } else if (idx == 2)
        {
            frequency = Frequency.Yearly;
        }
        return frequency;
    }

    public static int FrequencyToIndex(Frequency frequency)
    {
        switch (frequency)
        {
            case weekly:
                return 0;
            case Monthly:
                return 1;
            case Yearly:
                return 2;
        }

        return -1;
    }


}
