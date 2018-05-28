package com.log.sec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import com.log.sec.model.RequestLog;
import com.log.sec.model.UserSession;
import com.log.sec.util.ComparatorByListOrder;
import com.log.sec.util.DateTimeUtil;


public class DataAnalyzer {

    // NOTE: they are used only for quick testing during development
    private static final String ttlFilePath0 = "/Users/dxiong/Downloads/4zzz/xzxz/edgar-weblogs/log20170630/dx-inactivity_period.txt";
    private static final String inputFilePath0 = "/Users/dxiong/Downloads/4zzz/xzxz/edgar-weblogs/log20170630/dx-log.csv";
    private static final String outputFilePath0 = "/Users/dxiong/Downloads/4zzz/xzxz/edgar-weblogs/log20170630/dx-sessionization.txt";

    public static void main(String[] args) {

        // DateTimeUtil.testDateTimeParser();
        // System.exit(0);

        DataAnalyzer analyzer = null;
        int nArgs = args.length;

        System.out.println("number of arguments to main method: " + nArgs);

        if (nArgs == 0) {
            analyzer = new DataAnalyzer(ttlFilePath0, inputFilePath0, outputFilePath0);
        } else if (nArgs == 3) {
            analyzer = new DataAnalyzer(args[0], args[1], args[2]);
        } else {
            System.out
                    .println("USAGE: java -cp sec-log-processor.jar com.log.sec.DataAnalyzer <path for inactivity_period.txt> <path for log.csv> <path for sessionization.txt>");
            System.exit(0);
        }

        if (!analyzer.init()) {
            System.out.println("error: failed to input files");
            return;
        } else {
            System.out.println("inactivity period in seconds: " + DateTimeUtil.ttlInSeconds);
        }

        try {
            analyzer.processRequestRecords();
        } catch (IOException e) {
            System.out.println("error: failed to process request records");
        }

        analyzer.cleanup();
    }

    private AtomicLong snGenerator;
    private Map<String, UserSession> ip2UserSessions;
    private TreeSet<UserSession> sortedUserSessions;

    private String ttlFilePath; // inactivity_period.txt
    private String inputFilePath;
    private String outputFilePath;

    private BufferedWriter writer;
    private BufferedReader reader;

    private ComparatorByListOrder comparatorByListOrder;

    public DataAnalyzer(String ttlFilePath, String inputFilePath, String outputFilePath) {
        this.ttlFilePath = ttlFilePath;
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;

        this.ip2UserSessions = new HashMap<String, UserSession>();
        this.sortedUserSessions = new TreeSet<UserSession>();

        this.comparatorByListOrder = new ComparatorByListOrder();
    }

	public boolean init() {
        this.snGenerator = new AtomicLong(1L);

        long ttlInSeconds = readInactivityPeriod();
		if (ttlInSeconds < 0L) {
			return false;
		}

        DateTimeUtil.ttlInSeconds = ttlInSeconds;

		File inputFile = new File(inputFilePath);
		if (inputFile == null || !inputFile.exists()) {
			return false;
		}

        boolean bRet = false;
        try {
            reader = new BufferedReader(new FileReader(inputFile));
            writer = new BufferedWriter(new FileWriter(outputFilePath));

            return true;
        } catch (Exception e) {
            System.out.println("error: failed to open input or output file. " + e.getMessage());
        }

        return bRet;
	}

    public void cleanup() {
        ip2UserSessions.clear();
        sortedUserSessions.clear();

        try {
            if (writer != null) {
                writer.close();
            }

            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) {
            System.out.println("error: while closing files, " + e.getMessage());
        }
    }

    private long getNextSerialNumber() {
        return this.snGenerator.getAndIncrement();
    }

	private long readInactivityPeriod() {
		long inactivityPeriod = -1L;
		try {
			final BufferedReader reader = new BufferedReader(new FileReader(new File(ttlFilePath)));
	    	String strLine = reader.readLine();
	    	if (strLine != null) {
	    		strLine = strLine.trim();
	    		if (!strLine.isEmpty()) {
	    			inactivityPeriod = Long.parseLong(strLine);
	    		}
	    	}

	        reader.close();
		} catch(Exception e) {
			System.out.println("error: while reading file [" + ttlFilePath + "], " + e.getMessage());
		}

		return inactivityPeriod;
	}

