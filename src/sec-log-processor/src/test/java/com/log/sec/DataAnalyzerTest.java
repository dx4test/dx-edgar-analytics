package com.log.sec;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.log.sec.model.RequestLog;
import com.log.sec.util.DateTimeUtil;

@Test(groups = "unit")
public class DataAnalyzerTest {

    private DataAnalyzer analyzer;

    @BeforeMethod
    public void setUp() {
        analyzer = new DataAnalyzer("", "", "");

        DateTimeUtil.ttlInSeconds = 10L;
    }

    @Test(enabled = true, description = "test parsing different input records")
    public void testParseRecordLineIntoRequestLog() {

        // required field(s) is missing
        String strLine = "38.140.198.dgd,2017-06-30";
        RequestLog reqLog = this.analyzer.parseRecordLineIntoRequestLog(strLine);
        Assert.assertNull(reqLog);

        // invalid date or time field
        strLine = "abc,2017-6-3,10:20,0.0,917851.0,";
        reqLog = this.analyzer.parseRecordLineIntoRequestLog(strLine);
        Assert.assertNotNull(reqLog);
        Assert.assertNull(reqLog.getDateTime());

        // well-formatted valid record
        strLine = "abc,2017-06-03,10:20:03,0.0,917851.0,";
        reqLog = this.analyzer.parseRecordLineIntoRequestLog(strLine);
        Assert.assertNotNull(reqLog);
        Assert.assertNotNull(reqLog.getDateTime());

        // not well-formatted but still valid record
        strLine = "abc,2017-6-03 , 10:20:3, 0.0,917851.0,";
        RequestLog reqLog2 = this.analyzer.parseRecordLineIntoRequestLog(strLine);
        Assert.assertNotNull(reqLog2);
        Assert.assertNotNull(reqLog2.getDateTime());

        Assert.assertEquals(reqLog.getDateTime().getTime(), reqLog2.getDateTime().getTime());
    }
}
