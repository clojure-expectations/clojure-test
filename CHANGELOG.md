# Unreleased changes on **master**:

* Add support for mocking return values in `side-effects`.
* Add support for optional message argument in `expect`. #9
* Added article-style documentation for cljdoc.org.

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
