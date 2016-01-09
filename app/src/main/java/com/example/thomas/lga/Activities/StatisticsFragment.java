package com.example.thomas.lga.Activities;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.Finances.Expenses;
import com.example.thomas.lga.Finances.FinanceUtilities;
import com.example.thomas.lga.Finances.Frequency;
import com.example.thomas.lga.Finances.OverviewItem;
import com.example.thomas.lga.Finances.StandingOrder;
import com.example.thomas.lga.Finances.TimeFrame;
import com.example.thomas.lga.Installation;
import com.example.thomas.lga.LGA;
import com.example.thomas.lga.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 05.01.2016.
 */
@EFragment(R.layout.fragment_statistics)
public class StatisticsFragment extends Fragment
{
    @ViewById
    public LineChart lineChart;
    @ViewById
    public BarChart barchart;
    @ViewById
    public RadioGroup radiogroup;
    @ViewById
    public PieChart piechart;
    @ViewById
    public Spinner spinnerWho;
    @ViewById
    public Spinner spinnerDate;
    public int checkedId = R.id.radioButton_ownings;
    private String LogKey = "StatisticsFragment";
    private List<String> spinnerWhoItems;
    private int spinnerWhoPosition;
    private int spinnerDatePosition;
    private ArrayList<String> spinnerDateItems;

