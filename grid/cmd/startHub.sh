#!/bin/bash -x

. $testHome/cmd/setConfig.sh
logName=`getLogPrefix`
$testHome/cmd/killHub.sh


java -jar $RmJar -role hub &> $logName.log &
exit 0;
