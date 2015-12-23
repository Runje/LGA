package com.example.thomas.lga.Views.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.thomas.lga.Finances.Expenses;
import com.example.thomas.lga.LGA;
import com.example.thomas.lga.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Thomas on 08.09.2015.
 */
public class ExpensesAdapter extends BaseAdapter
{
    protected final Context context;
    protected ExpensesDeleteListener callback;
    protected boolean showDeleteButton;
    private List<Expenses> expenses;


    public ExpensesAdapter(Context context, ExpensesDeleteListener listener)
    {
        this.context = context;
        this.callback = listener;
        updateExpenses();
    }

    public ExpensesAdapter(Context context)
    {
        this(context, null);
    }

    public void updateExpenses()
    {
        if (expenses == null)
        {
            return;
        }

        Collections.sort(expenses, new Comparator<Expenses>()
        {
            @Override
            public int compare(Expenses lhs, Expenses rhs)
            {
                return rhs.getDate().compareTo(lhs.getDate());
            }
        });
        ((Activity) context).runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount()
    {
        return expenses.size();
    }

    @Override
    public Object getItem(int position)
    {
        return expenses.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return expenses.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final Expenses ex = expenses.get(position);
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expenses_item, null);
            convertView.setLongClickable(true);
            //Utilities.adaptListRow(convertView, position);
        }

        final Button delete = (Button) convertView.findViewById(R.id.button_delete);
        delete.setVisibility(showDeleteButton ? View.VISIBLE : View.GONE);
        delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                delete(ex);
            }
        });


        TextView person = (TextView) convertView.findViewById(R.id.text_who);
        TextView name = (TextView) convertView.findViewById(R.id.text_name);
        TextView category = (TextView) convertView.findViewById(R.id.text_category);
        TextView costs = (TextView) convertView.findViewById(R.id.text_costs);
        TextView date = (TextView) convertView.findViewById(R.id.text_date);
        TextView user = (TextView) convertView.findViewById(R.id.text_user);

        person.setText(LGA.getSingleton().getAbbreviation(ex.getWho()));
        name.setText(ex.getName());
        category.setText(ex.getCategory().toString());
        costs.setText(Float.toString(ex.getCosts()));
        if (ex.getCosts() >= 0)
        {
            costs.setTextColor(ContextCompat.getColor(context, R.color.positive_highlight));
        } else
        {
            costs.setTextColor(ContextCompat.getColor(context, R.color.negative_highlight));
        }

        date.setText(ex.getDate().toString("dd.MM.yy"));
        user.setText(LGA.getSingleton().getAbbreviation(ex.getUser()));

        return convertView;
    }

    protected void delete(final Expenses ex)
    {

    }

    public void setExpenses(List<Expenses> expenses)
    {
        this.expenses = expenses;
    }

    public interface ExpensesDeleteListener
    {
        void onDelete();
    }
}
