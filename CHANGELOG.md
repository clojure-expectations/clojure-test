# Version 2.0.0 in progress

* Make `(defexpect foo)` and `(defexpect foo (bar))` match the behavior of `deftest`, without wrapping the body in `(expect ,,,)`. This is potentially breaking insofar as `(defexpect foo (produces-falsey))` would have been a failing test in 1.x but now silently just runs `(produces-falsey)` in the same way that `(deftest foo (produces-falsey))` does.
* Add `before`/`after`/`around` for each/once fixtures, as an alternative for having to require `clojure.test` just for `use-fixtures`. This is an experimental feature and is not yet documented!
* Add various macro-like constructs back into the documentation to improve the documentation (`in`, `from-each`, `more-of`, `more->`, `more` are really only syntactic constructs inside `expect`).

# Version 1.2.1 -- 2019-12-09

* Fix cljdoc.org index (Collections was missing).

# Version 1.2.0 -- 2019-12-09

* Improve failure reporting for `in`; allow it to be combined with `more` etc. #11
* Add support for mocking return values in `side-effects`.
* Add support for optional message argument in `expect`. #9
* Added article-style documentation for cljdoc.org. #6, #7, #8, #10
* Add example of `more->` equivalent to `thrown-with-msg?`. #5

# Version 1.1.2 -- 2019-12-07

* Adds `between` and `between'` for inclusive and exclusive range checking.
* Fix `in` with a hash map to correctly detect failing cases.
* Add a first round of tests (finally!). Verified support for Clojure 1.8 (without Spec expectations). Verified full support for Clojure 1.9 and 1.10.1.
* Clean up `:require` .. `:refer` in README to list all public symbols. #4
* Fixes links in README. PR #3 (@marekjeszka)
* Add/improve docstrings. Add `^:no-doc` metadata for cljdoc.org.

# Version 1.1.1 -- 2019-01-14

* An expectation can now use a qualified keyword spec to test conformance of the actual value. Failures are reported with the spec explanation. #2
* If Paul Stadig's Humane Test Output is available (on the classpath), failure reporting is automatically made compatible with it. Expectations that use data structure "equality" (the `=?` extension to `is`) will produce "humane" output for failures, showing differences. #1

# Version 1.1.0 -- 2019-01-08

(broken version)

# Initial version 1.0.1 -- 2019-01-02
