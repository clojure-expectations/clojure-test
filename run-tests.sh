#!/bin/sh
for v in 8 9 10
do
	clojure -A:1.${v}:test:runner
	if test $? -ne 0
	then
		exit 1
	fi
	clojure -A:1.${v}:test:humane:runner -e :negative
	if test $? -ne 0
	then
		exit 1
	fi
done
if test "$1" != "clj-only"
then
	clojure -A:test:cljs-runner -e :negative
	if test $? -ne 0
	then
		exit 1
	fi
fi
