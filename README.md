# `clojure.test` for Expectations

A `clojure.test`-compatible version of the [classic Expectations testing library](https://clojure-expectations.github.io/).

## Where?

[![Clojars Project](https://clojars.org/expectations/clojure-test/latest-version.svg)](https://clojars.org/expectations/clojure-test)

Try it out:

```
clj -Sdeps '{:deps {expectations/clojure-test {:mvn/version "RELEASE"}}}'
```

## What?

This library brings `expect`, `more`, `more-of`, etc from Expectations into the
`clojure.test` world to be used instead of (or in addition to) the familiar `is`
macro. This library has no dependencies, other than `clojure.test` itself, and
should be compatible with all existing `clojure.test`-based tooling in editors
and command-line tools.

You can either use `deftest` from `clojure.test`, or `defexpect` from
this library to wrap your tests.

```clojure
(ns my.cool.project-test
  (:require [clojure.test :refer [deftest is]]
            [expectations.clojure.test :refer :all]))

;; mix'n'match libraries:

(deftest mixed
  (is 2 (+ 1 1))
  (expect even? (+ 1 1)))

;; simple equality tests:

(defexpect equality
  (expect 1 (* 1 1))
  (expect "foo" (str "f" "oo")))

;; the expected outcome can be a regular expression:

(defexpect regex-1
  (expect #"foo" "It's foobar!"))

;; since that has only a single expectation, it can be written more succinctly:

(defexpect regex-2 #"foo" "It's foobar!")

;; the expected outcome can be an exception type:

(defexpect divide-by-zero ArithmeticException (/ 12 0))

;; the expected outcome can be a predicate:

(defexpect no-elements empty? (list))

;; the expected outcome can be a type:

(defexpect named String (name :foo))

;; the expected outcome can be a Spec:

;; assumes (require '[clojure.spec.alpha :as s])
(s/def ::value (s/and pos-int? #(< % 100)))
(defexpect small-value
  (expect ::value (* 13 4)))

;; if the actual value is a collection, the expected outcome can be an element or subset "in" that collection:

(defexpect collections
  (expect {:foo 1} (in {:foo 1 :cat 4}))
  (expect :foo (in #{:foo :bar}))
  (expect :foo (in [:bar :foo])))

;; just like clojure.test's testing macro to label groups of tests
;; you can use expecting to label groups of expectations (this uses
;; some of more advanced features listed below):

(defexpect grouped-behavior
  (expecting "numeric behavior"
    (expect (more-of {:keys [a b]}
                     even? a
                     odd?  b)
            {:a (* 2 13) :b (* 3 13)})
    (expect pos? (* -3 -5)))
  (expecting "string behavior"
    (expect (more #"foo" "foobar" #(clojure.string/starts-with? % "f"))
            (str "f" "oobar"))
    (expect #"foo"
            (from-each [s ["l" "d" "bar"]]
              (str "foo" s)))))
```

Just like `deftest`, the `defexpect` macro creates a function that contains the test\(s\). You can run each function individually:

```clojure
user=> (equality)
nil
```

If the test passes, nothing is printed, and `nil` is returned. Let's look at a failing test:

```clojure
user=> (defexpect inequality (* 2 21) (+ 13 13 13))
#'user/inequality
user=> (inequality)

FAIL in (inequality) (.../README.md:67)
expected: (=? (* 2 21) (+ 13 13 13))
  actual: 39
nil
```

The output is produced by `clojure.test`'s standard reporting functionality.
The `=?` operator is an extension to `clojure.test`'s `assert-expr` multimethod
that allows for Expectations style of predicate-or-equality testing (based on
whether the "expected" expression resolves to a function or some other value):

```clojure
user=> (defexpect indivisible odd? (+ 1 1))
#'user/indivisible
user=> (indivisible)

FAIL in (indivisible) (.../README.md:83)
expected: (=? odd? (+ 1 1))
  actual: (not (odd? 2))
nil
```

Here we see the predicate (`odd?`) being applied in the "actual" result from
`clojure.test`.

`expectations.clojure.test` supports the following features from Expectations so far:
* simple equality test
* simple predicate test
* spec test (using a keyword that identifies a spec)
* class test -- see `named` above
* exception test -- see `divide-by-zero` above
* regex test -- see `regex-1` and `regex-2` above
* `(expect expected-expr (from-each [a values] (actual-expr a)))`
* `(expect expected-expr (in actual-expr))` -- see `collections` above
* `(expect (more-of destructuring e1 a1 e2 a2 ...) actual-expr)`
* `(expect (more-> e1 a1 e2 a2 ...) actual-expr)` -- where `actual-expr` is threaded into each `a1`, `a2`, ... expression
* `(expect (more e1 e2 ...) actual-expr)`
* `(expect expected-expr (side-effects [fn1 fn2 ...] actual-expr))`

Read [the Expectations documentation](https://clojure-expectations.github.io/)
for more details of these features.

Tests defined with `defexpect` behave just like tests defined with `deftest` and
all of the existing `clojure.test`-based tooling will work with them, including
fixtures, test runners, and other libraries that patch `clojure.test` to improve
its error reporting and other features.

## Why?

Given the streamlined simplicity of Expectations, you might wonder why you
would want to migrate your Expectations test suite to `clojure.test`-style
named tests? The short answer is **tooling**! Whilst Expectations has
well-maintained, stable plugins for Leiningen and Boot, as well as an Emacs mode,
the reality is that Clojure tooling is constantly evolving and most of those
tools -- such as the excellent [CIDER](https://cider.readthedocs.io/en/latest/),
[Cursive](https://cursive-ide.com/),
and the more recent [ProtoREPL](https://atom.io/packages/proto-repl)
and [Chlorine](https://atom.io/packages/chlorine) (both for Atom) --
are going to focus on Clojure's built-in testing library first.
Support for the original form of Expectations, using unnamed tests, is
non-existent in Cursive, and can be problematic in other editors.

A whole ecosystem
of tooling has grown up around `clojure.test` and to take advantage of
that with Expectations, we either need to develop compatible extensions to each
and every tool or we need Expectations to be compatible with `clojure.test`.

One of the big obstacles for that compatibility is that, by default, Expectations
generates "random" function names for test code (the function names are based on the
hashcode of the text form of the `expect` body), which means the test
name changes whenever the text of the test changes. To address that, the new
`expectations.clojure.test` namespace introduces named expectations via
the `defexpect` macro (mimicking `clojure.test`'s `deftest`
macro). Whilst this goes against the [Test Names
philosophy](https://clojure-expectations.github.io/odds-ends.html) that Expectations was created with, it buys us a lot in terms of
tooling support!

## Differences from Expectations

Aside from the obvious difference of providing names for tests -- essential for
compatibility with `clojure.test`-based tooling -- here are the other differences
to be aware of:

* You use standard `clojure.test`-based tooling -- `lein test`, `boot test`, and [Cognitect's `test-runner`](https://github.com/cognitect-labs/test-runner) -- instead of the Expectations-specific tooling.
* Because of that, tests run when you decide, not at JVM shutdown (which is the default with Expectations).
* If you have [Paul Stadig's Humane Test Output](https://github.com/pjstadig/humane-test-output) on your classpath, it will be activated and failures reported by `=?` will be compatible with it, providing better reporting.
* Instead of the `in-context`, `before-run`, `after-run` machinery of Expectations, you can just use `clojure.test`'s fixtures machinery (`use-fixtures`).
* Instead of Expectations' concept of "focused" test, you can use metadata on tests and tell your test runner to "select" tests as needed (e.g., Leiningen's "test selectors", Boot's "filters").
* `freeze-time` and `redef-state` are not (yet) implemented.
* The undocumented `CustomPred` protocol is not implemented -- you can use plain `is` and extend `clojure.test`'s `assert-expr` multimethod if you need that level of control.

## Test & Development

To test, run `clj -A:test:runner`.

## TODO

Add tests(!) and more examples.

## License & Copyright

Copyright © 2018-2019 Sean Corfield, all rights reserved.

Distributed under the Eclipse Public License version 1.0.
