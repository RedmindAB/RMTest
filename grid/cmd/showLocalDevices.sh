#!/bin/bash 

. $testHome/cmd/setConfig.sh

export modelName=""
export androidVersion=""

echo "Connected devices:"
$ADB_PATH/adb devices | grep "	device" | cut -d "	" -f1 | while read currDevId
do
	modelName=`$ADB_PATH/adb -s $currDevId shell getprop ro.product.model | tr -d "\r"`
	androidVersion=`$ADB_PATH/adb -s $currDevId shell getprop ro.build.version.release | tr -d "\r"`
	echo "----------------------------------------------------"
	echo "Model: $modelName ---- AndroidVersion: $androidVersion ---- ID: $currDevId"

done









