# Change Log

> NOTE: Clojars has introduced a [Verified Group Names policy](https://github.com/clojars/clojars-web/wiki/Verified-Group-Names) which means no new libraries can be pushed to the `expectations` group, and `doo` filters out JAR artifacts that begin `clojure-` for self-hosted ClojureScript environments (i.e., `planck`), so continuing to use `clojure-test` for the artifact name is not viable. Accordingly, the 2.x versions of this library are published as `com.github.seancorfield/expectations`.

## Stable Releases

Only accretive/fixative changes will be made from now on.

* 2.0.next in progress
  * Fix [#24](https://github.com/clojure-expectations/clojure-test/issues/24) by using a local (gensym) for the actual value in `more` and `more->` so it is only evaluated once.
  * Update `build-clj` to v0.6.7 and automate snapshot/release builds.

* 2.0.143 -- 2021-12-01
  * Fix #23 by adding support for set-`in`-set expectations.
  * Documentation updates.
  * Build deps updates.

* 2.0.137 -- 2021-11-07
  * Address #22 by adding `clj-kondo.exports` (this is just a first pass; the `:lint-as` mappings will probably be replaced by hooks in the future).
  * Fix #19 by supporting regex/patterns dynamically inside `=?` (as well as the compile-time support already in `expect`).
  * Update `build-clj` to v0.5.0.
  * Switch to `build.clj`/`tools.build` for CI/deploy.

## 2.0.x Prereleases

* 2.0.0-alpha2 -- 2021-06-09
  * Mostly a documentation refresh, compared to Alpha 1.

* 2.0.0-alpha1 -- 2021-06-05
  * Make `(defexpect foo)` and `(defexpect foo (bar))` match the behavior of `deftest`, without wrapping the body in `(expect ,,,)`. This is potentially breaking insofar as `(defexpect foo (produces-falsey))` would have been a failing test in 1.x but now silently just runs `(produces-falsey)` in the same way that `(deftest foo (produces-falsey))` does.
  * Bring in several test-running functions from `clojure.test`, for convenience in dev/test so users don't need to require `clojure.test` as well.
  * Implement `cljs.test`'s version of `use-fixtures`: accepts functions or hash maps (containing `:before` and/or `:after` keys with 0-arity functions).
  * Add various macro-like constructs back into the source code to improve the documentation (`in`, `from-each`, `more-of`, `more->`, `more` are really only syntactic constructs inside `expect`).
  * Support (self-hosted) ClojureScript via `planck` -- see https://github.com/clojure-expectations/clojure-test/pull/16 for details (@kkinear).

## Previous Releases

These versions required users to also require `clojure.test` and were not as
directly comparable to `clojure.test` behaviors.

* 1.2.1 -- 2019-12-09
  * Fix cljdoc.org index (Collections was missing).

* 1.2.0 -- 2019-12-09
  * Improve failure reporting for `in`; allow it to be combined with `more` etc. #11
  * Add support for mocking return values in `side-effects`.
  * Add support for optional message argument in `expect`. #9
  * Added article-style documentation for cljdoc.org. #6, #7, #8, #10
  * Add example of `more->` equivalent to `thrown-with-msg?`. #5

* 1.1.2 -- 2019-12-07
  * Adds `between` and `between'` for inclusive and exclusive range checking.
  * Fix `in` with a hash map to correctly detect failing cases.
  * Add a first round of tests (finally!). Verified support for Clojure 1.8 (without Spec expectations). Verified full support for Clojure 1.9 and 1.10.1.
  * Clean up `:require` .. `:refer` in README to list all public symbols. #4
  * Fixes links in README. PR #3 (@marekjeszka)
  * Add/improve docstrings. Add `^:no-doc` metadata for cljdoc.org.

* 1.1.1 -- 2019-01-14
  * An expectation can now use a qualified keyword spec to test conformance of the actual value. Failures are reported with the spec explanation. #2
  * If Paul Stadig's Humane Test Output is available (on the classpath), failure reporting is automatically made compatible with it. Expectations that use data structure "equality" (the `=?` extension to `is`) will produce "humane" output for failures, showing differences. #1

* 1.1.0 -- 2019-01-08
  * (broken version)

* 1.0.1 -- 2019-01-02
  * Initial version
