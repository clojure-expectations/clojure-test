#!/bin/sh
for v in 9 10
do
	clojure -X:1.${v}:test
	if test $? -ne 0
	then
		exit 1
	fi
	clojure -X:1.${v}:test:humane :excludes '[:negative]'
	if test $? -ne 0
	then
		exit 1
	fi
done
if test "$1" != "clj-only"
then
	clojure -M:cljs-test -e :negative
	if test $? -ne 0
	then
		exit 1
	fi
fi
