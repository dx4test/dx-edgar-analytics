package com.log.sec.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtil {
    public static long ttlInSeconds;

    public static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // example:
    // 117.91.230.gha,2017-06-30,13:20:56,0.0,1082923.0,0001140361-17-023097,-index.htm,200.0,6335.0,1.0,0.0,0.0,10.0,0.0,

    /**
     * Insight docs READ.md says:
     * "Note that the fields we are interested in are in bold below but will not be like that in the input file"
     *
     * but the "zone" field is not in bold, so we have no need to consider different time zone while
     * parsing date time. (According to SEC web pages, the zone is the time zone associated with the
     * server that completed processing the request).
     *
     * @param strDate: date info in format of yyyy-MM-dd.
     * @param strTime: time info in format of HH-mm-ss.
     * @return Date object.
     */
    public static Date parseDateTime(String strDate, String strTime) {
        if (strDate == null || strDate.isEmpty())
            return null;

        if (strTime == null || strTime.isEmpty())
            return null;

        Date theDate = null;
        try {
            // let's ignore time zone.
            String strDateTime = strDate + " " + strTime;
            theDate = dateTimeFormatter.parse(strDateTime);
        } catch (ParseException e) {
            System.out.println("error: failed to parse date from input info: " + strDate + " " + strTime);
        }

        return theDate;
    }

    /**
     * format Date object into a string in format "yyyy-MM-dd HH:mm:ss".
     *
     * @param theDate: a Date object.
     * @return: string of date and time in pre-selected format.
     */
    public static String formatDateTime(Date theDate) {
        if (theDate == null)
            return "";

        return dateTimeFormatter.format(theDate);
    }

    public static void testDateTimeParser() {
        String strDate = "2014-10-05";
        String strTime = "15:23:01";

        Date theDate = DateTimeUtil.parseDateTime(strDate, strTime);

        System.out.println(theDate);

        System.out.println("original date time: " + (strDate + " " + strTime) + "; parsed date time: " + dateTimeFormatter.format(theDate));

        long timeInSeconds = theDate.getTime() / 1000;
        System.out.println("timeInSeconds: " + timeInSeconds);

        long timeInMS = theDate.getTime();
        Date theDate2 = new Date(timeInMS);
        System.out.println("theDate2: " + theDate2 + "; " + dateTimeFormatter.format(theDate2));
    }
}