    public void processRequestRecords() throws IOException {
        if (this.reader == null) {
            return;
        }

        long prevReqTime = -1L;
        long requestCount = 0;
        long expUserSessionCount = 0;

        String strLine;
        while ((strLine = this.reader.readLine()) != null) {
            if (strLine.isEmpty()) {
                continue;
            }

            final RequestLog reqLog = this.parseRecordLineIntoRequestLog(strLine);
            if (reqLog == null || reqLog.getDateTime() == null) {
                continue;
            }

            requestCount++;

            final Date reqDateTime = reqLog.getDateTime();

            // determine if it's time to check if any UserSessions become expired.
            if (reqDateTime.getTime() > prevReqTime) {
                prevReqTime = reqDateTime.getTime();

                // it's time to output and clean up those expired user sessions.
                int nExpiredUserSessions = this.outputAndCleanupExpiredUserSessions(reqDateTime);
                expUserSessionCount += nExpiredUserSessions;
            }

            // create a new user session or update existing active UserSession having current ip.
            String currentIP = reqLog.getIP();
            UserSession userSession = ip2UserSessions.get(currentIP);
            if (userSession == null) {
                userSession = new UserSession(this.getNextSerialNumber());
                userSession.setIP(currentIP);
                userSession.setStartDateTime(reqDateTime);
                userSession.setEndDateTime(reqDateTime);
                userSession.increaseDocCountByOne();

                this.ip2UserSessions.put(currentIP, userSession);
                this.sortedUserSessions.add(userSession);
            } else {
                // it's found, so need remove it from sorted set at first
                this.sortedUserSessions.remove(userSession);

                // then update it and further put it back into sorted set
                userSession.setEndDateTime(reqDateTime);
                userSession.increaseDocCountByOne();

                this.sortedUserSessions.add(userSession);
            }

            if (requestCount % 50000 == 0) {
                System.out.println("== count of request records processed: " + requestCount + "; count of expired user sessions: " + expUserSessionCount);
            }
        }

        // write out all the active user sessions still left in sorted set due to reaching the end of input file
        System.out.println("== when the end of input file is reached, " + this.sortedUserSessions.size() + " active user sessions left");

        expUserSessionCount += this.outputActiveUserSessionsLeftInSortedSetWhenEndOfInputFileIsReached();

        System.out.println("== Total # of request records processed: " + requestCount + "; Total # of user sessions written: " + expUserSessionCount);
    }

    /**
     * According to example in README.md at https://github.com/InsightDataScience/edgar-analytics,
     * those non-expired user sessions should be written into output file in the following order
     * when the end of input file is reached: (1) the user sessions with earlier request date and
     * time should be written out prior to others; (2) if multiple user sessions have the same
     * request date and time, they should be written out in the order where they were read from
     * input file.
     *
     * @return
     */
    private int outputActiveUserSessionsLeftInSortedSetWhenEndOfInputFileIsReached() {
        if (this.writer == null) {
            return 0;
        }

        if (this.sortedUserSessions.isEmpty()) {
            return 0;
        }

        List<UserSession> userSessionList = new LinkedList<UserSession>();
        userSessionList.addAll(this.sortedUserSessions);
        if (userSessionList.size() > 1) {
            // let's sort them in ascending order of startDateTime and serialNumber
            Collections.sort(userSessionList, this.comparatorByListOrder);
        }

        int nUserSessionsWritten = 0;
        for (UserSession session : userSessionList) {
            try {
                this.writer.write(session.toResultRecordString() + "\n");

                nUserSessionsWritten++;
            } catch (IOException e) {
                System.out.println("error: failed to write user session (" + session.toResultRecordString() + "); " + e.getMessage());
            }
        }

        return nUserSessionsWritten;
    }

    private int outputAndCleanupExpiredUserSessions(final Date currentDateTime) {
        if (currentDateTime == null) {
            return 0;
        }

        int nExpUserSessions = 0;
        while (!(this.sortedUserSessions.isEmpty())) {
            final UserSession userSession = this.sortedUserSessions.first();
            if (userSession.isExpiredInTermOfCurrentTime(currentDateTime)) {

                nExpUserSessions++;

                // remove it from in-memory storages
                this.sortedUserSessions.pollFirst();
                this.ip2UserSessions.remove(userSession.getIP());

                // write it into output file
                if (this.writer != null) {
                    try {
                        this.writer.write(userSession.toResultRecordString() + "\n");
                    } catch (IOException e) {
                        System.out.println("error: failed to write user session (" + userSession.toResultRecordString() + "); " + e.getMessage());
                    }
                }
            } else {
                // break from loop because no more expired user sessions so far.
                break;
            }
        }

        return nExpUserSessions;
    }

    public RequestLog parseRecordLineIntoRequestLog(String strLine) {
        if (strLine == null || strLine.isEmpty()) {
            return null;
        }

        // field names in input record line:
        // ip,date,time,zone,cik,accession,extention,code,size,idx,norefer,noagent,find,crawler,browser
        // example:
        // 38.140.198.dgd,2017-06-30,23:46:47,0.0,917851.0,0001104659-17-042597,a17-15925_26k.htm,200.0,23202.0,0.0,0.0,0.0,10.0,0.0,

        strLine = strLine.trim();
        String[] tokens = strLine.split(",");
        if (tokens == null || tokens.length < 3) {
            return null;
        }

        String ip = tokens[0].trim();
        String strDate = tokens[1].trim();
        String strTime = tokens[2].trim();

        Date dateTime = DateTimeUtil.parseDateTime(strDate, strTime);
        RequestLog reqLog = new RequestLog(ip, dateTime);

        return reqLog;
    }
}
