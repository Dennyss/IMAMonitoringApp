#!/bin/bash
JAVA_HOME=/tools/jdk1.7.0_40

$JAVA_HOME/bin/java -Dlog4j.configurationFile=/share/vsb/IMAMonitoring/log4j2.xml -cp imaMonitoring.jar:libs/* com.Application &
