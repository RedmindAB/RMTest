#!/bin/bash
ps ax | grep "node" | grep "ium/bin/appium.js" | sed 's/^[ \t]*//' | cut -d " " -f1 | while read thePid
	do
	echo "killing $thePid"
	kill $thePid
done
ps ax | grep "node" | grep "appium" | sed 's/^[ \t]*//' | cut -d " " -f1 | while read thePid
	do
	echo "killing $thePid"
	kill $thePid
done
