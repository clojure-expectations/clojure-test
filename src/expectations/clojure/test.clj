;; copyright (c) 2018-2020 sean corfield, all rights reserved

(ns expectations.clojure.test
  "This namespace provides compatibility with clojure.test and related tooling.

  This namespace should be used standalone, without requiring the 'expectations'
  namespace -- this provides a translation layer from Expectations syntax down
  to clojure.test functionality.

  We do not support ClojureScript in clojure.test mode, sorry."
  (:require [clojure.data :as data]
            [clojure.string :as str]
            [clojure.test :as t]))

;; to avoid the need to pull in clojure.test for use-fixtures

(defmacro before-each
  "Execute the given block of code before each test."
  [& body]
  `(t/use-fixtures :each (fn [t#] ~@body (t#))))

(defmacro after-each
  "Execute the given block of code after each test."
  [& body]
  `(t/use-fixtures :each (fn [t#] (try (t#) (finally ~@body)))))

(defmacro around-each
  "With a single argument, assume it is a function that accepts a test
  and executes code around the test. With two arguments, assume it is a
  block to execute before and a block to execute after each test."
  ([wrapper]
   `(t/use-fixtures :each ~wrapper))
  ([before after]
   `(t/use-fixtures :each (fn [t#] ~before (try (t#) (finally ~after))))))

(defmacro before-once
  "Execute the given block of code before running tests in this ns."
  [& body]
  `(t/use-fixtures :once (fn [t#] ~@body (t#))))

(defmacro after-once
  "Execute the given block of code after running tests in this ns."
  [& body]
  `(t/use-fixtures :each (fn [t#] (try (t#) (finally ~@body)))))

(defmacro around-once
  "With a single argument, assume it is a function that accepts a test group
  and executes code around that test group. With two arguments, assume it is a
  block to execute before and a block to execute after the test group."
  ([wrapper]
   `(t/use-fixtures :once ~wrapper))
  ([before after]
   `(t/use-fixtures :once (fn [t#] ~before (try (t#) (finally ~after))))))

(def humane-test-output?
  "If Humane Test Output is available, activate it, and enable compatibility
  of our =? with it.

  This Var will be `true` if Humane Test Output is available and activated,
  otherwise it will be `nil`."
  (try
    (require 'pjstadig.humane-test-output)
    ((resolve 'pjstadig.humane-test-output/activate!))
    true
    (catch Throwable _)))

;; stub functions for :refer compatibility:
(defn- bad-usage [s]
  `(throw (IllegalArgumentException.
           (str ~s " should only be used inside expect"))))

(defmacro in
  "`(expect expected (in actual))` -- expect a subset of a collection.

  If `actual` is a hash map, `expected` can be a hash map of key/value pairs
  that you expect to be in the `actual` result (there may be other key/value
  pairs, which are ignored).

  If `actual` is a set, vector, or list, `expected` can be any value that
  you expect to be a member of the `actual` data.

  `(expect {:b 2} (in {:a 1 :b 2 :c 3}))`
  `(expect 2 (in #{1 2 3}))`
  `(expect 2 (in [1 2 3]))`

  `in` may only be used inside `expect` and is a purely syntactic construct.
  It should not be `refer`'d nor used as a qualified symbol."
  [coll]
  (bad-usage "in"))

(defmacro from-each
  "`(expect expected (from-each [v coll] (f v)))` -- expect this to be true
  for each element of collection. `(f v)` is the actual result.

  Equivalent to: `(doseq [v coll] (expect expected (f v)))`

  `(expect even? (from-each [v (range 10)] (* 2 v)))`

  `from-each` may only be used inside `expect` and is a purely syntactic construct.
  It should not be `refer`'d nor used as a qualified symbol."
  [bindings & body]
  (bad-usage "from-each"))

(defmacro more-of
  "`(expect (more-of destructuring expected1 actual1 ...) actual)` -- provide
  multiple expectations on `actual` based on binding it against the
  `destructuring` expression (like in a `let`) and then expecting things about
  its subcomponents.

  Equivalent to: `(let [destructuring actual] (expect expected1 actual1) ...)`

  `(expect (more-of [a b] string? a int? b) [\"test\" 42])`

  `more-of` may only be used inside `expect` and is a purely syntactic construct.
  It should not be `refer`'d nor used as a qualified symbol."
  [destructuring & expected-actual-pairs]
  (bad-usage "more-of"))

(defmacro more->
  "`(expect (more-> expected1 (threaded1) ...) actual)` -- provide multiple
  expectations on `actual` based on threading it into various expressions.

  Equivalent to: `(do (expect expected1 (-> actual (threaded1))) ...)`

  `(expect (more-> string? (first) int? (second)) [\"test\" 42])`

  `more->` may only be used inside `expect` and is a purely syntactic construct.
  It should not be `refer`'d nor used as a qualified symbol."
  [& expected-threaded-pairs]
  (bad-usage "more->"))

(defmacro more
  "`(expect (more expected1 ...) actual)` -- provide multiple expectations
  on `actual` as a series of expected results.

  Equivalent to: `(do (expect expected1 actual) ...)`

  `(expect (more int? even?) 42)`

  `more` may only be used inside `expect` and is a purely syntactic construct.
  It should not be `refer`'d nor used as a qualified symbol."
  [& expecteds]
  (bad-usage "more"))

(defn ^:no-doc spec? [e]
  (and (keyword? e)
       (try
         (require 'clojure.spec.alpha)
         (when-let [get-spec (resolve 'clojure.spec.alpha/get-spec)]
           (boolean (get-spec e)))
         (catch Throwable _))))

;; smart equality extension to clojure.test assertion -- if the expected form
;; is a predicate (function) then the assertion is equivalent to (is (e a))
;; rather than (is (= e a)) and we need the type check done at runtime, not
;; as part of the macro translation layer
(defmethod t/assert-expr '=? [msg form]
  ;; (is (=? val-or-pred expr))
  (let [[_ e a] form
        conform? (spec? e)]
    `(let [e# ~e
           a# ~a
           valid?# (when ~conform? (resolve 'clojure.spec.alpha/valid?))
           explain-str?# (when ~conform? (resolve 'clojure.spec.alpha/explain-str))
           r# (cond ~conform?
                    (valid?# e# a#)
                    (fn? e#)
                    (e# a#)
                    :else
                    (= e# a#))
           humane?# (and humane-test-output? (not (fn? e#)) (not ~conform?))]
       (if r#
         (t/do-report {:type :pass, :message ~msg,
                       :expected '~form, :actual (if (fn? e#)
                                                   (list '~e a#)
                                                   a#)})
         (t/do-report {:type :fail, :message (if ~conform?
                                               (if ~msg
                                                 (str ~msg "\n"
                                                      (explain-str?# e# a#))
                                                 (explain-str?# e# a#))
                                               ~msg)
                       :diffs (if humane?#
                                [[a# (take 2 (data/diff e# a#))]]
                                [])
                       :expected (if humane?# e# '~form)
                       :actual (cond (fn? e#)
                                     (list '~'not (list '~e a#))
                                     humane?#
                                     [a#]
                                     :else
                                     (list '~'not (list '~'=? e# a#)))}))
       r#)))

(defmacro ^:no-doc ?
  "Wrapper for forms that might throw an exception so exception class names
  can be used as predicates. This is only needed for `more->` so that you can
  thread exceptions into code that can parse information out of them, to be
  used with various expect predicates."
  [form]
  `(try ~form (catch Throwable t# t#)))

(defn ^:no-doc all-report
  "Given an atom in which to accumulate results, return a function that
  can be used in place of clojure.test/do-report, which simply remembers
  all the reported results.

  This is used to support the semantics of expect/in."
  [store]
  (fn [m]
    (swap! store update (:type m) (fnil conj []) m)))

(defmacro expect
  "Translate Expectations DSL to clojure.test language.

  These are approximate translations for the most basic forms:

  `(expect actual)`               => `(is actual)`

  `(expect expected actual)`      => `(is (= expected actual))`

  `(expect predicate actual)`     => `(is (predicate actual))`

  `(expect regex actual)`         => `(is (re-find regex actual))`

  `(expect ClassName actual)`     => `(is (instance? ClassName actual))`

  `(expect ExceptionType actual)` => `(is (thrown? ExceptionType actual))`

  `(expect spec actual)`          => `(is (s/valid? spec actual))`

  An optional third argument can be provided: a message to be included
  in the output if the test fails.

  In addition, `actual` can be `(from-each [x coll] (computation-of x))`
  or `(in set-of-results)` or `(in larger-hash-map)`.

  Also, `expect` can be one of `(more predicate1 .. predicateN)`,
  `(more-> exp1 expr1 .. expN exprN)` where `actual` is threaded through
  each expression `exprX` and checked with the expected value `expX`,
  or `(more-of binding exp1 val1 .. expN valN)` where `actual` is
  destructured using the `binding` and then each expected value `expX`
  is used to check each `valX` -- expressions based on symbols in the
  `binding`."
  ([a] `(t/is ~a))
  ([e a] `(expect ~e ~a nil true ~e))
  ([e a msg] `(expect ~e ~a ~msg true ~e))
  ([e a msg ex? e']
   (let [within (if (and (sequential? e') (= 'expect (first e')))
                  `(pr-str '~e')
                  `(pr-str (list '~'expect '~e' '~a)))
         msg' `(str/join
                "\n"
                (cond-> []
                  ~msg
                  (conj ~msg)
                  ~(not= e e')
                  (conj (str "  within: " ~within))))]
     (cond
      (and (sequential? a) (= 'from-each (first a)))
      (let [[_ bindings & body] a]
        (if (= 1 (count body))
          `(doseq ~bindings
             (expect ~e ~(first body) ~msg ~ex? ~e))
          `(doseq ~bindings
             (expect ~e (do ~@body) ~msg ~ex? ~e))))

      (and (sequential? a) (= 'in (first a)))
      (let [form `(~'expect ~e ~a)]
        `(let [a#     ~(second a)
               not-in# (str '~e " not found in " a#)
               msg#    (if (seq ~msg') (str ~msg' "\n" not-in#) not-in#)]
           (cond (or (sequential? a#) (set? a#))
                 (let [all-reports# (atom nil)
                       one-report# (atom nil)]
                   ;; we accumulate any and all failures and errors but we
                   ;; only accumulate passes if each sequential expectation
                   ;; fully passes (i.e., no failures or errors)
                   (with-redefs [t/do-report (all-report one-report#)]
                     (doseq [a'# a#]
                       (expect ~e a'# msg# ~ex? ~form)
                       (if (or (contains? @one-report# :error)
                               (contains? @one-report# :fail))
                         (do
                           (when (contains? @one-report# :fail)
                             (swap! all-reports#
                                    update :fail into (:fail @one-report#)))
                           (when (contains? @one-report# :error)
                             (swap! all-reports#
                                    update :error into (:error @one-report#))))
                         (when (contains? @one-report# :pass)
                           (swap! all-reports#
                                  update :pass into (:pass @one-report#))))
                       (reset! one-report# nil)))

                   (if (contains? @all-reports# :pass)
                     ;; report all the passes (and no failures or errors)
                     (doseq [r# (:pass @all-reports#)] (t/do-report r#))
                     (do
                       (when-let [r# (first (:error @all-reports#))]
                         (t/do-report r#))
                       (when-let [r# (first (:fail @all-reports#))]
                         (t/do-report r#)))))
                 (map? a#)
                 (let [e# ~e]
                   (expect e# (select-keys a# (keys e#)) ~msg ~ex? ~form))
                 :else
                 (throw (IllegalArgumentException. "'in' requires map or sequence")))))

      (and (sequential? e) (= 'more (first e)))
      (let [es (mapv (fn [e] `(expect ~e ~a ~msg ~ex? ~e')) (rest e))]
        `(do ~@es))

      (and (sequential? e) (= 'more-> (first e)))
      (let [es (mapv (fn [[e a->]]
                       (if (and (sequential? a->)
                                (symbol? (first a->))
                                (let [s (name (first a->))]
                                  (or (str/ends-with? s "->")
                                      (str/ends-with? s "->>"))))
                         `(expect ~e (~(first a->) (? ~a) ~@(rest a->)) ~msg false ~e')
                         `(expect ~e (-> (? ~a) ~a->) ~msg false ~e')))
                     (partition 2 (rest e)))]
        `(do ~@es))

      (and (sequential? e) (= 'more-of (first e)))
      (let [es (mapv (fn [[e a]] `(expect ~e ~a ~msg ~ex? ~e'))
                     (partition 2 (rest (rest e))))]
        `(let [~(second e) ~a] ~@es))

      (and ex? (symbol? e) (resolve e) (class? (resolve e)))
      (if (seq `~msg')
        (if (isa? (resolve e) Throwable)
          `(t/is (~'thrown? ~e ~a) ~msg')
          `(t/is (~'instance? ~e ~a) ~msg'))
        (if (isa? (resolve e) Throwable)
          `(t/is (~'thrown? ~e ~a))
          `(t/is (~'instance? ~e ~a))))

      (isa? (type e) java.util.regex.Pattern)
      (if (seq `~msg')
        `(t/is (re-find ~e ~a) ~msg')
        `(t/is (re-find ~e ~a)))

      :else
      (if (seq `~msg')
        `(t/is (~'=? ~e ~a) ~msg')
        `(t/is (~'=? ~e ~a)))))))

(comment
  (macroexpand '(expect (more-> 1 :a 2 :b 3 (-> :c :d)) {:a 1 :b 2 :c {:d 4}}))
  (macroexpand '(expect (more-of a 2 a) 4))
  (macroexpand '(expect (more-of {:keys [a b c]} 1 a 2 b 3 c) {:a 1 :b 2 :c 3})))

(defn- contains-expect?
  "Given a form, return true if it contains any calls to the 'expect' macro."
  [e]
  (when (and (coll? e) (not (vector? e)))
    (or (= 'expect (first e))
        (some contains-expect? e))))

(defmacro defexpect
  "Given a name (a symbol that may include metadata) and a test body,
  produce a standard 'clojure.test' test var (using 'deftest').

  `(defexpect name expected actual)` is a special case shorthand for
  `(defexpect name (expect expected actual))` provided as an easy way to migrate
  legacy Expectation tests to the 'clojure.test' compatibility version.

  `(defexpect name actual)` is a special case shorthand for
  `(defexpect name (expect actual))` which is equivalent to
  `(deftest name (is actual))`"
  [n & body]
  (if (and (<= (count body) 2)
           (not (some contains-expect? body)))
    (if (<= (count body) 1)
      ;; #13 match deftest behavior starting in 2.0.0
      `(t/deftest ~n ~@body)
      ;; but still treat (defexpect my-name pred (expr)) as a special case
      `(t/deftest ~n (expect ~@body)))
    `(t/deftest ~n ~@body)))

(defmacro expecting
  "The Expectations version of `clojure.test/testing`."
  [string & body]
  `(t/testing ~string ~@body))

(defmacro side-effects
  "Given a vector of functions to track calls to, execute the body.

  Returns a vector of each set of arguments used in calls to those
  functions. The specified functions will not actually be called:
  only their arguments will be tracked. If you need the call to return
  a specific value, the function can be given as a pair of its name
  and the value you want its call(s) to return. Functions given just
  by name will return `nil`."
  [fn-vec & forms]
  (when-not (vector? fn-vec)
    (throw (IllegalArgumentException.
            "side-effects requires a vector as its first argument")))
  (let [mocks (reduce (fn [m f-spec]
                        (if (vector? f-spec)
                          (assoc m (first f-spec) (second f-spec))
                          (assoc m f-spec nil)))
                      {}
                      fn-vec)
        called-args (gensym "called-args")]
    `(let [~called-args (atom [])]
       (with-redefs ~(reduce-kv (fn [defs f v]
                                  (conj defs
                                        f
                                        `(fn [& args#]
                                           (swap! ~called-args conj args#)
                                           ~v)))
                                []
                                mocks)
         ~@forms)
       @~called-args)))

(defn approximately
  "Given a value and an optional delta (default 0.001), return a predicate
  that expects its argument to be within that delta of the given value."
  ([^double v] (approximately v 0.001))
  ([^double v ^double d]
   (fn [x] (<= (- v (Math/abs d)) x (+ v (Math/abs d))))))

(defn between
  "Given a pair of (numeric) values, return a predicate that expects its
  argument to be be those values or between them -- inclusively."
  [a b]
  (fn [x] (<= a x b)))

(defn between'
  "Given a pair of (numeric) values, return a predicate that expects its
  argument to be (strictly) between those values -- exclusively."
  [a b]
  (fn [x] (< a x b)))

(defn functionally
  "Given a pair of functions, return a custom predicate that checks that they
  return the same result when applied to a value. May optionally accept a
  'difference' function that should accept the result of each function and
  return a string explaininhg how they actually differ.
  For explaining strings, you could use expectations/strings-difference.
  (only when I port it across!)

  Right now this produces pretty awful failure messages. FIXME!"
  ([expected-fn actual-fn]
   (functionally expected-fn actual-fn (constantly "not functionally equivalent")))
  ([expected-fn actual-fn difference-fn]
   (fn [x]
     (let [e-val (expected-fn x)
           a-val (actual-fn x)]
       (t/is (= e-val a-val) (difference-fn e-val a-val))))))
