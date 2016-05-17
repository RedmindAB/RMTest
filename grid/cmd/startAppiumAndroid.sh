#!/bin/bash -x

. $testHome/cmd/setConfig.sh
logName=`getLogPrefix`
#./killAppiums.sh
export APK_PATH=$1
if [ -z "$APK_PATH" ]
then
	echo "No APK supplied as argument, starting Chrome"
else 
	export APK_PACKAGE=`$AAPTCmd dump badging $APK_PATH | grep package: | cut -d " " -f 2 | cut -d "'" -f 2`
fi

isTablet() {
	tabletLimit=6.0
	currDevId=$1
screenDensity=`$ANDROID_HOME/platform-tools/adb -s $currDevId shell getprop ro.sf.lcd_density | tr -d "\r"`

        screenWidth=`$ANDROID_HOME/platform-tools/adb -s $currDevId shell dumpsys window | grep mUnrestrictedScreen | cut -d ")" -f2 | cut -d "x" -f1 | xargs echo`
        screenHight=`$ANDROID_HOME/platform-tools/adb -s $currDevId shell dumpsys window | grep mUnrestrictedScreen | cut -d ")" -f2 | cut -d "x" -f2 | tr -d "\r" | xargs echo`
        screenSizeW=`echo "scale=2;$screenWidth/$screenDensity" | bc`
        screenSizeW2=`echo "scale=2;$screenSizeW*$screenSizeW" | bc`
        screenSizeH=`echo "scale=2;$screenHight/$screenDensity" | bc`
        screenSizeH2=`echo "scale=2;$screenSizeH*$screenSizeH" | bc`
        screenSize=`echo "scale=2;sqrt($screenSizeW2+$screenSizeH2)" | bc`
	isTabletBool=`echo "scale=0;$screenSize>$tabletLimit" | bc`
	echo $isTabletBool
}

export jar_home="$testHome/lib/selenium/"
export androidNodeFile="/tmp/androidNodes.cfg"

export basePort=8080
export bootstrapPort=10080
export chromeDriverPort=11080
export selendroidPort=12080
export modelName=""
export androidVersion=""
export isInstalled=""
rm -f $androidNodeFile


$ANDROID_HOME/platform-tools/adb devices | grep "	device" | cut -d "	" -f1 | while read currDevId
do
	modelName=`$ANDROID_HOME/platform-tools/adb -s $currDevId shell getprop ro.product.model | tr -d "\r"`	
	modelBrand=`$ANDROID_HOME/platform-tools/adb -s $currDevId shell getprop ro.product.brand | tr -d "\r"`	
	androidVersion=`$ANDROID_HOME/platform-tools/adb -s $currDevId shell getprop ro.build.version.release | tr -d "\r"`
	$ANDROID_HOME/platform-tools/adb -s $currDevId uninstall io.appium.unlock
	description="$modelBrand $modelName  $androidVersion"	
	echo "####### $modelName ########"
	basePort=$[$basePort+1]
	bootstrapPort=$[$bootstrapPort+1]
	chromeDriverPort=$[$chromeDriverPort+1]
	selendroidPort=$[$selendroidPort+1]
	cp -f $testHome/etc/Appium_TEMPLATE.json	$testHome/etc/Appium_TEMP.json
	
	sed -i '' "s/PLATFORM/Android/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/OS_NAME/ANDROID/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/DEVICE_ID/$currDevId/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/DESCR_STRING/$description/g" $testHome/etc/Appium_TEMP.json
	
	     if [ `isTablet $currDevId` -eq 1 ] || [ "$modelName" =  "SM-T550" ]  || [ "$modelName" =  "SM-T530" ]
                then
			sed -i '' "s/DEVICE_TYPE/tablet/g" $testHome/etc/Appium_TEMP.json
                	echo "im a tablet, oh yes i am!"
					
		else
			sed -i '' "s/DEVICE_TYPE/mobile/g" $testHome/etc/Appium_TEMP.json
			echo "im a mobileomophone, oh yes i am!"
        	fi
	
	 
	if [ -z $APK_PATH ]
		then
		sed -i '' "/APP_PATH/d" $testHome/etc/Appium_TEMP.json
		sed -i '' '/APP_PKG/d' $testHome/etc/Appium_TEMP.json	
		sed -i '' '/START_ACTIVITY/d' $testHome/etc/Appium_TEMP.json
		sed -i '' "s:BROWSER_NAME:Chrome:g" $testHome/etc/Appium_TEMP.json
	else
		if [ -z $2 ]
		then	
			echo "Usage: command.sh <apk path> <start activity>"
			exit 1
		else
			sed -i '' "s:APP_PKG:$APK_PACKAGE:g" $testHome/etc/Appium_TEMP.json
			sed -i '' "s:APP_PATH:$APK_PATH:g" $testHome/etc/Appium_TEMP.json
			sed -i '' "s:START_ACTIVITY:$2:g" $testHome/etc/Appium_TEMP.json
			sed -i '' "s:WAIT_ACTIVITY:$3:g" $testHome/etc/Appium_TEMP.json
			sed -i '' '/BROWSER_NAME/d' $testHome/etc/Appium_TEMP.json
		fi
	fi
	sed -i '' "s/DEVICE_NAME/$modelName/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/DEVICE_VERSION/$androidVersion/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/MAX_SESSIONS/1/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/APPIUM_PORT/$basePort/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/APPIUM_HOST/$RMTestLocalNodeIp/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/HUB_PORT/4444/g" $testHome/etc/Appium_TEMP.json
	sed -i '' "s/HUB_HOST/$RMTestHubIp/g" $testHome/etc/Appium_TEMP.json
	cat $testHome/etc/Appium_TEMP.json
	$appiumBinary -U $currDevId -a $RMTestLocalNodeIp -p $basePort -bp $bootstrapPort --chromedriver-port $chromeDriverPort --selendroid-port $selendroidPort --nodeconfig $testHome/etc/Appium_TEMP.json  --session-override &> $logName.$currDevId.log &
	loopcount=0
	keepTrying=true
	while $keepTrying
		do
		connectedCount=`grep -c "Appium successfully registered with the grid on $RMTestHubIp:4444" "$logName.$currDevId.log"`
		if [ $connectedCount -gt 0  ]
		then
			echo "Connected to HUB"
			keepTrying=false
		else
			echo "Not yet connected to HUB"

                	sleep 1
		fi
		loopcount=$[loopcount+1]
		echo loopcount
		if [ $loopcount -gt 58 ]
		then
			keepTrying=false
			echo "Failed to connect to HUB"
		fi
		
	done
done

