#!/bin/bash 

scriptDir="$( cd "$( dirname "$0" )" && pwd )"
newTestHome=$scriptDir
coreHome=$scriptDir/../java
cd $newTestHome

foundOldConfig=false
if [ -f $HOME/.RmTest ]; then
	oldRmHome=`grep TESTHOME ~/.RmTest | cut -d "=" -f2`
	if [ -f $oldRmHome/etc/LocalConfig.json ]; then
		foundOldConfig=true		
	fi    
fi

echo "TESTHOME=$newTestHome" > $HOME/.RmTest
echo ".RmTest configured"

if [[ ! -f $newTestHome/etc/LocalConfig.json ]]
then
	if [ $foundOldConfig ] && [ -f $oldRmHome/etc/LocalConfig.json ]; then
    		echo "Found old installation, copying config"
		cp $oldRmHome/etc/LocalConfig.json $newTestHome/etc/LocalConfig.json
	else
		echo "Could not find old config, creating from template"
		cp $newTestHome/etc/LocalConfigTemplate.json $newTestHome/etc/LocalConfig.json
	fi
fi
echo "Verified LocalConfig.json"

mvnHome=`which mvn`
if [[ -x "$mvnHome" ]]
then
	echo "Maven seems OK"
else
	echo "Maven doesnt exist or is not executable"
	exit 1
fi

echo "Running maven compile, correct any errors and rerun this script"
cd $coreHome
mvn package -DskipTests=true

if [[ $? -ne 0 ]]
then
	echo "Found maven Error, please fix and rerun installation"
	exit 1
else
	echo "mvn build completed sucessfully"
	cd $scriptDir
fi

npmHome=`which npm`
if [[ -x "$npmHome" ]]
then
        echo "NPM seems OK"
else
        echo "NPM doesnt exist or is not executable"
        exit 1
fi

npm install appium

#appiumHome=`which appium`
if [[ $? -eq 0 ]]
then
        echo "Appium seems OK"
else
        echo "Appium installation seems to have failed, please run [npm install appium] from test home root folder"
        exit 1
fi

npm install phantomjs
if [[ $? -eq 0 ]]
then
		echo "PhantomJS seems OK"
else
		echo "PhantomJS installation seems to have failed, please run [npm install phantomjs] from test home root folder"
		exit 1
fi

npm install chromedriver
if [[ $? -eq 0 ]]
	then
		echo "Chromedriver seems OK"
	else
		echo "Chromedriver installation seems to have failed, please install with [npm install chromedriver --verbose] in test home root folder"
		exit 1
fi

echo ""
echo ""
echo "### Installation finished successfully ###"
echo ""
echo ""

echo $(tput setaf 1) "Proceed with the following manual configurations"

cat $scriptDir/Installation.txt

echo $(tput sgr0)


