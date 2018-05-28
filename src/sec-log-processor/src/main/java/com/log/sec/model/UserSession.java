package com.log.sec.model;

import java.util.Date;

import com.log.sec.util.DateTimeUtil;

public class UserSession implements Comparable<UserSession> {

    /*
     * serialNumber is used to distinguish two or more UserSession objects having the same
     * endDateTime and/or startDateTime.
     */
    private long serialNumber;

	private String ip;
	private int docCount;

	/*
	 * combine date and time info of a request into java Date object.
	 */
	private Date startDateTime;
	private Date endDateTime;

    /*
     * Since a user session will not be expired until the inactivity period is over, its expiration
     * time in seconds must be greater than sum of endDateTime in seconds and TTL (inactivity
     * period).
     */
    private long expirationTimeInSeconds;

    public UserSession(long serialNumber) {
        this.serialNumber = serialNumber;
    }

	public String getIP() {
		return this.ip;
	}

	public void setIP(String ip) {
		this.ip = ip;
	}

	public long getSerialNumber() {
		return this.serialNumber;
	}

    public int increaseDocCountByOne() {
        this.docCount += 1;
        return this.docCount;
    }

	public int getDocCount() {
		return this.docCount;
	}

	public Date getStartDateTime() {
		return this.startDateTime;
	}

	public void setStartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
	}

	public Date getEndDateTime() {
		return this.endDateTime;
	}

	public void setEndDateTime(Date endDateTime) {
        if (this.endDateTime != null && endDateTime != null) {
            if (this.endDateTime.getTime() == endDateTime.getTime()) {
                return;
            }
        }

		this.endDateTime = endDateTime;

        if (endDateTime != null) {
            // One extra second is added because a user session is not expired until inactivity
            // period is over.
            long expTimeInSec = endDateTime.getTime() / 1000 + DateTimeUtil.ttlInSeconds + 1;

            this.setExpirationTimeInSeconds(expTimeInSec);
        }
	}

    public boolean isExpiredInTermOfCurrentTime(final Date currentDateTime) {
        if (currentDateTime == null) {
            return false;
        }

        boolean bRet = false;
        if ((currentDateTime.getTime() / 1000) >= this.expirationTimeInSeconds) {
            bRet = true;
        }

        return bRet;
    }

    public long getExpirationTimeInSeconds() {
        return this.expirationTimeInSeconds;
	}

    private void setExpirationTimeInSeconds(long expirationTimeInSeconds) {
        this.expirationTimeInSeconds = expirationTimeInSeconds;
	}

    @Override
    public int compareTo(UserSession obj) {
        if (obj == null) {
            throw new NullPointerException("input object is null");
        }

        int nRet;
        long dt = this.expirationTimeInSeconds - obj.expirationTimeInSeconds;
        if (dt < 0L)
            nRet = -1;
        else if (dt > 0L)
            nRet = 1;
        else {
            if (this.serialNumber < obj.serialNumber)
                nRet = -1;
            else if (this.serialNumber > obj.serialNumber)
                nRet = 1;
            else
                nRet = 0;
        }

        return nRet;
    }

    public long getDuration() {

        if (this.startDateTime == null || this.endDateTime == null)
            return -1L;

        long durationInSeconds = (this.endDateTime.getTime() - this.startDateTime.getTime()) / 1000;
        durationInSeconds += 1;

        return durationInSeconds;
    }

    /**
     * output only the following 5 fields that we care about.
     *
     * @return 5 fields (ip, date time of the 1st request, data time of the last request, duration,
     *         count)
     */
    public String toResultRecordString() {
        StringBuilder sb = new StringBuilder();

        sb.append(ip).append(',');
        sb.append(DateTimeUtil.formatDateTime(this.startDateTime)).append(',');
        sb.append(DateTimeUtil.formatDateTime(this.endDateTime)).append(',');

        sb.append(this.getDuration()).append(',');
        sb.append(this.docCount);

        return sb.toString();
    }
}
