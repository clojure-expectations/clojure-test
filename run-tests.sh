#!/bin/sh
for v in 8 9 10
do
	clojure -M:1.${v}:test:runner
	if test $? -ne 0
	then
		exit 1
	fi
	clojure -M:1.${v}:test:humane:runner -e :negative
	if test $? -ne 0
	then
		exit 1
	fi
done
if test "$1" != "clj-only"
then
	clojure -M:test:cljs-runner -e :negative
	if test $? -ne 0
	then
		exit 1
	fi
fi
