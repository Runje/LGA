package com.example.thomas.lga.Database;

import java.util.List;

/**
 * Created by Thomas on 07.01.2016.
 */
public class Filter
{
    public static final int ALL = 0;
    public static final int YES = 1;
    public static final int NO = 2;
    List<String> categorys;
    int standingOrder;

    public List<String> getCategorys()
    {
        return categorys;
    }

    public void setCategorys(List<String> categorys)
    {
        this.categorys = categorys;
    }

    public int getStandingOrder()
    {
        return standingOrder;
    }

    public void setStandingOrder(int standingOrder)
    {
        this.standingOrder = standingOrder;
    }
}
