package com.example.thomas.lga.Finances;

/**
 * Created by Thomas on 12.09.2015.
 */
public class OverviewItem
{
    private TimeFrame time;
    private float all;
    private float p1;
    private float p2;

    public OverviewItem(TimeFrame time, float all, float p1, float p2)
    {
        this.time = time;
        this.all = all;
        this.p1 = p1;
        this.p2 = p2;
    }

    public OverviewItem(TimeFrame timeFrame)
    {
        this.time = timeFrame;
    }

    public TimeFrame getTime()
    {

        return time;
    }

    public void setTime(TimeFrame time)
    {
        this.time = time;
    }

    public float getAll()
    {
        return all;
    }

    public void setAll(float all)
    {
        this.all = all;
    }

    public float getP1()
    {
        return p1;
    }

    public void setP1(float p1)
    {
        this.p1 = p1;
    }

    public float getP2()
    {
        return p2;
    }

    public void setP2(float p2)
    {
        this.p2 = p2;
    }

    public void addP1(float costs)
    {
        p1 += costs;
        all += costs;
    }

    public void addP2(float costs)
    {
        p2 += costs;
        all += costs;
    }

    public void addAll(float costs)
    {
        all += costs;
    }

    public void divide(int nu)
    {
        p1 /= nu;
        p2 /= nu;
        all /= nu;
    }
}
