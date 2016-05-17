#!/bin/bash

green='\033[0;32m'
NC='\033[0m'

echo "##### Starting checks #####"

########## CHECKING MAVEN #########
mvnRes=""
echo "Checking Maven"
mvn=`which mvn`
if [[ -x $mvn ]]
then
	mvnRes="OK!"
else
	mvnRes="No maven seems to be installed"
fi

########## CHECKING JAVA ##########

javaRes="";

echo "Checking java"

if type -p java; then
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    _java="$JAVA_HOME/bin/java"
else
    javaRes="No java version seems to be installed"
fi

if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    if [[ "$version" > "1.8" ]]; then
	javaRes="OK!"
    else
	javaRes="The java version is to low, you have $version installed and it needs to be 1.8 or higher"
    fi
fi

########## CHECKING TESTHOME ########
echo "Checking \$testHome"
testHomeRes="";
if [[ ! -z $testHome ]]
then
	testHomeRes="OK!"
else
	testHomeRes="the testHome system variable is not set"
fi

######## Checking node_modules ######
echo "Checking node_modules"
node_modules_res="";
node_modules=./node_modules
if [ -d $node_modules ]
then
	node_modules_res="OK!"
else
	node_modules_res="it seems like the required node_modules for RMTest is not installed at all, or not where it should be.
		node_modules must be in the folder: $testHome"
fi

######### is node modules present #######
echo "Checking chromedriver"
CHROMEDRIVER_RES="";
chromedriver=./node_modules/chromedriver/bin/chromedriver
if [ -f $chromedriver ]
then
	CHROMEDRIVER_RES="OK!"
else
	CHROMEDRIVER_RES="Cant find chromedriver installation, should be in \$testHome/node_modules/chromedriver/bin/chromedriver"
fi

######## is phantomjs installed ###########
echo "Checking phantomjs"
phantomjs_res="";
phantomjs=./node_modules/phantomjs/bin/phantomjs
if [ -f $phantomjs ]
then
	phantomjs_res="OK!"
else
	phantomjs_res="Cant find phantomjs installation, should be in \$testHome/node_modules/phantomjs/bin/phantomjs"
fi

######## is phantomjs installed ###########
echo "Checking Appium"
appium_res="";
appium=./node_modules/appium/bin
if [ -d $appium ]
then
	appium_res="OK!"
else
	appium_res="Cant find appium installation, should be in \$testHome/node_modules/appium"
fi


####### Print Results ########
echo "##### Results #####"
echo "Maven:		$mvnRes"
echo "Java:		$javaRes"
echo "\$testHome	$testHomeRes"
echo "node_modules	$node_modules_res"
echo "chromedriver	$CHROMEDRIVER_RES"
echo "phantomjs	$phantomjs_res"
echo "appium		$appium_res"
