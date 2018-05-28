#!/bin/bash
#
# Use this shell script to compile (if necessary) your code and then execute it. Below is an example of what might be found in this file if your program was written in Python
#
#python ./src/sessionization.py ./input/log.csv ./input/inactivity_period.txt ./output/sessionization.txt

echo -e "\n=== compiling this java proejct using maven ......\n"

## compile java project
mvn clean install -f ./src/sec-log-processor/pom.xml

## print multiple empty lines
echo -e "\n\n=== starting application 'sec-log-processor' ...... \n\n"

## run the application
java -cp ./src/sec-log-processor/target/sec-log-processor-0.0.1-SNAPSHOT.jar \
	com.log.sec.DataAnalyzer \
	./input/inactivity_period.txt \
	./input/log.csv \
	./output/sessionization.txt

echo -e "\n\n"
