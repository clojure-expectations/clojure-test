# Getting Started with expectations/clojure-test

This library provides an expressive alternative to [`clojure.test`](http://clojure.github.io/clojure/clojure.test-api.html), based on the syntax of Jay Fields' [Expectations](https://clojure-expectations.github.io/) library, but fully compatible with all the `clojure.test`-based tooling out there: no special plugins or editor modes are needed.

## Installation

You can add `expectations/clojure-test` to your project with either:

```clojure
{expectations/clojure-test {:mvn/version "1.1.2"}}
```
for `deps.edn` or:

```clojure
[expectations/clojure-test "1.1.2"]
```
for `project.clj` or `build.boot`.

Then in your test namespaces, you just require `expectations.clojure.test` (instead of `clojure.test`, in most cases) and start using the Expectations-style syntax for your tests.

### Requirements

This library is designed to work with Clojure 1.8 or later, and automatically provides support for `clojure.spec` if you use Clojure 1.9 or later. It is also designed to work with Paul Stadig's [Humane Test Output](https://github.com/pjstadig/humane-test-output), which provides better failure messages for `clojure.test`.

### Humane Test Output

If you have `pjstadig/humane-test-output` as a dependency (i.e., it is on your classpath), then when you require `expectations.clojure.test` it will automatically activate Humane Test Output, regardless of how you are running your tests: again, no need for any special setup or `:injections` (Leiningen).

> Take note of the caveat Paul Stadig provides about some tooling and/or IDEs installing their own "helpers" for `clojure.test` output!

## The Basics

This example provides a quick comparison with `clojure.test` (the tests match those in the [`clojure.test` documentation](http://clojure.github.io/clojure/clojure.test-api.html)):

```clojure
(defexpect simple-test                  ; (deftest simple-test
  (expect 4 (+ 2 2))                    ;   (is (= 4 (+ 2 2)))
  (expect Long 256)                     ;   (is (instance? Long 256))
  (expect (.startsWith "abcde" "ab"))   ;   (is (.startsWith "abcde" "ab"))
  (expect ArithmeticException (/ 1 0))  ;   (is (thrown? ArithmeticException (/ 1 0)))
  (expecting "Arithmetic"               ;   (testing "Arithmetic"
    (expecting "with positive integers" ;     (testing "with positive integers"
      (expect 4 (+ 2 2))                ;       (is (= 4 (+ 2 2)))
      (expect 7 (+ 3 4)))               ;       (is (= 7 (+ 3 4))))
    (expecting "with negative integers" ;     (testing "with negative integers"
      (expect -4 (+ -2 -2))             ;       (is (= -4 (+ -2 -2)))
      (expect -1 (+ 3 -4)))))           ;       (is (= -1 (+ 3 -4))))))
```

The third example could also be written as follows, since `expect` allows an arbitrary predicate in the "expected" position:

```clojure
  (expect #(.startsWith % "ab") "abcde")
```

Or like this, since `expect` allows a regular expression in the "expected" position:

```clojure
  (expect #"^ab" "abcde")
```

Both of these more accurately reflect an expectation on the actual value `"abcde"`, that the string begins with `"ab"`, than the `is` equivalent which has the actual value embedded in the test expression. Separating the "expectation" (value or predicate) from the "actual" expression being tested often makes the test much clearer.

## Expecting Specs

If you are using Clojure 1.9 or later, you have access to Spec and `expect` those as well:

```clojure
(require '[clojure.spec.alpha :as s])
(s/def :small/value (s/and pos-int? #(< % 100)))
(defexpect spec-test
  (expect :small/value (* 14 3)))
```

If an expectation on a Spec fails, you get the explanation as well as the standard `clojure.test` failure:

```clojure
(defexpect spec-failure
  (expect :small/value (* 14 30)))

;; when run:

FAIL in (spec-failure) (...:...)
420 - failed: (< % 100) spec: :small/value

expected: (=? :small/value (* 14 30))
  actual: (not (=? :small/value 420))
```

> The `=?` operator appearing here is an Expectations extension to `clojure.test` that provides an "intelligent equality" that supports predicates and Specs, as well as regular value equality.

## Failure Messages

Just like the `is` macro, the `expect` macro can take an additional (third) argument that is a message to display if the expectation fails:

```clojure
user=> (defexpect failure-msg
         (expect even? (+ 1 1 1) "It's uneven!"))
#'user/failure-msg
user=> (failure-msg)

FAIL in (failure-msg) (...:...)
It's uneven!
expected: (=? even? (+ 1 1 1))
  actual: (not (even? 3))
nil

;; messages are combined in a Spec failure:

user=> (defexpect spec-failure-msg
         (expect :small/value (* 14 30) "Too big!"))
#'user/spec-failure-msg
user=> (spec-failure-msg)

FAIL in (spec-failure) (...:...)
Too big!
420 - failed: (< % 100) spec: :small/value

expected: (=? :small/value (* 14 30))
  actual: (not (=? :small/value 420))
nil

;; expecting adds its message too:

user=> (defexpect another-spec-failure-msg
         (expecting "Large number should fail"
          (expect :small/value (* 14 30) "Too big!"))
         (expecting "Negative number should fail"
          (expect :small/value (* -14 30) "Too small!")))
#'user/another-spec-failure-msg
user=> (another-spec-failure-msg)

FAIL in (another-spec-failure-msg) (...:...)
Large number should fail
Too big!
420 - failed: (< % 100) spec: :small/value

expected: (=? :small/value (* 14 30))
  actual: (not (=? :small/value 420))

FAIL in (another-spec-failure-msg) (...:...)
Negative number should fail
Too small!
-420 - failed: pos-int? spec: :small/value

expected: (=? :small/value (* -14 30))
  actual: (not (=? :small/value -420))
nil
```

# Useful Predicates, Collections, Expecting More, Expecting Side Effects

While the above can already get you further than `clojure.test`, Expectations provides a lot more:

* [Useful Predicates](/doc/useful-predicates.md)
* [Collections](/doc/collections.md)
* [Expecting More](/doc/more.md)
* [Expecting Side Effects](/doc/side-effects.md)
