package com.example.ai_bsrs.notification_module;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.ai_bsrs.notification_module.DateTimeListener;

import java.util.Calendar;
import java.util.Date;

public class DateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private DateTimeListener callback;
    int year, month, day;
    Date weekOfDay;

    public DateFragment(DateTimeListener callback) {
        // Required empty public constructor
        this.callback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getContext(), this, year, day, month);

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        String newDate = dayOfMonth + " / " + (month+1) + " / " + year;
        this.callback.selectDate(newDate);
    }

}

