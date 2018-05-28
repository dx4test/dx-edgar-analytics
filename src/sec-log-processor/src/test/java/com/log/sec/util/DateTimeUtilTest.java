package com.log.sec.util;

import java.text.ParseException;
import java.util.Date;

import junit.framework.Assert;

import org.testng.annotations.Test;

@Test(groups = "unit")
public class DateTimeUtilTest {

    @Test(enabled = true, description = "parse valid input data")
    public void testParseDateTime() {
        String strDate = "2014-10-05";
        String strTime = "15:23:01";

        Date theDate = DateTimeUtil.parseDateTime(strDate, strTime);
        Assert.assertNotNull(theDate);

        String dateTimeString = DateTimeUtil.dateTimeFormatter.format(theDate);
        Assert.assertTrue((strDate + " " + strTime).equals(dateTimeString));
    }

    @Test(enabled = true, description = "parse non date or time info")
    public void testParseDateTime_2() {
        String strDate = "year2014-10-05";
        String strTime = "time15";

        try {
            DateTimeUtil.parseDateTime(strDate, strTime);
        } catch (Exception e) {
            if (e instanceof ParseException) {
                // good!
            } else {
                Assert.fail("unexpected exception thrown while parsing non-date time info");
            }
        }
    }

    @Test(enabled = true, description = "parse date or time info which are not in specified format")
    public void testParseDateTime_3() {
        String strDate = "2014/10/5";
        String strTime = "15:1:9";

        try {
            DateTimeUtil.parseDateTime(strDate, strTime);
        } catch (Exception e) {
            if (e instanceof ParseException) {
                // good!
            } else {
                Assert.fail("unexpected exception thrown while parsing date time info in non-pecified format");
            }
        }
    }

    @Test(enabled = true, description = "inputs are null or empty")
    public void testParseDateTime_4() {

        Date theDate1 = DateTimeUtil.parseDateTime("2014-10-05", "");
        Assert.assertNull(theDate1);

        Date theDate2 = DateTimeUtil.parseDateTime(null, "01:23:09");
        Assert.assertNull(theDate2);

        Date theDate3 = DateTimeUtil.parseDateTime("", null);
        Assert.assertNull(theDate3);
    }
}
