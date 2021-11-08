# Collections

While you can use Clojure's standard functions to manipulate collections of data to build up your tests, Expectations provides some conveniences to make life a bit easier: `in` and `from-each`.

## `expect in`

If you have a function that returns a hash map but you only care about testing certain parts of it, you can write an expectation like this:

```clojure
  (expect {:membership/status "active" :membership/type "platinum"}
          (in (lookup-membership db-spec test-user)))
```

This says "we expect the following key/value pairs to be in the result".

You can also use `in` with sets, lists, and vectors:

```clojure
  (expect "platinum" (in (membership-types db-spec)))
```

It's worth noting that the default output from `in` can be somewhat confusing and this is definitely a case where Humane Test Output helps:

```clojure
  (expect {:foo 1} (in {:bar 2}))

;; default output
FAIL in () (...:...)
  within: (expect {:foo 1} (in {:bar 2}))
expected: (=? e__206__auto__ (clojure.core/select-keys a__203__auto__ (clojure.core/keys e__206__auto__)))
  actual: (not (=? {:foo 1} {}))

;; with Humane Test Output:
FAIL in () (...:...)
  within: (expect {:foo 1} (in {:bar 2}))
expected: {:foo 1}
  actual: {}
    diff: - {:foo 1}
```

This is an area that will be improved in the future.

## `expect from-each`

If you have an expectation that should apply to every element of a collection, such as expecting all `active-users` to have memberships that have an `"active"` status, we can write this:

```clojure
  (expect {:membership/status "active"}
          (from-each [user (find-active-users db-spec)]
            (in (lookup-membership db-spec user))))
```

`from-each` is effectively a shorthand for a test that would otherwise look like this:

```clojure
  (doseq [user (find-active-users db-spec)]
    (expect {:membership/status "active"}
            (in (lookup-membership db-spec user))))
```

A simpler `from-each` example:

```clojure
  (expect even? (from-each [n (range 10)] (* 2 n)))
```

`from-each` also supports `:let` and `:when` syntax just like `for` and `doseq`.

See also [Expecting More](/doc/more.md) for examples of combining `from-each`
with other Expectations for powerful multi-valued tests.

# Further Reading

* [Getting Started](/doc/getting-started.md)
* [Useful Predicates](/doc/useful-predicates.md)
* [Expecting More](/doc/more.md)
* [Expecting Side Effects](/doc/side-effects.md)
* [Fixtures & Focused Test Execution](/doc/fixtures-focus.md)
