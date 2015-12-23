package com.example.thomas.lga.Views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.thomas.lga.Activities.Utilities;
import com.example.thomas.lga.LGA;
import com.example.thomas.lga.R;

/**
 * Created by Thomas on 13.09.2015.
 */
public class PINDialog
{
    private final onExitListener callback;
    private final String pin;
    private final Context context;

    public PINDialog(Context context, String pin, onExitListener listener)
    {
        this.context = context;
        this.callback = listener;
        this.pin = pin;
    }

    public void show()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.pin_dialog, null);
        final EditText editPin = (EditText) layout.findViewById(R.id.edit_pin);
        builder.setView(layout);
        builder.setTitle(R.string.enter_pin);
        builder.setNeutralButton(R.string.demo, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (callback != null)
                {
                    LGA.getSingleton().setAuthorized(false);
                    callback.onUseDemoData();
                }
            }
        });
        builder.setPositiveButton(R.string.ok, null);

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                LGA.getSingleton().setAuthorized(false);
                callback.onCancel();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
        Utilities.adaptDialogSize(dialog, 6 / 7f, 0.5f);
        Utilities.clickOn(editPin);


        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (callback != null)
                {
                    String enteredPin = editPin.getText().toString();
                    if (enteredPin.equals(pin))
                    {
                        LGA.getSingleton().setAuthorized(true);
                        callback.onExit();
                        dialog.dismiss();
                    } else
                    {
                        editPin.setText("");
                        editPin.setEnabled(false);
                        editPin.postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                editPin.setEnabled(true);
                            }
                        }, 3000);
                        Toast.makeText(context, R.string.wrong_pin, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public interface onExitListener
    {
        void onExit();

        void onUseDemoData();

        void onCancel();
    }
}
