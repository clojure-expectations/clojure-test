# Getting Started with expectations/clojure-test using ClojureScript

> NOTE: ClojureScript support, via `planck` is coming in 2.0.0 but you can try it out now via the **develop** branch in the repo!

You can use `expectations/clojure-test` to run tests in both Clojure
and ClojureScript.  Many tests will work without changes in both
Clojure and ClojureScript, though of course some will require
changes for the different environments.  This section describes how
to use `expectations/clojure-test` in ClojureScript and the differences
from using it in Clojure -- see the other sections for details of how
to use it in Clojure for a complete picture.


## Installation

In order to run `expectations/clojure-test` with ClojureScript, you
will use `olical/cljs-test-runner` and the Clojure tool `clj`.

Your `deps.edn` should include this information:

```clojure
{:aliases {:cljs-runner
             {:extra-deps {com.github.seancorfield/expectations {:mvn/version "2.0.0-alpha2"},
                           olical/cljs-test-runner {:mvn/version "3.7.0"},
                           pjstadig/humane-test-output {:mvn/version "0.10.0"}},
              :extra-paths ["src" "test" "cljs-test-runner-out/gen"],
              :main-opts ["-m" "cljs-test-runner.main"
                          "--doo-opts" "dooopts.edn"
                          "-x" "planck"]}}}
```

You will need two small `.edn` files in your project:

`dooopts.edn`:
```clojure
{:paths {:planck "planck --compile-opts planckopts.edn"}}
```

`planckopts.edn`:
```clojure
{:warnings {:private-var-access false}}
```

To run the tests, you run:

```
clj -M:cljs-runner
```

These tests will take a good while longer to run than the same tests
in Clojure, so if you don't get any output for a while, that is not
necessarily a bad thing.

### Requirements

The ClojureScript version of `expectations/clojure-test` works (at present)
only with a specific implementation of self-hosted ClojureScript:
[`planck`](https://planck-repl.org).  You will have to install `planck`
yourself in order to use `expectations/clojure-test` with ClojureScript.

You will have to get `planck -h` to work locally.  See
[here](https://planck-repl.org) for instructions on how to install
`planck` on a variety of systems.  Planck `2.24.0` or later is required.

### Humane Test Output

The use of Paul Stadig's
[Humane Test Output](https://github.com/pjstadig/humane-test-output), is
optional for the Clojure version of `expectations/clojure-test` but it is
required for the ClojureScript version of `expectations/clojure-test`.

## The Basics

This example is the ClojureScript version of the quick comparison provided
for the Clojure version of `expectations/clojure-test`, and provides a quick
comparison with `clojure.test` (the tests match those in the [`clojure.test`
documentation](http://clojure.github.io/clojure/clojure.test-api.html)):

```clojure
(require '[expectations.clojure.test :refer [defexpect expect expecting]])

(defexpect simple-test                  ; (deftest simple-test
  (expect 4 (+ 2 2))                    ;   (is (= 4 (+ 2 2)))
  (expect number? 256)                     ;   (is (instance? Long 256))
  (expect (.startsWith "abcde" "ab"))   ;   (is (.startsWith "abcde" "ab"))
  (expect ##Inf (/ 1 0))                ;   (is (thrown? ArithmeticException (/ 1 0)))
  (expecting "Arithmetic"               ;   (testing "Arithmetic"
    (expecting "with positive integers" ;     (testing "with positive integers"
      (expect 5 (+ 2 2))                ;       (is (= 4 (+ 2 2)))
      (expect 7 (+ 3 4)))               ;       (is (= 7 (+ 3 4))))
    (expecting "with negative integers" ;     (testing "with negative integers"
      (expect -4 (+ -2 -2))             ;       (is (= -4 (+ -2 -2)))
      (expect -1 (+ 3 -4)))))           ;       (is (= -1 (+ 3 -4))))))
```

The third example could also be written as follows, since `expect`
allows an arbitrary predicate in the "expected" position:

```clojure
  (expect #(.startsWith % "ab") "abcde")
```

Or like this, since `expect` allows a regular expression in the "expected" position:

```clojure
  (expect #"^ab" "abcde")
```

Both of these more accurately reflect an expectation on the actual
value `"abcde"`, that the string begins with `"ab"`, than the `is`
equivalent which has the actual value embedded in the test expression.
Separating the "expectation" (value or predicate) from the "actual"
expression being tested often makes the test much clearer.

## Differences from the Clojure version of `expectations/clojure-test`

Here is the list of features from Expectations supported by the
Clojure version of `expectations.clojure.test` where there are
differences in the ClojureScript implementation.

### * Class test

Classes are all different in ClojureScript, and in some cases things
that would be a class in Clojure are different in ClojureScript.  For
instance, lists are a class:
```clojure
(defexpect class-test cljs.core/List '(a b c))
```
and this test passes.  Strings, however, don't have an easily
discoverable type or class, and are better handled with a predicate:
```clojure
(defexpect string-class-test string? "abc")
```
In general, the classes in ClojureScript will not be the same as
the classes in Clojure.  You can do this to write a test that
will work in both environments:
```clojure
(defexpect both-class-test (expect (= (type "abc") (type "def"))))
```
but you cannot write this:
```
(defexpect bad-both-class-test (type "abc") (type "def"))
```
because `(type "abc")` yields something that tests positive as a
`fn?`, causing expectations to think it is a predicate.  Which,
as it happens, it is not.

### * Exception test

Exceptions are very different in ClojureScript from Clojure.

The Clojure example:
```clojure
(defexpect divide-by-zero ArithmeticException (/ 12 0))
```
doesn't even throw an exception -- it returns `##Inf`.
You can do this for that situation:
```clojure
(defexpect divide-by-zero ##Inf (/ 12 0))
```
but be careful putting `##Inf` in a reader conditional, as some versions of
Clojure don't handle that well.  But all of this is a bit off-topic,
as we are discussing exceptions.

Exceptions certainly exist and can be thrown. You can throw pretty
much anything in Javascript.  There is no `Throwable` class in
Clojurecript to distinguish things that can be thrown from anything
else.  The only exception supported in `expectations/clojure-test`
in ClojureScript is where the exception is: `js/Error`.  For example:
```clojure
(defexpect exception js/Error (count 5))
```
will pass, because `(count 5)` throws `js/Error`.

### * `with-test`
There is no `with-test` in `cljs.test`, so it is not available in
`expectations/clojure-test`.

### * Specs
Specs are always supported, and work equivalently to Clojure.

# Useful Additional Information

The end of the Clojure [Getting Started](/doc/getting-started.md) provides
additional information on how to use `expectations/clojure-test`, and most
of the information is directly applicable to using `expectations/clojure-test`
in ClojureScript as well.

# Further Reading

Expectations provides a lot more:

* [Useful Predicates](/doc/useful-predicates.md)
* [Collections](/doc/collections.md)
* [Expecting More](/doc/more.md)
* [Expecting Side Effects](/doc/side-effects.md)
* [Fixtures & Focused Test Execution](/doc/fixtures-focus.md)
