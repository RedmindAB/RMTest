#!/bin/sh

if [ -z "$1" ]
  then
    echo "usage: ./setVersion.sh [VERSION]"
    exit 0
fi

cd $(dirname $0)
mvn versions:set -DnewVersion=$1
mvn versions:commit
