#!/bin/bash -x

. $testHome/cmd/setConfig.sh

export jar_home="$testHome/lib/selenium/"

export basePort=8180
export modelName=""
export androidVersion=""
export isInstalled=""
rm -f $androidNodeFile
export idevicePath="$testHome/libimobiledevice-macosx"


$idevicePath/idevice_id -l | while read currDevId
do
	modelName=`$idevicePath/ideviceinfo -u $currDevId | grep DeviceName | sed "s/DeviceName: //g"`
	iosVersion=`$idevicePath/ideviceinfo -u $currDevId | grep ProductVersion | sed "s/ProductVersion: //g"`
	description="$modelName  $iosVersion"	
	echo "####### $modelName ########"
	basePort=$[$basePort+1]
	cp -f $testHome/etc/Appium_TEMPLATE_IOS.json	$testHome/etc/Appium_TEMP.json
	
	sed -i '' "s/PLATFORM/iOS/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/OS_NAME/IOS/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/DEVICE_ID/$currDevId/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/DESCR_STRING/$description/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s:APP_PATH:$IPA_PATH:g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/DEVICE_NAME/iphone/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/DEVICE_VERSION/$iosVersion/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/MAX_SESSIONS/1/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/APPIUM_PORT/$basePort/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/APPIUM_HOST/$RMTestLocalNodeIp/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/HUB_PORT/4444/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/HUB_HOST/$RMTestHubIp/g" $testHome/etc/Appium_TEMP.json
    sed -i '' "s/BROWSER_NAME/Safari/g" $testHome/etc/Appium_TEMP.json

	cat $testHome/etc/Appium_TEMP.json	
	
#	$appiumBinary -U $currDevId -a $RMTestLocalNodeIp -p $basePort --nodeconfig ../etc/Appium_TEMP.json &> $testHome/log/appium_$currDevId.log & 
#	sleep 5
	logfile="$testHome/log/appium_ios_$currDevId.log"
        $appiumBinary -U $currDevId -a $RMTestLocalNodeIp -p $basePort --nodeconfig $testHome/etc/Appium_TEMP.json --app /Users/testrunner/GIT/SafariLauncher.ipa --session-override &> $logfile &
        ios_webkit_debug_proxy -c $currDevId:27753 -d &
        appiumStarted=true
        while $appiumStarted
                do
                connectedCount=`grep -c "Appium successfully registered with the grid on $RMTestHubIp:4444" $logfile`
                if [ $connectedCount -gt 0  ]
                then
                        echo "Connected to HUB"
                        appiumStarted=false
                fi
                echo "Not yet connected to HUB"
                sleep 1
        done
done

