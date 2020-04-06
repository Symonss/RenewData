package com.dennohpeter.renewdata;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class DateUtil {


    String getFormattedDate(long date){
        return  getFormattedDate(new Date(date));
    }

    String getFormattedDate(Date date){
        return new SimpleDateFormat("MMM dd, yyyy\nhh:mm:ss", Locale.getDefault()).format(date);
    }
}
