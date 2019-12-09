# Fixtures & Focused Test Execution

When tests are run -- by Leiningen, Boot, Cognitect's `test-runner`, or your IDE/editor -- it can be useful to perform setup and teardown around those tests, as well as electing to run only a subset of those tests. Those features are known as "fixtures" and "focused tests" (or "selected tests") respectively.

Test fixtures are a way to run arbitrary code before and/or after each test or each group of tests. The classic Expectations library used metadata on certain functions to indicate that they should be run before the entire suite of tests, after the entire suite of tests, or around each individual expectation (test). Since this new Expectations library leans on `clojure.test` for test running infrastructure, it assumes you will use `clojure.test`'s features to provide test fixtures.

Focused tests are identified in the code somehow so that your test runner can execute just a subset of all the tests, when needed. The classic Expectations library used a special `expect-focused` macro to signal that just a subset of tests should be run. This new Expectations library follows the convention of `clojure.test`-based tooling instead, relying on metadata on test functions to signal to all the standard tooling the various test selectors available for the runner to filter on.

## Test Fixtures

To use test fixtures with Expectations, you will need to require `clojure.test` to make the `use-fixtures` function available. For example:

```clojure
(ns my.cool.project-test
  (:require [clojure.test :refer [use-fixtures]]
            [expectations.clojure.test
             :refer [defexpect expect ,,,]]))
```

You then define your fixture as a function that accepts the test(s) to be run as a single argument, performs whatever setup you need, calls the test(s), and the performs whatever teardown you need. Since tests could throw exceptions, you generally want to use `try`/`finally` here to here teardown runs even if the tests abort:

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
  (:require [clojure.test :refer [use-fixtures]]
            [expectations.clojure.test
             :refer [defexpect expect in ,,,]]
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

When the tests in this namespace are run, `clojure.test` will invoke `pool-setup` once, passing in a function that will invoke `connection-setup` for each test in the namespace,
in turn passing in that test (as a function).

> Note: Fixtures are only executed when tests are run via `clojure.test/run-tests` or `clojure.test/test-vars` -- just invoking a test as a function, e.g., `(db-test)` will not cause the fixtures to run.

## Focused Test Execution

# Further Reading

* [Getting Started](/doc/getting-started.md)
* [Useful Predicates](/doc/useful-predicates.md)
* [Collections](/doc/collections.md)
* [Expecting More](/doc/more.md)
* [Expecting Side Effects](/doc/side-effects.md)
