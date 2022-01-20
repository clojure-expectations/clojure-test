# Getting Started with expectations.clojure.test

This library provides an expressive alternative to [`clojure.test`](http://clojure.github.io/clojure/clojure.test-api.html), based on the syntax of Jay Fields' [Expectations](https://clojure-expectations.github.io/) library, but fully compatible with all the `clojure.test`-based tooling out there: no special plugins or editor modes are needed.

## Installation

You can add `expectations.clojure.test` to your project with either:

```clojure
;; add this to :extra-deps under a :test alias:
com.github.seancorfield/expectations {:mvn/version "2.0.156"}
```
for `deps.edn` or:

```clojure
;; add this to :dev-dependencies (Leiningen)
[com.github.seancorfield/expectations "2.0.156"]
;; or add this to :dependencies (Boot)
[com.github.seancorfield/expectations "2.0.156" :scope "test"]
```
for `project.clj` or `build.boot`.

Then in your test namespaces, you just require `expectations.clojure.test` (instead of `clojure.test`) and start using the Expectations-style syntax for your tests.

### Requirements

This library is designed to work with Clojure 1.9 or later, and provides support for `clojure.spec` predicates. It is also designed to work with Paul Stadig's [Humane Test Output](https://github.com/pjstadig/humane-test-output), which provides better failure messages for `clojure.test`.

### Humane Test Output

If you have `pjstadig/humane-test-output` as a dependency (i.e., it is on your classpath), then when you require `expectations.clojure.test` it will automatically activate Humane Test Output, regardless of how you are running your tests: again, no need for any special setup or `:injections` (Leiningen).

> Take note of the caveat Paul Stadig provides about some tooling and/or IDEs installing their own "helpers" for `clojure.test` output!

## The Basics

This example provides a quick comparison with `clojure.test` (the tests match those in the [`clojure.test` documentation](http://clojure.github.io/clojure/clojure.test-api.html)):

```clojure
(require '[expectations.clojure.test
           :refer [defexpect expect expecting run-tests test-vars]])

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

## Running Tests

How you run tests will depend a lot on the tooling and/or IDE/editor that you use in your day-to-day workflow.

### Editor Integration

While you are developing tests, it's probably best to run them via your editor (using the REPL connected to it). Most Clojure integrations for editors allow you run an individual test, all tests in a given namespace, or all tests in the project. You'll have to consult the documentation for your chosen editor/integration for the ways to do that. _[In Chlorine for Atom, it's `ctrl-; t` to run a single test and `ctrl-; x` to run all the tests in the current namespace.]_

### REPL

If you are working directly in the REPL (not recommended but, hey...) you can run an individual test simply by calling it, as if it were a function:

```clojure
user=> (simple-test)
nil
```

It will return `nil` and print nothing if the test succeeds. It will print out failure messages otherwise (and still return `nil`). While this is the simplest way to run a test, it is not always the best, since it won't run any test fixtures -- see [Fixtures](/doc/fixtures-focus.md) for more details. You can run a test (with fixtures) like this:

```clojure
user=> (test-vars [#'simple-test])
nil
```

As you might imagine, you can run more than one test using `test-vars`. You can also run all the tests in the current namespace, which produces more informative output:

```clojure
user=> (run-tests)

Testing user

Ran 1 tests containing 8 assertions.
0 failures, 0 errors.
{:test 1, :pass 8, :fail 0, :error 0, :type :summary}
```

As of 2.0.0, `test-vars` and `run-tests` are imported from `clojure.test` automatically behind the scenes, along with other test running functions.

### Cognitect's `test-runner`

This assumes you are using the [CLI and `deps.edn`](https://clojure.org/guides/deps_and_cli) for your project, and that you have set up a `:test` alias per [`test-runner`'s README](https://github.com/cognitect-labs/test-runner/blob/master/readme.md):

```bash
> clojure -X:test
```

### Leiningen

The following is usually sufficient to run tests via Leiningen, assuming your `project.clj` file is set up correctly:

```bash
> lein test
```

### Boot

The following is usually sufficient to run tests via Boot, assuming your `build.boot` file is set up correctly (including [Adzerk's `boot-test`](https://github.com/adzerk-oss/boot-test)):

```bash
> boot test
```

### Test Placement

While not directly related to how to run your tests, it's a common question asked by folks new to Clojure: where should I put my tests?

#### Standard Conventions

Most of the `clojure.test`-based tooling assumes that for each source file `src/path/to/my_code.clj` (which represents the namespace `path.to.my-code`), you will have a test file `test/path/to/my_code_test.clj` with the namespace `path.to.my-code-test`.

That test file will generally start out with:

```clojure
(ns path.to.my-code-test
  (:require [expectations.clojure.test :refer [defexpect expect expecting ,,,]
            [path.to.my-code :refer [the-functions you-want to-test]]]))
```

Following this convention means that all the tooling and IDE/editor integrations should work with no configuration: it's what everyone "expects".

#### Tests with Source Code

`clojure.test` has a macro called `with-test` that allows you to define tests inline following your function definition. Given that `clojure.test` ships directly with Clojure, this is reasonable because putting test code in your function definition's metadata doesn't add any dependencies and it has the benefit of being able to see the source of the function and the source of its test right next to each other. You can do that with Expectations too, since it is `clojure.test`-compatible, although it does mean your source code has an additional dependency -- but Expectations is fairly small (~300 lines) and has no additional dependencies. As of 2.0.0, `with-test` is available directly in `expectations.clojure.test`.

However, if you put tests in your source files, using `with-test`, then most tooling won't know how to find those tests by default. Here's an example of an inline test and how to run it with Leiningen and the CLI (`deps.edn`):

```clojure
(ns my.cool.project
  (:require [expectations.clojure.test :refer [expect with-test]]))

(with-test
  (defn square [x] (* x x))
  (expect 1 (square 1))
  (expect 1 (square -1))
  (expect 100 (square 10)))
```

For Leiningen, you'll need to tell it to look for tests in `src` (as well as `test`) so add this to `project.clj`:

```clojure
  :test-paths ["src" "test"]
```

then you can just run `lein test` and it will check for tests inside the `src` test, find `my.cool.project/square` test metadata and run it as a test.

For the `clojure` CLI, you'll need to tell Cognitect's `test-runner` to look for tests in `src` _and_ you'll have to override it's default regex pattern for matching test namespaces:

```bash
clojure -X:test :dirs '["src"]' :patterns '[".*"]'
```

Of course, you can also update the `:test` alias to add those new options into `:main-opts` so that you don't need them on the command line:

```clojure
{:aliases
 {:test
  {:extra-paths ["test"]
   :extra-deps
   {com.github.seancorfield/expectations {:mvn/version "2.0.156"}
    ;; assumes Clojure CLI 1.10.3.933 or later:
    io.github.cognitect-labs/test-runner
    {:git/tag "v0.5.0" :git/sha "48c3c67"}}
   :exec-fn cognitect.test-runner.api/test
   :exec-args {:dirs ["src" "test"]
               :patterns [".*"]}}}}
```

Note that you'll need both `src` _and_ `test` directories if you want `test-runner` to look in both places.

## Expecting Specs

If you are using Clojure 1.9 or later, you have access to Spec and can `expect` those as well:

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

# Further Reading

While the above can already get you further than `clojure.test`, Expectations provides a lot more:

* [Useful Predicates](/doc/useful-predicates.md)
* [Collections](/doc/collections.md)
* [Expecting More](/doc/more.md)
* [Expecting Side Effects](/doc/side-effects.md)
* [Fixtures & Focused Test Execution](/doc/fixtures-focus.md)
