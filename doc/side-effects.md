# Expecting Side Effects

Sometimes the code we are testing calls out to other functions to cause side effects.
When we are testing, we don't always want side effects to happen but we often want
to make sure the code being tested still calls those functions.

Expectations provides a `side-effects` macro that lets you run the code under
test with those side-effecting functions mocked out, and returns a vector of
all of the argument lists passed in calls to those functions.

```clojure
(defn my-fn [x] (cond x        (println "x" x)
                      (nil? x) (println "no value")))

(defexpect my-fn-test
  (expect [["x" 42]]
          (side-effects [println] (my-fn 42)))
  (expect [["no value"]]
          (side-effects [println] (my-fn nil)))
  (expect empty?
          (side-effects [println] (my-fn false))))
```

Our function under test calls `println` conditionally. We want to verify the logic
so we expect `println` to be called with `"x"` and the (truthy) value passed to `my-fn`,
`"no value"` when `nil` is passed to `my-fn`, and for there to be no calls to
`println` if other values are passed (i.e., if `false` is passed).

By default, mocked out calls return `nil`, but sometimes your code under test
will expect other values back, in order for you to correctly test paths through
that code. You can specify the mocked function as a pair of its name and its
return value in that case.

```clojure
(defn my-pred [x] (= 42 x))
(defn my-code [x] (if (my-pred x) (println "good") (println "bad")))

(defexpect my-code-test-1
  (expect [[99] ["bad"]]
          (side-effects [my-pred println] (my-code 99)))
  ;; this will fail because the mocked my-pred returns nil
  (expect [[42] ["good"]]
          (side-effects [my-pred println] (my-code 42))))

(defexpect my-code-test-2
  (expect [[99] ["bad"]]
          (side-effects [my-pred println] (my-code 99)))
  (expect [[42] ["good"]]
          (side-effects [[my-pred true] println] (my-code 42))))
```

Here's the output of running those two tests:

```clojure
user=> (my-code-test-1)

FAIL in (my-code-test-1) (...:...)
(side-effects [my-pred println] (my-code 42))

expected: (= [[42] ["good"]] (side-effects [my-pred println] (my-code 42)))
  actual: (not= [[42] ["good"]] [(42) ("bad")])
nil
user=> (my-code-test-2)
nil
```

As we can see, the second expectation in `my-code-test-1` fails because `my-code`
calls `println` with `"bad"` -- because the mocked `my-pred` returns `nil` and
so `my-code` executes the non-truthy path.

In `my-code-test-2`, we mock `my-pred` to return `true` and so `my-code` executes
the truthy path and we can verify that calls `println` as expected.

Because `side-effects` can return multiple results, and each result is a sequence
of values, it often helps to combine `side-effects` with `more-of`:

```clojure
(defn my-pred [x] (= 42 x))
(defn my-code [x] (if (my-pred x) (println "good") (println "bad")))

(defexpect my-code-test
  (expect (more-of [[x] [msg] :as all]
                   2     (count all) ; check there were just two calls
                   99    x
                   "bad" msg)
          (side-effects [my-pred println] (my-code 99)))
  (expect (more-of [[x] [msg] :as all]
                   2      (count all) ; check there were just two calls
                   42     x
                   "good" msg)
          (side-effects [[my-pred true] println] (my-code 42))))
```

# Further Reading

* [Getting Started](/doc/getting-started.md)
* [Useful Predicates](/doc/useful-predicates.md)
* [Collections](/doc/collections.md)
* [Expecting More](/doc/more.md)
* [Fixtures & Focused Test Execution](/doc/fixtures-focus.md)
