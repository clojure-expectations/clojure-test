# Fixtures & Focused Test Execution

When tests are run -- by Leiningen, Boot, Cognitect's `test-runner`, or your IDE/editor -- it can be useful to perform setup and teardown around those tests, as well as electing to run only a subset of those tests. Those features are known as "fixtures" and "focused tests" (or "selected tests") respectively.

Test fixtures are a way to run arbitrary code before and/or after each test or each group of tests. The classic Expectations library used metadata on certain functions to indicate that they should be run before the entire suite of tests, after the entire suite of tests, or around each individual expectation (test). Since this new Expectations library leans on `clojure.test` for test running infrastructure, it assumes you will use `clojure.test`'s features to provide test fixtures.

Focused tests are identified in the code somehow so that your test runner can execute just a subset of all the tests, when needed. The classic Expectations library used a special `expect-focused` macro to signal that just a subset of tests should be run. This new Expectations library follows the convention of `clojure.test`-based tooling instead, relying on metadata on test functions to signal to all the standard tooling the various test selectors available for the runner to filter on.

## Test Fixtures

To use test fixtures with Expectations, you can refer `use-fixtures` (which is imported from `clojure.test` behind the scenes automatically, as of 2.0.0). For example:

```clojure
(ns my.cool.project-test
  (:require [expectations.clojure.test
             :refer [defexpect expect ,,, use-fixtures]]))
```

You then define your fixture as a function that accepts the test(s) to be run as a single argument, performs whatever setup you need, calls the test(s), and the performs whatever teardown you need. Since tests could throw exceptions, you generally want to use `try`/`finally` here to ensure teardown runs even if the tests abort:

```clojure
(defn my-fixture [work]
  ;; perform test setup
  (try
    (work)
    (finally
      ;; perform test teardown
      )))
```

Then you inform `clojure.test` about your fixture, telling it to run around each test in this namespace, or just once around the whole namespace of tests:

```clojure
;; as a top-level form, usually before you define your tests:
(use-fixtures :each my-fixture) ; run around each test
;; or
(use-fixtures :once my-fixture) ; run once around the whole namespace
```

`use-fixtures` can accept multiple fixture functions if you need to combine setup and/or teardown from more than one test context.

Here's an example that sets up a database connection pool for use across the whole namespace and sets up a database connection for use in each test:

```clojure
(ns my.cool.project-test
  (:require [expectations.clojure.test
             :refer [defexpect expect in ,,, use-fixtures]]
            [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [project.membership :as sut])
  (:import (com.zaxxer.hikari HikariDataSource)))

(def ^:dynamic *pool* nil)
(def ^:dynamic *con* nil)

(def db-spec {:dbtype "..." :dbname "testdb" ,,,})

(defn pool-setup [work]
  (let [pool (connection/->pool HikariDataSource db-spec)]
    (try
      (binding [*pool* pool]
        (work))
      (finally
        (.close pool)))))

(defn connection-setup [work]
  (with-open [con (jdbc/get-connection *pool*)]
    (binding [*con* con]
      (work))))

(use-fixtures :once pool-setup)
(use-fixtures :each connection-setup)

(def test-user {,,,})

(defexpect db-test
  (expect {:membership/status "active"}
          (in (sut/lookup-membership *con* test-user))))

;; lots more tests that use *con*          
```

When the tests in this namespace are run, the underlying `clojure.test` machinery
will invoke `pool-setup` once, passing in a function that will invoke
`connection-setup` for each test in the namespace,
in turn passing in that test (as a function).

> Note: Fixtures are only executed when tests are run via `clojure.test/run-tests` or `clojure.test/test-vars` -- just invoking a test as a function, e.g., `(db-test)` will not cause the fixtures to run. As of 2.0.0, these are available via `expectations.clojure.test` directly, without requiring `clojure.test`.

## Focused Test Execution

Most test runners allow you to specify namespace(s) or patterns to match namespaces so that you can restrict your test run to just some namespaces in your project rather than all of them.

```bash
> lein test just.this.ns-test
...
> boot test -n just.this.ns-test
...
> clojure -A:test -n just.this.ns-test
...
```

Some runners let you specify a regex to match on the namespaces to run:

```bash
> boot test -I "just.*-test$"
...
> clojure -A:test -r "just.*-test$"
...
```

> `boot` also lets you use a regex to exclude namespaces via `-X`

Some runners let you run a specific test:

```bash
> lein test :only just.this.ns-test/just-this-test
...
> clojure -A:test -v just.this.ns-test/just-this-test
...
```

It is also very useful to be able to tag individual tests with metadata and then include or exclude groups of tests when you run them. Both Leiningen and Cognitect's `test-runner` support this using simple keyword metadata on test functions.

The common example given for this is to mark tests as being "integration" level (rather than "unit" level) so they might only be run in a full continuous integration test suite pass, whereas you might run just the lightweight "unit" tests all the time locally while developing -- the assumption being that integration tests are slow and/or have complex environment setup/teardown requirements.

The test suite for this library annotates some of the negative tests so that they can be excluded when running the tests with Humane Test Output enabled (since that modifies the test report data structure in ways that can be incompatible with the `is-not'` macro used to verify failing tests are reported):

```clojure
(deftest ^:negative not-collection-test
  (is-not' (sut/expect {:foo 1} (in {:foo 2 :cat 4})) (not (=? {:foo 1} {:foo 2}))))
```

`^:negative` is a simple piece of metadata added to `not-collection-test` so that tests can be run like this:

```bash
> clojure -A:test:runner:humane -e :negative
```

By contrast, this runs all the tests (without Humane Test Output enabled):

```bash
> clojure -A:test:runner
```

Cognitect's `test-runner` also has a `-i` option to include only tests marked with specific metadata:

```bash
> clojure -A:test:runner -i :negative
```

This run's _only_ tests marked as being `^:negative`.

Leiningen's approach uses an additional layer in its `project.clj` file where you specify `:test-selectors` which are labels for predicates that run on the metadata of tests to determine whether to include them or not. Run `lein test help` for details. The equivalent of the above "humane" run would be:

```clojure
  :profiles {:humane {:dependencies [pjstadig/humane-test-output "0.10.0"]}}
  :test-selectors {:humane (complement :negative)}
```

and:

```bash
> lein with-profile humane test :humane
```

> `with-profile humane` is equivalent to `-A:humane` in the CLI above and `lein ... test :humane` is equivalent to `-e :negative` because it has `(complement :negative)` in the test selector definition.

# Further Reading

* [Getting Started](/doc/getting-started.md)
* [Useful Predicates](/doc/useful-predicates.md)
* [Collections](/doc/collections.md)
* [Expecting More](/doc/more.md)
* [Expecting Side Effects](/doc/side-effects.md)
