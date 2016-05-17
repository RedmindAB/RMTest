#!/bin/bash

ps ax | grep java | grep "\-role\ node" | sed 's/^[ \t]*//' | cut -d " " -f1 | while read thePid
	do
	echo "killing $thePid"
	kill $thePid
done
