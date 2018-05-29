
# Code challenge for Analysis of SEC EDGAR Web Logs

![#f03c15](https://placehold.it/15/f03c15/000000?text=+) NOTE: `due to storage limit on a github account, didn't check in input files for my load-testing on this project. But load or scalability testing was done locally on input file `log20170630.csv` (2.73GB, downloaded from SEC EDGAR website) by configuring inacitivity_period from 1 second to 24 hours.`![#f03c15](https://placehold.it/15/f03c15/000000?text=+) 

# Table of Contents
1. [References](README.md#references)
2. [My repo dx-edgar-analytics](README.md#my-repo-dx-edgar-analytics)
3. [Dependencies: Java, Maven and TestNG](README.md#dependencies-java-maven-and-testng)
4. [How to compile and run this project](README.md#how-to-compile-and-run-this-project)
5. [My solution to this code challenge](README.md#my-solution-to-this-code-challenge)

# References

* Insight's Edgar-Analytics Repo: https://github.com/InsightDataScience/edgar-analytics 
* EDGAR Log File Data Set: https://www.sec.gov/dera/data/edgar-log-file-data-set.html

# My repo dx-edgar-analytics

https://github.com/dx4test/dx-edgar-analytics

This repo directory structure is the same as that of Insight's Edgar-Analytics Repo.

# Dependencies: Java, Maven and TestNG
	
This project is implemented in Java, and compiled using Maven.
	
TestNG is a dependent library which is used do unit-testing on some methods of important code logics through test classes, including:

{DateTimeUtilTest.java, ComparatorByListOrderTest.java, UserSessionTest.java, and DataAnalyzerTest.java }.	
  
	<dependency>
	  <groupId>org.testng</groupId>
	  <artifactId>testng</artifactId>
	  <version>6.8.7</version>
	  <scope>test</scope>
	</dependency>
 
# How to compile and run this project

* go to home directory (or the top-most directory) of project "dx-edgar-analytics" which has the same directory structure as Insight's repo.

* directly run script file "run.sh" to compile and run this project;

* or alternatively take the following 2 steps to compile and run this project, respectively:

	> run this command to compile it: 

	`mvn clean install -f ./src/sec-log-processor/pom.xml`

	> use this command to run it:

	`java -cp ./src/sec-log-processor/target/sec-log-processor-0.0.1-SNAPSHOT.jar \
        com.log.sec.DataAnalyzer \
        ./input/inactivity_period.txt \
        ./input/log.csv \
        ./output/sessionization.txt`
        
Here is my local environment to compile and run this project:

    System: MacOS Sierra (v.10.12.6).
    Apache Maven: version 3.2.5 (for compiling java maven project).
    TestNG: v6.8.7 (for java unit-testing).
    Java: version 1.8.0_72 

NOTE: I tried to avoid those new features of Java 1.8, so it might also work with other old versions of Java.
In addition, both Eclipse IDE and bash shell script were able to compile and run this project successfully on MacOS Sierra.

# My solution to this code challenge

## (1) Serial number

This project requires that a collection of expired user sessions be written into output file in the order in which their corresponding request log records were listed in input file if their ending date-time info and/or starting date-time info are the same; and also the same requirement is applied to a collection of active user sessions having the same starting date-time info when the end of input file is reached.

To meet the above requirements, a serial number counter of type long is introduced for user sessions; whenever a new user session need be generated, the serial number counter is increased by one, and its current value is assigned to the user session. The serial number together with request data times helps ensure user sessions are written out in the specified order.

## (2) Utility class "DateTimeUtil"

It helps combine together the date and time info of a request record into a string in format "yyyy-MM-dd HH:mm:ss", and then parse the combined string into an object of type java.util.Date.

It also keeps a global constant "`ttlInSeconds`" (i.e. inactivity period).

NOTE: According to READ.md of the repo "insight edgar-analytics", we are required to consider only those fields in BOLD; the zone field is not in BOLD, so default time zone is used while parsing date and time info into a Date object.

## (3) Model class "UserSession"

It has the following class members:

  - `ip`: the first field info extracted from a request record, it uniquely identifies a user.
  - `serialNumber`:  the serial number assigned for this session when this session is created.
  - `startDateTime`: the Date object parsed from the date and time info of the first request record of this session.
  - `endDateTime`: the Date object parsed from the date and time info of the last request record of this session.
  - `expirationTimeInSeconds`: it is when will this session become expired ( `endDateTime.getTime()/1000 + inactivity_period + 1 second` ).
  - `docCount`: number of documents requested during this session.


The duration of a user session can be calculated through startDateTime and endDateTime as follows:

	(endDateTime.getTime() - startDateTime.getTime())/1000 + 1 second
	
To output a user session into output file, only the following info items are printed:

    {ip, startDateTime,	endDateTime, "duration" (derived from startDateTime and endDateTime), docCount}

## (4) Data structures

### * HashMap `ip2UserSessions`

It maps an ip string to a UserSession object through classic data structure `hashtable`.

It's very fast (time complexity of `amortized O(1)`) to check if an incoming request record should be either merged into some existing user session, or used to generate a new user session.

### * TreeSet `sortedUserSessions`

Java class TreeSet is essentially a `balanced binary search tree` implemented through `Red-Black Tree`. Its add, remove, and contains methods have time complexity of `O(logn)`; moreover, the time complexity is only `O(1)` for peeking and polling the elements from its head or tail.

It's used to maintain a collection of user sessions which are sorted in ascending order of
expirationTimeInSeconds and/or serialNumber. By this data structure, it's super fast to check which user sessions have been expired by starting from its head; also it's still fast enough for adding and removing some user session to/from this data structure when inserting a new user session or updating some existing user session.

NOTE: when some existing user session need be updated with another endDateTime (including expirationTimeInSeconds), we need remove it from this data structure at first, then update the user session, and finally put it back into this data structure.

## (5) Work-flow of main class "DataAnalyzer"

### * Summary

It's the core class of this project for dealing with business logics. Its primary functionalities include:

  (a) read a request record from input file;

  (b) parse the request record, then either merge it into some existing user session or create a new user session with this info, and further update data buffers (ip2UserSessions and sortedUserSessions) with this user session;

  (c) whenever time changes according to incoming request records, check expired user sessions and print out them into output file;

  (d) when the end of input file is reached, print out all the user sessions still left in "sortedUserSessions" into output file.


### * Initialization 


  `reader`: open input data file (log.csv);

  `writer`: open output result file (sessionization.txt);

  `DateTimeUtil.ttlInSeconds`: read TTL value from input file (inactivity_period.txt);

  `snGenerator`: instantiate a serial number counter;

  `ip2UserSessions`: instantiate this data buffer using HashMap<String, UserSession>;

  `sortedUserSessions`: instantiate this data buffer using TreeSet<UserSession>.


### * Process request records one by one (below are pseudo codes)

   prevReqTime = 1L; // a local variable (in milliseconds)

   while (read a new non-null line into strLine) 
   {

	    parse the line into RequestLog (ip: String, dateTime: Date);
	
	    if (dateTime.getTime() > prevReqTime) {

		    // time changes (because the date and time info of current request record are different from those of previous one).
		
		    prevReqTime = dateTime.getTime();
		    check and output expired users sessions from sortedUserSessions;

      }
		
	    if (ip2UserSessions.get(ip) != null) {

		    // merge the request record into existing user session
		
		    userSession = ip2UserSessions.get(ip);
		    remove it from sortedUserSessions;
		    update userSession (endDateTime, expirationTimeInSeconds, docCount);
		    add it into sortedUserSessions.

	    } else {

		    // create a new user session with this request record

		    userSession = new UserSession(getNextSerialNumber());
        userSession.setIP(ip);
        userSession.setStartDateTime(dateTime);
        userSession.setEndDateTime(dateTime); // internally 'expirationTimeInSeconds' is also updated.
        userSession.increaseDocCountByOne();

        ip2UserSessions.put(ip, userSession);
        sortedUserSessions.add(userSession);

	    }

   }

   // Since the end of input file is reached, write into output file all the left user sessions in ip2UserSessions regardless of expiration.

   // NOTE: since it's required that these user sessions be written out in the order in which they were listed in input file,

   // these user sessions have to be sorted in the ascending order of startDateTime and/or serial number.

   if (ip2UserSessions is not empty) {

      if 2 or more items left, sort the left user sessions in ascending order of startDateTime and/or serial number;

      write them into output file one by one.
   }


### * clean up

close and clean up resources.
