package com.log.sec.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.log.sec.model.UserSession;

@Test(groups = "unit")
public class ComparatorByListOrderTest {

    private ComparatorByListOrder comparator;

    @BeforeMethod
    public void setUp() {
        this.comparator = new ComparatorByListOrder();
    }

    @Test(enabled = true, description = "test sorting with ComparatorByListOrder")
    public void testComparatorForSorting() {
        Date date1 = DateTimeUtil.parseDateTime("2018-04-05", "13:01:01");
        Date date2 = DateTimeUtil.parseDateTime("2018-04-05", "13:01:03");
        Date date3 = DateTimeUtil.parseDateTime("2018-04-05", "13:01:05");

        UserSession session10 = new UserSession(10L);
        session10.setStartDateTime(date1);

        UserSession session11 = new UserSession(11L);
        session11.setStartDateTime(date1);

        UserSession session20 = new UserSession(20L);
        session20.setStartDateTime(date3);

        UserSession session50 = new UserSession(50L);
        session50.setStartDateTime(date2);

        List<UserSession> items = new LinkedList<UserSession>();

        items.add(session50);
        items.add(session11);
        items.add(session10);
        items.add(session20);

        Collections.sort(items, this.comparator);

        // They should be sorted in the order of startDateTime and serialNumber
        List<UserSession> itemsInExpectedOrder = Arrays.asList(session10, session11, session50, session20);

        int count = 0;
        for (UserSession item : items) {
            Assert.assertEquals(itemsInExpectedOrder.get(count++), item);
        }
    }
}
