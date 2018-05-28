package com.log.sec.model;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.log.sec.util.DateTimeUtil;

@Test(groups = "unit")
public class UserSessionTest {

    private UserSession userSession;

    @BeforeMethod
    public void setUp() {
        userSession = new UserSession(111L);

        DateTimeUtil.ttlInSeconds = 10L;
    }

    @Test(enabled = true, description = "set valid date time")
    public void testSetEndDateTime() {
        final String strDate = "2018-04-05";
        final String strTime = "13:01:56";
        Date endDateTime = DateTimeUtil.parseDateTime(strDate, strTime);

        userSession.setEndDateTime(endDateTime);

        Assert.assertEquals(endDateTime.getTime(), userSession.getEndDateTime().getTime());

        Assert.assertEquals(1L + 10L + endDateTime.getTime() / 1000, userSession.getExpirationTimeInSeconds());

        // try to set the same ending date time info again
        Date endDateTime2 = DateTimeUtil.parseDateTime(strDate, strTime);
        userSession.setEndDateTime(endDateTime2);

        Assert.assertFalse(userSession.getEndDateTime() == endDateTime2);
        Assert.assertTrue(userSession.getEndDateTime() == endDateTime);
    }

    @Test(enabled = true, description = "test if comparison method works for sorting")
    public void testElementComparison() {
        Date date1 = DateTimeUtil.parseDateTime("2018-04-05", "13:01:01");
        Date date2 = DateTimeUtil.parseDateTime("2018-04-05", "13:01:03");
        Date date3 = DateTimeUtil.parseDateTime("2018-04-05", "13:01:05");

        UserSession session10 = new UserSession(10L);
        session10.setEndDateTime(date1);

        UserSession session11 = new UserSession(11L);
        session11.setEndDateTime(date1);

        UserSession session20 = new UserSession(20L);
        session20.setEndDateTime(date3);

        UserSession session50 = new UserSession(50L);
        session50.setEndDateTime(date2);

        TreeSet<UserSession> sortedItems = new TreeSet<UserSession>();

        sortedItems.add(session10);
        sortedItems.add(session20);
        sortedItems.add(session50);
        sortedItems.add(session11);

        List<UserSession> itemsInExpectedOrder = Arrays.asList(session10, session11, session50, session20);
        for (int i = 0; i < sortedItems.size(); i++) {
            UserSession item = sortedItems.pollFirst();
            Assert.assertEquals(itemsInExpectedOrder.get(i), item);
        }
    }
}
