#!/bin/bash

USAGE=$(mktemp)
DEFINED=$(mktemp)

grep 'R.string' src/nl/atcomputing/examtrainer/*.java | sed 's/.*R\.string\.\([A-Za-z_][A-Za-z_]*\).*/\1/' > $USAGE
grep -R '\@string/' res/ | sed 's/.*\@string\/\([A-Za-z_][A-Za-z_]*\).*/\1/' >> $USAGE


if [ "$1" = "-d" ] && [ -n "$2" ] 
then
	grep '<string name=' ${2} | sed 's/.*<string name=\"\([A-Za-z_][A-Za-z_]*\).*/\1/' | sort -u > $DEFINED
	NOTUSED=$(sort -u $USAGE | diff $DEFINED - | grep '^<' | sed 's/^< //')

	echo $NOTUSED | tr ' ' '\n'
	echo
	echo Deleting above string identifiers from ${2}.
	echo "Are you sure? (yes or no)"
	read answer
	if [ "$answer" = "yes" ]
	then
		for i in $(echo $NOTUSED)
		do
			sed -i "/.*<string name=\"${i}\".*/d" ${2}
		done
	fi
fi

rm $USAGE $DEFINED
