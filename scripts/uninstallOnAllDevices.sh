#!/bin/bash

DEVICES=$(adb devices | grep 'device$' | cut -f1)

for device in ${DEVICES}
do
	echo "Uninstalling on ${device}"
	adb -s ${device} uninstall nl.atcomputing.examtrainer
done
