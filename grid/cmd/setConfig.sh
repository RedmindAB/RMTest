#!/bin/bash

export testHome=`grep TESTHOME ~/.RmTest | cut -d "=" -f2`

getLocalConfigParam() {
	$testHome/cmd/json.sh -l < $testHome/etc/LocalConfig.json | grep "\"configuration\",\"$1\"" | cut -d"]" -f2 | tr -d '\t' | sed 's/"//g'
}
getRmConfigParam() {
	$testHome/cmd/json.sh -l < $testHome/etc/RmConfig.json | grep "\"configuration\",\"$1\"" | cut -d"]" -f2 | tr -d '\t' | sed 's/"//g'
}

export androidHome=`getLocalConfigParam androidHome`
export ANDROIDHOME=$androidHome
if [[ -z "$ANDROID_HOME" ]]
then
	export ANDROID_HOME=$androidHome
fi
export seleniumVersion=`getLocalConfigParam seleniumVersion`
export ADB_PATH="$ANDROIDHOME/platform-tools"
export PATH=$PATH:$ANDROID_HOME/platform-tools
export RmJar=`find $testHome/../java/rmtest-selenium-server/target/rmtest-selenium-server*.jar`
export appiumBinary="$testHome/node_modules/appium/bin/appium.js"
export RMTestHubIp=`getLocalConfigParam hubIp`
export RMTestLocalNodeIp=`getLocalConfigParam localIp`
#export AndroidBuildToolVersion=`getLocalConfigParam AndroidBuildtoolsVersion`
if [[ -e $androidHome ]]
then
	export AAPTCmd=`find $androidHome -name aapt | head -1`
fi
export PRETTY_TIMESTAMP="date +%Y%m%d_%H%M%S"


alias ws="cd $testHome"
alias cmd="cd $testHome/cmd/"
alias adt="cd $ANDROID_HOME"

getLogPrefix() {
	echo "$testHome/log/$0-`$PRETTY_TIMESTAMP`"
}


PATH=$PATH:$testHome/lib
export MAVEN_OPTS="-Xms1024m -Xmx2048m -Xss2048k"
