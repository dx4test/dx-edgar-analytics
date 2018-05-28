package com.log.sec.util;

import java.util.Comparator;

import com.log.sec.model.UserSession;

/**
 * This comparator is used to sort a collection of UserSession items in the ascending order in which
 * original request records are listed inside input data file. That is, they are sorted by
 * startDateTime and SerialNumber.
 */
public class ComparatorByListOrder implements Comparator<UserSession> {

    @Override
    public int compare(UserSession o1, UserSession o2) {

        // NOTE: if a object is null, let's treat its startDateTime as "huge/infinity".
        int nRet = 0;
        if (o1 == null && o2 == null) {
            nRet = 0;
        } else if (o1 == null) {
            nRet = 1;
        } else if (o2 == null) {
            nRet = -1;
        } else { // both o1 and o2 are not null
            long dt = o1.getStartDateTime().getTime() - o2.getStartDateTime().getTime();
            if (dt < 0L) {
                nRet = -1;
            } else if (dt > 0L) {
                nRet = 1;
            } else {
                // they have the same start date and time, so need compare their serial
                // numbers further!
                if (o1.getSerialNumber() < o2.getSerialNumber())
                    nRet = -1;
                else if (o1.getSerialNumber() > o2.getSerialNumber())
                    nRet = 1;
                else
                    nRet = 0;
            }
        }

        return nRet;
    }

}
