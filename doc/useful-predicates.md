# Useful Predicates

Since Expectations leans heavily on predicates in `expect` tests, it is common to want to check that values are close to some expected value or are within an expected range. The following predicates are provided to help with that:

## `approximately`

```clojure
  (expect (approximately 19.99) (default-package-price store))
```

This checks that the resulting price is "close to" `19.99`. By default, `approximately` uses a "delta" of `0.001` so the above accepts values between `19.989` and `19.991` inclusively. You can provide your own delta value:

```clojure
  (expect (approximately 100.0 0.5) (total-coverage tests))
```

This checks that the coverage value is within +/- `0.5` of `100.0` (i.e., `99.5 <= v <= 100.5`).

## `between` and `between'`

Instead of expecting a result to be close to a value, you may expect a result to be within a known range of values. These two predicates offer an inclusive range and an exclusive range respectively.

```clojure
  ;; (rand-int 10) will produce 0, 1, 2, .., 9
  (expect (between 0 9)    (rand-int 10))
  (expect (between' -1 10) (rand-int 10))
  ;; if you want an inclusive/exclusive range:
  (let [n 100]
    (expect (between 0 (dec n)) (rand-int n)))
```

## `functionally`

Sometimes you have two functions that you expect to be "functionally equivalent". In other words, when they are given the same argument, they should produce the same value perhaps via different computations. This can also emerge when you have a data structure and a function under test where the result of that function can also be computed via a simple test function for a given set of input data.

_EXAMPLE NEEDS TO GO HERE_

# Getting Started, Collections, Expecting More, Expecting Side Effects

Further reading:

* [Getting Started](/doc/getting-started.md)
* [Collections](/doc/collections.md)
* [Expecting More](/doc/more.md)
* [Expecting Side Effects](/doc/side-effects.md)
