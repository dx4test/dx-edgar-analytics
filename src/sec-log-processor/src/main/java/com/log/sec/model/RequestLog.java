package com.log.sec.model;

import java.util.Date;

/**
 * This class maintains only those fields in which we're interested.
 */
public class RequestLog {
    private String ip;
    private Date dateTime;

    public RequestLog(String ip, Date dateTime) {
        this.ip = ip;
        this.dateTime = dateTime;
    }

    public String getIP() {
        return this.ip;
    }

    public Date getDateTime() {
        return this.dateTime;
    }
}