    public StatisticsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context)
    {
        Log.d(LogKey, "On Attach Context");
        super.onAttach(context);
        //callback = (StandingOrderListener) context;
    }

    @AfterViews
    public void init()
    {
        spinnerWhoItems = LGA.getSingleton().getNames();
        spinnerWho.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, spinnerWhoItems));
        spinnerWho.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                switch (checkedId)
                {
                    case R.id.radioButton_ownings:
                        break;
                    case R.id.radioButton_compensation:
                        spinnerWhoPosition = position;
                        initCompensation();
                        break;
                    case R.id.radioButton_expenses:
                        spinnerWhoPosition = position;
                        initExpenses();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        spinnerDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                switch (checkedId)
                {
                    case R.id.radioButton_ownings:
                        break;
                    case R.id.radioButton_compensation:
                        spinnerDatePosition = position;
                        initCompensation();
                        break;
                    case R.id.radioButton_expenses:
                        spinnerDatePosition = position;
                        initExpenses();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {


            @Override
            public void onCheckedChanged(RadioGroup group, int id)
            {

                checkedId = id;
                switch (id)
                {
                    case R.id.radioButton_ownings:
                        barchart.setVisibility(View.GONE);
                        lineChart.setVisibility(View.VISIBLE);
                        spinnerWho.setVisibility(View.GONE);
                        spinnerDate.setVisibility(View.GONE);
                        piechart.setVisibility(View.GONE);
                        initOwnings();
                        break;
                    case R.id.radioButton_compensation:
                        barchart.setVisibility(View.VISIBLE);
                        lineChart.setVisibility(View.GONE);
                        spinnerWho.setVisibility(View.VISIBLE);
                        spinnerDate.setVisibility(View.GONE);
                        piechart.setVisibility(View.GONE);

                        spinnerWhoPosition = 0;
                        spinnerDatePosition = 0;

                        initCompensation();
                        break;
                    case R.id.radioButton_expenses:
                        barchart.setVisibility(View.GONE);
                        piechart.setVisibility(View.VISIBLE);
                        lineChart.setVisibility(View.GONE);
                        spinnerWho.setVisibility(View.VISIBLE);
                        DateTime start = LGA.getSingleton().getStartDate().withDayOfMonth(1);
                        DateTime stop = DateTime.now().withDayOfMonth(1);
                        int months = Months.monthsBetween(start, stop).getMonths();
                        ArrayList<String> dates = new ArrayList<String>();
                        dates.add(getContext().getResources().getString(R.string.all));
                        for (int i = 0; i < months + 1; i++)
                        {
                            dates.add(start.toString("MM/yy"));
                            start = start.plusMonths(1);
                        }

                        spinnerDateItems = dates;
                        spinnerWhoPosition = 0;
                        spinnerDatePosition = 0;
                        spinnerDate.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, spinnerDateItems));
                        initExpenses();
                        break;
                }
            }
        });
        initOwnings();
    }

    private void initExpenses()
    {
        List<String> categorys = SQLiteFinanceHandler.getAllCategorys(getContext());
        ArrayList<Entry> yVals = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();
        String dateString = spinnerDateItems.get(spinnerDatePosition);
        DateTime date = null;
        if (!dateString.equals(getContext().getResources().getString(R.string.all)))
        {
            date = DateTime.parse(dateString, DateTimeFormat.forPattern("MM/yy"));
        }
        String name = spinnerWhoItems.get(spinnerWhoPosition);
        float wholeCosts = 0;
        for (int i = 0; i < categorys.size(); i++)
        {
            List<Expenses> expensesList = SQLiteFinanceHandler.getExpensesFromCategoryAndMonth(getContext(), categorys.get(i), date);
            float costs = 0;
            for (Expenses expenses : expensesList)
            {
                if (expenses.getCosts() < 0)
                {
                    if (spinnerWhoPosition == 0 || spinnerWhoPosition == 1)
                    {
                        if (expenses.getUser().equals(name))
                        {
                            costs += expenses.getCosts();
                        }
                        if (expenses.getUser().equals(LGA.getSingleton().getNames().get(2)))
                        {
                            costs += expenses.getCosts() / 2;
                        }
                    } else
                    {
                        costs += expenses.getCosts();
                    }
                }
            }

            if (costs < 0)
            {
                yVals.add(new Entry(costs, i));
                xVals.add(categorys.get(i));
                wholeCosts += costs;
            }
        }

        DecimalFormat df = new DecimalFormat("#");
        PieDataSet dataSet = new PieDataSet(yVals, df.format(wholeCosts));
        piechart.setDescription(df.format(wholeCosts));
        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setValueTextColor(Color.WHITE);
        PieData data = new PieData(xVals, dataSet);
        piechart.setData(data);
        piechart.getLegend().setTextColor(Color.WHITE);
        piechart.setDescriptionColor(Color.WHITE);
        piechart.invalidate();
    }

    private void initCompensation()
    {
        DateTime start = LGA.getSingleton().getStartDate().withDayOfMonth(1);
        DateTime stop = DateTime.now().withDayOfMonth(1);
        int months = Months.monthsBetween(start, stop).getMonths();
        List<String> names = LGA.getSingleton().getNames();
        OverviewItem lastOwnings = FinanceUtilities.getOwningsFrom(getContext(), names, start, TimeFrame.Ownings);

        ArrayList<BarEntry> thomasYOwnings = new ArrayList<>();
        ArrayList<BarEntry> thomasYExpenses = new ArrayList<>();
        ArrayList<BarEntry> thomasYCompensation = new ArrayList<>();

        ArrayList<String> xVals = new ArrayList<>();
        String name = names.get(spinnerWhoPosition);
        for (int i = 0; i < months; i++)
        {
            DateTime date = start.plusMonths(1);
            xVals.add(start.toString("MM/yy"));
            OverviewItem ownings = FinanceUtilities.getOwningsFrom(getContext(), names, date, TimeFrame.Ownings);
            OverviewItem lastExpenses = FinanceUtilities.getExpensesBetween(getContext(), names, start, date);
            Expenses compensation = SQLiteFinanceHandler.getCompensationFrom(getContext(), name, start);
            if (name.equals(LGA.getSingleton().getNames().get(2)))
            {
                Expenses compensation1 = SQLiteFinanceHandler.getCompensationFrom(getContext(), names.get(0), start);
                Expenses compensation2 = SQLiteFinanceHandler.getCompensationFrom(getContext(), names.get(1), start);
                compensation = compensation1;
                compensation.setCosts(compensation1.getCosts() + compensation2.getCosts());
            }
            float owningsDiff = 0;
            float expensesWithoutCompensation = 0;
            switch (spinnerWhoPosition)
            {
                case 0:
                    owningsDiff = ownings.getP1() - lastOwnings.getP1();
                    expensesWithoutCompensation = lastExpenses.getP1() - compensation.getCosts();
                    break;
                case 1:
                    owningsDiff = ownings.getP2() - lastOwnings.getP2();
                    expensesWithoutCompensation = lastExpenses.getP2() - compensation.getCosts();
                    break;
                case 2:
                    owningsDiff = ownings.getAll() - lastOwnings.getAll();
                    expensesWithoutCompensation = lastExpenses.getAll() - compensation.getCosts();
                    break;
            }
            thomasYOwnings.add(new BarEntry(owningsDiff, i));
            thomasYExpenses.add(new BarEntry(expensesWithoutCompensation, i));
            thomasYCompensation.add(new BarEntry(compensation.getCosts(), i));

            start = date;
            lastOwnings = ownings;
        }

        BarDataSet thomasOwningsSet = new BarDataSet(thomasYOwnings, getContext().getResources().getString(R.string.ownings));
        thomasOwningsSet.setColor(Color.RED);
        thomasOwningsSet.setValueTextColor(Color.WHITE);
        BarDataSet thomasCompensationSet = new BarDataSet(thomasYCompensation, getContext().getResources().getString(R.string.compensation));
        thomasCompensationSet.setColor(Color.GREEN);
        thomasCompensationSet.setValueTextColor(Color.WHITE);
        BarDataSet thomasExpensesSet = new BarDataSet(thomasYExpenses, getContext().getResources().getString(R.string.expenses));
        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(thomasExpensesSet);
        dataSets.add(thomasOwningsSet);
        dataSets.add(thomasCompensationSet);

        BarData barData = new BarData(xVals, dataSets);
        barchart.setData(barData);
        barchart.setDescriptionColor(Color.WHITE);
        barchart.getAxisRight().setTextColor(Color.WHITE);
        barchart.getAxisLeft().setTextColor(Color.WHITE);
        barchart.getXAxis().setTextColor(Color.WHITE);
        barchart.getLegend().setTextColor(Color.WHITE);
        barchart.setGridBackgroundColor(Color.BLACK);
        barchart.invalidate();
    }

    private void initOwnings()
    {
        List<DateTime> dates = FinanceUtilities.getDueDates(new StandingOrder(0, "", 0, "dummy", "", "", LGA.getSingleton().getStartDate(), DateTime.now(), Frequency.Monthly, 1, Installation.id(getContext())));
        ArrayList<Entry> ThomasVals = new ArrayList<>();
        ArrayList<Entry> MilenasVals = new ArrayList<>();
        ArrayList<Entry> allVals = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();
        List<String> names = LGA.getSingleton().getNames();
        int index = 0;
        for (DateTime date : dates)
        {
            OverviewItem ownings = FinanceUtilities.getOwningsFrom(getContext(), names, date, TimeFrame.Ownings);
            ThomasVals.add(new Entry(ownings.getP1(), index));
            MilenasVals.add(new Entry(ownings.getP2(), index));
            allVals.add(new Entry(ownings.getAll(), index));
            xVals.add(date.toString("MM/yy"));
            index++;
        }
        Log.i(LogKey, "Drawing data");


        LineDataSet func1 = new LineDataSet(ThomasVals, names.get(0));
        func1.setAxisDependency(YAxis.AxisDependency.LEFT);
        func1.setColor(Color.WHITE);
        func1.setValueTextColor(getResources().getColor(R.color.text_list));
        LineDataSet func2 = new LineDataSet(MilenasVals, names.get(1));
        func2.setColor(Color.GREEN);
        func2.setAxisDependency(YAxis.AxisDependency.LEFT);
        func2.setValueTextColor(getResources().getColor(R.color.text_list));
        LineDataSet func3 = new LineDataSet(allVals, names.get(2));
        func3.setAxisDependency(YAxis.AxisDependency.LEFT);
        func3.setColor(Color.RED);
        func3.setValueTextColor(getResources().getColor(R.color.text_list));
        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(func1);
        dataSets.add(func2);
        dataSets.add(func3);

        lineChart.setData(new LineData(xVals, dataSets));
        lineChart.setDescription(getContext().getString(R.string.ownings));
        lineChart.setDescriptionColor(getContext().getResources().getColor(R.color.text_list));
        lineChart.setGridBackgroundColor(getContext().getResources().getColor(R.color.background_header));
        lineChart.getXAxis().setTextColor(getResources().getColor(R.color.text_list));
        lineChart.getAxisLeft().setTextColor(getResources().getColor(R.color.text_list));
        lineChart.getAxisRight().setTextColor(getResources().getColor(R.color.text_list));
        lineChart.getLegend().setTextColor(getResources().getColor(R.color.text_list));
        lineChart.invalidate();
    }


}
