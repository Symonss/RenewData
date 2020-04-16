package com.dennohpeter.renewdata;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/*
 * Houses commonly used date functions
 */
class DateUtil {


    String getFormattedDate(long date) {
        return getFormattedDate(new Date(date));
    }

    String getFormattedDate(Date date) {
        return new SimpleDateFormat("MMM dd, yyyy\nhh:mm:ss", Locale.getDefault()).format(date);
    }

    long toMinutes(long millis) {
        return TimeUnit.MILLISECONDS.toMinutes(millis);
    }

    long toMillis(long minutes) {
        return TimeUnit.MINUTES.toMillis(minutes);
    }

    // returns Current date format in  YYYMMDDHHmmss.
    String timestamp() {
        return new SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault()).format(new Date());
    }


    // Adds 24 hours to the given date
    Date add24Hours(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, 24);
        return calendar.getTime();
    }
    long add24Hours(long date){
        return add24Hours(new Date(date)).getTime();
    }
    long currentDate(){
        return Calendar.getInstance().getTimeInMillis();
    }
}
