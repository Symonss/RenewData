package com.dennohpeter.renewdata;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/*
 * Houses commonly used date functions
 */
class DateUtil {


    String getFormattedDate(long date) {
        return getFormattedDate(new Date(date));
    }

    private String getFormattedDate(Date date) {
        return new SimpleDateFormat("MMM dd, yyyy\nhh:mm:ss", Locale.getDefault()).format(date);
    }

    // returns Current date format in  YYYMMDDHHmmss.
    String timestamp() {
        return new SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault()).format(new Date());
    }


    // Adds 24 hours to the given date
    private Date add24Hours(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, 24);
        return calendar.getTime();
    }

    long add24Hours(long date) {
        return add24Hours(new Date(date)).getTime();
    }

    long currentDate() {
        return Calendar.getInstance().getTimeInMillis();
    }
}
