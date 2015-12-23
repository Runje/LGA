package com.example.thomas.lga.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.thomas.lga.Database.SQLiteFinanceHandler;
import com.example.thomas.lga.LGA;
import com.example.thomas.lga.R;

public class MainActivity extends AppCompatActivity
{

    public static String SharedPreferencesName = "SharedPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LGA.init(this);
        LGA.getSingleton().setAuthorized(true);
        new SQLiteFinanceHandler(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void click_options(View view)
    {


    }

    public void click_cleanPlanner(View view)
    {

    }

    public void click_owning(View view)
    {

    }

    public void click_finance(View view)
    {
        Intent intent = new Intent(MainActivity.this, FinanceActivity.class);
        startActivity(intent);
    }
}
