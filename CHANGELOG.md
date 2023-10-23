# Change Log

> NOTE: Since Clojars introduced a [Verified Group Names policy](https://github.com/clojars/clojars-web/wiki/Verified-Group-Names), no new libraries could be pushed to the `expectations` group, and `doo` filters out JAR artifacts that begin `clojure-` for self-hosted ClojureScript environments (i.e., `planck`), so continuing to use `clojure-test` for the artifact name is not viable. Accordingly, the 2.x versions of this library are published as `com.github.seancorfield/expectations`.

## Stable Releases

Only accretive/fixative changes will be made from now on.

* 2.1.187 -- 2023-10-22
  * Address [#29](https://github.com/clojure-expectations/clojure-test/issues/29) by providing a "hook" for `more-of`.
  * Update `tools.build` to 0.9.6 (and get rid of `template/pom.xml` in favor of new `:pom-data` option to `b/write-pom`).

* 2.1.182 -- 2023-09-29
  * Improved failure reporting: most failures now provide an additional message describing the failure as well as improving how the expected and actual values are displayed (primarily hiding `=?` and showing a more accurate/intuitive test form).
  * Update `deps.edn` to use `:main-args` (instead of `:main-opts`) for parameterized tasks in `build.clj` -- see [Running Tasks based on Aliases](https://clojure-doc.org/articles/cookbooks/cli_build_projects/)
  * Drop support for Java 8 (it may continue to work but it is no longer tested).
  * Update dependencies to latest stable versions.

* 2.0.165 -- 2023-01-31
  * Fix [#30](https://github.com/clojure-expectations/clojure-test/issues/30) by removing `build-clj` and using raw `tools.build`.
  * Address [#29](https://github.com/clojure-expectations/clojure-test/issues/29) by providing a "hook" for `more->` (but more work is needed).
  * Address [#27](https://github.com/clojure-expectations/clojure-test/issues/27) by changing `refer`'d note in stub macros' docstrings.

* 2.0.160 -- 2022-03-26
  * Fix [#28](https://github.com/clojure-expectations/clojure-test/issues/28) by recognizing qualified calls to `expect` (to suppress legacy behavior in more cases).
  * Update `build-clj` to v0.8.0.

* 2.0.157 -- 2022-01-25
  * Fix a small regression in how classes are treated when used as predicates in `expect`.

* 2.0.156 -- 2022-01-19
  * Address [#26](https://github.com/clojure-expectations/clojure-test/issues/26) by adding an example combining `more->` and `more-of` around destructuring `ex-info` data.
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
