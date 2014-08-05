#!/bin/bash

function usage 
{
cat << "EOF" >&2
Usage: installOnAllDevices.sh [-r]

Options:
  -r: reinstall package (default: install package)
  -h: show this help

EOF
}

INSTALL_OPTIONS=""

while getopts ":rh" opt; do
  case $opt in
    r)
      INSTALL_OPTIONS="${OPTIONS} -r"      
      ;;
    h)
      usage
      exit 0
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      usage
      exit 1
      ;;
  esac
done

DEVICES=$(adb devices | grep 'device$' | cut -f1)

for device in ${DEVICES}
do
	echo "Installing on ${device}"
	adb -s ${device} install ${INSTALL_OPTIONS} ../bin/StartScreenActivity.apk
	echo "Starting on ${device}"
	adb -s ${device} shell am start -n nl.atcomputing.examtrainer/nl.atcomputing.examtrainer.activities.StartScreenActivity
done
