# Expecting More

So far we've seen expectations with just a single value or predicate being used to test one or more result values. We will often want to expect several things about our results, and Expectations provides a way to articulate that without writing multiple checks that duplicate the expression being tested.

## `expect more`

If you have multiple predicates that you expect to be satisfied by a given expression, you can use `more` to combine them into a single expectation:

```clojure
  (expect (more vector? not-empty) [1 2 3])
```

This expects the (actual) test value to be a vector and also to be non-empty (we could have specified `seq` there just as easily). This can be particularly powerful when combined with `from-each` to check that multiple expectations hold for computations applied to multiple input values:

```clojure
  (expect (more vector? not-empty)
          (from-each [n [1 2 3]]
            (into [] (range n))))
```

> If you have expectations that should hold for **all** input values, you might want to look at [`clojure.test.check`](https://github.com/clojure/test.check) instead.

## `expect more->`

If you have a series of predicates (or values) that you expect to be satisfied by a given expression after a certain amount of preprocessing, you can use the threaded version -- `more->` -- to express that:

```clojure
  (expect (more-> 1 first
                  3 last)
          [1 2 3])
```

`more->` accepts a series of predicate (or value) and expression pairs.
The (actual) test value is threaded through each of the expressions and the predicate (or value) is expected of the result.

The example above is equivalent to these two expectations:

```clojure
  (expect 1 (-> [1 2 3] first))
  (expect 3 (-> [1 2 3] last))
```

Going back to our `lookup-membership` example (in
[**Collections**](/doc/collections.md) and
[**Fixtures**](/doc/fixtures-focus.md)), we might want to expect:

```clojure
  (expect (more-> #{:membership/id :membership/status :membership/type ,,,}
                  (-> keys set)
                  pos?       :membership/id
                  "active"   :membership/status
                  "platinum" :membership/type)
          (lookup-membership db-spec test-user))
```

Since the test value is threaded-first into the expressions, we can use `->` to further thread the value into additional processing steps, such as getting the sequence of `keys` and turning that into a `set` for the expectation of which keys should be returned in a membership hash map. In addition, we expect that `(-> actual :membership/id)` is positive `pos?` and that certain other keys have specific values.

`clojure.test` provides `thrown-with-msg?` as a way to assert both the type of exception thrown and a regular expression that should apply to the message in that exception. `more->` allows us to do that in a more general way:

```clojure
  (is (thrown-with-msg? ArithmeticException #"Divide by zero" (/ 1 0)))
  (expect (more-> ArithmeticException type
                  #"Divide by zero"   ex-message)
          (/ 1 0))
```

See below for a more comprehensive example of exception testing that also uses `more-of`.

## `expect more-of`

Sometimes destructuring an (actual) test value is the easiest way to apply your expectations:

```clojure
  (expect (more-of {:membership/keys [id status type] :as data}
                   ,,,
                   pos?       id
                   "active"   status
                   "platinum" type)
          (lookup-membership db-spec test-user))
```

The expectation on the set of keys has been omitted here to highlight how the destructuring may simplify the other expectations, but it would be:

```clojure
                   #{:membership/id :membership/status :membership/type ,,,}
                   (set (keys data))
```

Some simpler examples (taken from Expectations' original documentation):

```clojure
  (expect (more-of x
                   vector? x
                   1       (first x))
          [1 2 3])
  (expect (more-of [x :as all]
                   vector? all
                   1       x)
          [1 2 3])
```

You can think of `more-of` as being a shorthand for a predicate function
that `expect`s the pairs in its body:

```clojure
  (expect (fn [x]
            (expect vector? x)
            (expect 1 (first x)))
          [1 2 3])
  (expect (fn [[x :as all]]
            (expect vector? all)
            (expect 1 x))
          [1 2 3])
```

`more-of` can be used with `from-each` to provide functionality similar
to `are` in `clojure.test` (but more powerful):

```clojure
  (deftest are-example
    (are [expected start end]
         (= expected (range start end))
         [0 1 2 3] 0 4
         []        0 0
         [1 2 3]   1 4))

  (defexpect equivalent-to-are
    (expect (more-of [expected actual]
                     expected actual)
            (from-each [[expected start end]
                        [[[0 1 2 3] 0 4]
                         [[]        0 0]
                         [[1 2 3]   1 4]]]
              [expected (range start end)])))
```

Although this is more verbose for this basic example, remember that the
`expected` value could also be a predicate function, a regex, a Spec, etc:

```clojure
  (s/def ::coll-of-ints (s/coll-of int?))

  (defexpect more-than-are
    (expect (more-of [expected actual]
                     ::coll-of-ints actual
                     expected actual)
            (from-each [[expected start end]
                        [[[0 1 2 3] 0 4]
                         [empty?    0 0]
                         [[1 2 3]   1 4]]]
              [expected (range start end)])))
```

> Note: a Spec is only recognized as literal keyword in the "expected" position so it has to be directly in `more-of` rather than passed via `from-each`. This restriction will probably be lifted in a future release.

`more-of` can also be used with `more->` to provide succinct tests on Clojure's `ex-info` exceptions:

```clojure
  (defexpect ex-info-tests
    (expect (more-> clojure.lang.ExceptionInfo     type
                    #"boo"                         ex-message
                    (more-of {:keys [status responseCode]}
                             409 status
                             4001110 responseCode) ex-data)
            (throw (ex-info "boo" {:status 409 :responseCode 4001110}))))
```

In this example, the exception is threaded into `type` and the predicate is a class,
it is threaded into `ex-message` and the predicate is a regex,
and it is also threaded into `ex-data` and the predicate is a `more-of` expression that destructures that data and matches parts of it.

Another good use of `more-of` is for expectations on
[**Side Effects**](/doc/side-effects.md).

# Further Reading

* [Getting Started](/doc/getting-started.md)
* [Useful Predicates](/doc/useful-predicates.md)
* [Collections](/doc/collections.md)
* [Expecting Side Effects](/doc/side-effects.md)
* [Fixtures & Focused Test Execution](/doc/fixtures-focus.md)
