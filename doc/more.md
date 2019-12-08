# Expecting More

So far we've seen expectations with just a single value or predicate being used to test one or more result values. We will often want to expect several things about our results, and Expectations provides a way to articulate that without writing multiple checks that duplicate the expression being tested.

## `expect more`

If you have multiple predicates that you expect to be satisfied by a given expression, you can use `more` to combine them into a single expectation:

```clojure
  (expect (more vector? not-empty) [1 2 3])
```

This expects the (actual) test value to be a vector and also to be non-empty (we could have specified `seq` there just as easily). This can be particularly powerful when combined with `from-each` to check that multiple expectations hold for computations applied to multiple input values.

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

Going back to our `lookup-membership` example, we might want to expect:

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

# Getting Started, Useful Predicates, Collections, Expecting Side Effects

Further reading:

* [Getting Started](/doc/getting-started.md)
* [Useful Predicates](/doc/useful-predicates.md)
* [Collections](/doc/collections.md)
* [Expecting Side Effects](/doc/side-effects.md)
