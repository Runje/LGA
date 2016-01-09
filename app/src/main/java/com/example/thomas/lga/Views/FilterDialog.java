package com.example.thomas.lga.Views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;

import com.example.thomas.lga.Database.Filter;
import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 05.01.2016.
 */
public class FilterDialog
{
    private final Context context;
    private FilterListener callback;

    public FilterDialog(Context context, FilterListener listener)
    {
        this.context = context;
        this.callback = listener;
    }

    public void show()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.filter_dialog, null);
        final ListView listView = (ListView) layout.findViewById(R.id.listView);
        final List<String> categorys = SQLiteFinanceHandler.getAllCategorys(context);
        ArrayAdapter adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, categorys);
        final boolean[] selected = new boolean[categorys.size()];
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                selected[position] = !selected[position];

                if (selected[position])
                {
                    view.setBackgroundColor(Color.RED);
                } else
                {
                    view.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });
        builder.setView(layout);
        builder.setTitle(R.string.filter);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                List<String> selectedCategorys = new ArrayList<String>();
                for (int i = 0; i < categorys.size(); i++)
                {
                    if (selected[i])
                    {
                        selectedCategorys.add(categorys.get(i));
                    }
                }

                Filter filter = new Filter();
                filter.setCategorys(selectedCategorys);

                RadioButton buttonAll = (RadioButton) layout.findViewById(R.id.radioButton_all);
                if (buttonAll.isChecked())
                {
                    filter.setStandingOrder(Filter.ALL);
                }

                RadioButton buttonYes = (RadioButton) layout.findViewById(R.id.radioButton_yes);
                if (buttonYes.isChecked())
                {
                    filter.setStandingOrder(Filter.YES);
                }

                RadioButton buttonNo = (RadioButton) layout.findViewById(R.id.radioButton_no);
                if (buttonNo.isChecked())
                {
                    filter.setStandingOrder(Filter.NO);
                }

                callback.onSelectCategorys(filter);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.setNeutralButton(R.string.delete_filter, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                callback.onSelectCategorys(new Filter());
            }
        });
        builder.create().show();
    }

    public interface FilterListener
    {
        void onSelectCategorys(Filter filter);
    }
}
