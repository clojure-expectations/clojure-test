#!/bin/sh
for v in 1.9 1.10 1.11 master
do
	clojure -X:${v}:test
	if test $? -ne 0
	then
		exit 1
	fi
	clojure -X:${v}:test:humane :excludes '[:negative]'
	if test $? -ne 0
	then
		exit 1
	fi
done
if test "$1" != "clj-only"
then
	clojure -M:cljs -e :negative
	if test $? -ne 0
	then
		exit 1
	fi
fi
