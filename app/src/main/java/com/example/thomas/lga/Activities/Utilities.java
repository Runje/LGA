package com.example.thomas.lga.Activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.Finances.Expenses;
import com.example.thomas.lga.Finances.FinanceUtilities;
import com.example.thomas.lga.Finances.StandingOrder;
import com.example.thomas.lga.R;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Created by Thomas on 06.09.2015.
 */
public class Utilities
{
    public static final File BACKUP_PATH = new File(Environment.getExternalStorageDirectory(), "LGA");

    public static DateTime getDateFromDatePicker(DatePicker datePicker)
    {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();

        return new DateTime(year, month, day, 0, 0, 0);
    }

    public static void setDateToDatePicker(DatePicker datePicker, DateTime time)
    {
        int year = time.getYear();
        int month = time.getMonthOfYear() - 1;      // Need to subtract 1 here.
        int day = time.getDayOfMonth();

        datePicker.updateDate(year, month, day);
    }

    public static File exportDB(Context context, String dbName, String backupName)
    {
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;
        String currentDBPath = "/data/" + context.getPackageName() + "/databases/" + dbName;
        String backupDBPath = backupName;
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(BACKUP_PATH, backupDBPath);
        try
        {
            if (!BACKUP_PATH.exists())
            {
                BACKUP_PATH.mkdirs();
            }
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Toast.makeText(context, R.string.success_db_export, Toast.LENGTH_LONG).show();
            return backupDB;
        } catch (IOException e)
        {
            e.printStackTrace();
            Toast.makeText(context, R.string.error_db_export, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    public static void importDB(Context context, File backupFile, String db_name)
    {
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;
        String currentDBPath = "/data/" + context.getPackageName() + "/databases/" + db_name;
        File currentDB = new File(data, currentDBPath);
        try
        {
            source = new FileInputStream(backupFile).getChannel();
            destination = new FileOutputStream(currentDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            List<String> categorys = SQLiteFinanceHandler.getAllCategorys(context);
            for (String category : categorys)
            {
                FinanceUtilities.addNewCategory(context, category);
            }

            Toast.makeText(context, R.string.success_db_import, Toast.LENGTH_LONG).show();
        } catch (IOException e)
        {
            e.printStackTrace();
            Toast.makeText(context, R.string.error_db_import, Toast.LENGTH_LONG).show();
        }
    }

    public static void sendFilePerMail(Context context, File file, String email)
    {
        try
        {
            File sd = Environment.getExternalStorageDirectory();
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "LGA BACKUP " + DateTime.now().toString("yyyy.MM.dd"));
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            //intent.putExtra(Intent.EXTRA_TEXT, "test");
            if (email != null)
            {
                intent.setData(Uri.parse("mailto:" + email));
            } else
            {
                intent.setData(Uri.parse("mailto:"));
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


            context.startActivity(intent);
        } catch (Exception e)
        {
            System.out.println("is exception raises during sending mail" + e);
        }
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException
    {
        if ("content".equalsIgnoreCase(uri.getScheme()))
        {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try
            {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst())
                {
                    return cursor.getString(column_index);
                }
            } catch (Exception e)
            {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme()))
        {
            return uri.getPath();
        }

        return null;
    }

    public static Expenses createExpensesFromStandingOrder(StandingOrder order, DateTime date, String id)
    {
        return new Expenses(order.getWho(), order.getCosts(), order.getName(), order.getCategory(), order.getUser(), date, true, id, id);
    }

    public static void clickOn(final View view)
    {
        view.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
            }
        }, 100);

    }

    public static boolean areSameDay(DateTime date, DateTime date2)
    {
        return date.getYear() == date2.getYear() && date.getMonthOfYear() == date2.getMonthOfYear() && date.getDayOfMonth() == date2.getDayOfMonth();
    }

    public static void adaptListRow(View convertView, int position)
    {
        Context context = convertView.getContext();
        convertView.setBackgroundColor((position % 2 == 0) ? context.getResources().getColor(R.color.background_list) : context.getResources().getColor(R.color.background_list2));
    }
}
