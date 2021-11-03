package com.example.ai_bsrs.notification_module;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import com.example.ai_bsrs.notification_module.DateTimeListener;

import java.util.Calendar;

public class TimeFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private DateTimeListener callback;

    public TimeFragment (DateTimeListener callback){
        this.callback = callback;
    }


    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker

        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String amPm = "";
        if (hourOfDay>=12){
            hourOfDay-=12;
            amPm = "PM";
        }
        else
            amPm = "AM";
        String time = (String.format("%02d:%02d", hourOfDay, minute)+amPm);
        this.callback.selectTime(time);
    }
}

