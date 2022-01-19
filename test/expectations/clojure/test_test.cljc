;; copyright (c) 2019-2021 sean corfield, all rights reserved

(ns expectations.clojure.test-test
  "Test the testing framework -- this is sometimes harder than you might think!

  Tests marked `^:negative` will not pass with Humane Test Output enabled
  because it manipulates the report data which my `is-not` macros rely on."
  (:require [clojure.string :as str]
            #?(:clj [expectations.clojure.test-macros :refer
                     [is-not' is-not passes]]
               :cljs [expectations.clojure.test-macros
                      :refer-macros [is-not' is-not passes]])

            #?(:clj [clojure.test :refer [deftest is do-report testing]]
               :cljs [cljs.test :include-macros true
                          :refer [do-report assert-expr]
                      :refer-macros [deftest is testing assert-expr
                                         use-fixtures]])
            #?(:cljs [cljs.spec.alpha :as s])
            #?(:cljs [expectations.clojure.test-spec])
            #?(:clj [expectations.clojure.test :refer
                     [from-each in more more-of] :as sut]
               :cljs [expectations.clojure.test
                      :include-macros true
                      :as sut])))

; The macros are in test_macros.cljc to support ClojureScript.

(deftest predicate-test
  (is (sut/expect even? (+ 1 1)))
  (is (sut/expect empty? (list)))
  (is-not' (sut/expect even? (+ 1 1 1)) (not (even? 3)))
  (is-not' (sut/expect empty? [1]) (not (empty? [1]))))

(deftest equality-test
  (is (sut/expect 1 (* 1 1)))
  (is (sut/expect "foo" (str "f" "oo"))))

(deftest ^:negative not-equality-test
  (is-not' (sut/expect 2 (* 1 1)) (not (=? 2 1)))
  (is-not' (sut/expect "fool" (str "f" "oo")) (not (=? "fool" "foo"))))

(deftest ^:negative message-test
  (is-not' (sut/expect even? (+ 1 1 1) "It's uneven!")
           (not (even? 3))
           #"uneven")
  (is-not' (sut/expect empty? [1] "It's partly full!")
           (not (empty? [1]))
           #"full")
  (is-not' (sut/expect 2 (* 1 1) "One times one isn't two?")
           (not (=? 2 1))
           #"isn't two")
  (is-not' (sut/expect "fool" (str "f" "oo") "No fooling around!")
           (not (=? "fool" "foo"))
           #"fooling"))

(deftest regex-test
  (is (sut/expect #"foo" "It's foobar!"))
  ;; TODO: fails because regexes never compare equal to themselves!
  #_(is-not' (sut/expect #"fool" "It's foobar!") (not (re-find #"fool" "It's foobar!"))))

#?(:clj (deftest exception-test
         (passes (sut/expect ArithmeticException (/ 12 0))
                 (fn [ex]
                   (let [t (Throwable->map ex)]
                     (and (= "Divide by zero" (-> t :cause))
                          (or (= 'java.lang.ArithmeticException (-> t :via first :type))
                              (= java.lang.ArithmeticException (-> t :via first :type))))))))

   :cljs (deftest cljs-exception-test
           (passes (sut/expect js/Error (throw (ex-info "foo" {})))
                   (fn [ex]
                     (let [t (cljs.repl/Error->map ex)]
                       (and (= "foo"
                               (-> t :cause)) (or (= 'ExceptionInfo
                                                   (->> t :via first :type))
                                               (= ExceptionInfo
                                                  (->> t :via first :type)))))))))

#?(:clj (deftest class-test
         (is (sut/expect String (name :foo)))
         (is-not (sut/expect String :foo) clojure.lang.Keyword)))

#?(:cljs (deftest class-test
           (is (sut/expect cljs.core/List '(a b c)))
           (is-not (sut/expect cljs.core/List :foo) cljs.core/Keyword)))

#?(:clj (try
         (eval '(do
                  (require '[clojure.spec.alpha :as s])
                  (s/def :small/value (s/and pos-int? #(< % 100)))
                  (deftest spec-test
                    (is (sut/expect :small/value (* 13 4)))
                    (is-not' (sut/expect :small/value (* 13 40)) (not (=? :small/value 520))))))
         (catch Throwable _
           (println "\nOmitting Spec tests for Clojure" (clojure-version)))))

#?(:cljs (deftest spec-test
           (is (sut/expect :expectations.clojure.test-spec/small-value
                           (* 13 4)))
           (is-not'
             (sut/expect :expectations.clojure.test-spec/small-value (* 13 40))
             (not (=? :expectations.clojure.test-spec/small-value 520)))))

(deftest collection-test
  (is (sut/expect {:foo 1} (in {:foo 1 :cat 4})))
  (is (sut/expect #{1 2} (in #{0 1 2 3})))
  ;; TODO: need better tests here
  (is (nil? (sut/expect :foo (in #{:foo :bar}))))
  (is (nil? (sut/expect :foo (in [:bar :foo])))))

(deftest ^:negative not-collection-test
  (is-not' (sut/expect {:foo 1} (in {:foo 2 :cat 4})) (not (=? {:foo 1} {:foo 2}))))

;; TODO: need better tests here
(deftest grouping-more-more-of-from-each
  (sut/expecting "numeric behavior"
                 (sut/expect (more-of {:keys [a b]}
                                      even? a
                                      odd?  b)
                             {:a (* 2 13) :b (* 3 13)})
                 (sut/expect pos? (* -3 -5)))
  (sut/expecting "string behavior"
                 (sut/expect (more #"foo" "foobar" #(str/starts-with? % "f"))
                             (str "f" "oobar"))
                 (sut/expect #"foo"
                             (from-each [s ["l" "d" "bar"]]
                                        (str "foo" s)))))

(deftest more-evals-once
  (let [counter (atom 1)]
    ;; issue 24: more should only evaluate actual expression once:
    (sut/expect (more even? even? even? pos?)
                (swap! counter inc))
    (is (= 2 @counter)))
  (let [counter (atom 1)]
    ;; issue 24: more-> should only evaluate actual expression once:
    (sut/expect (more-> even? identity even? identity even? identity pos? identity)
                (swap! counter inc))
    (is (= 2 @counter))))

(defn- dummy1 [x] (throw (ex-info "called dummy1" {:x x})))
(defn- dummy2 [x] (throw (ex-info "called dummy2" {:x x})))

(deftest side-effect-testing
  (testing "No side effects"
    (is (= [] (sut/side-effects [dummy1 [dummy2 42]] (+ 1 1)))))
  (testing "Basic side effects"
    (is (= [[2]]
           (sut/side-effects [dummy1 [dummy2 42]] (dummy1 (+ 1 1)))))
    (is (= [[2]]
           (sut/side-effects [dummy1 [dummy2 42]] (dummy2 (+ 1 1))))))
  (testing "Mocked return values"
    (is (= [[2] [42]]
           (sut/side-effects [dummy1 [dummy2 42]] (dummy1 (dummy2 (+ 1 1))))))
    (is (= [[2] [nil]]
           (sut/side-effects [dummy1 [dummy2 42]] (dummy2 (dummy1 (+ 1 1))))))))

(def d-t-counter (atom 0))

; There is no cljs.test/with-test
#?(:clj (sut/with-test
         (defn definition-test
           "Make sure expectations work with clojure.test/with-test."
           [a b c]
           (swap! d-t-counter inc)
           (* a b c))
         (println "\nRunning inline tests")
         (reset! d-t-counter 0)
         (is (= 0 @d-t-counter))
         (sut/expect 1 (definition-test 1 1 1))
         (sut/expect 6 (definition-test 1 2 3))
         (is (= 2 @d-t-counter))))

;; these would be failing tests in 1.x but not in 2.x:
(sut/defexpect deftest-equivalence-0)
(sut/defexpect deftest-equivalence-1 nil)

(def ^:private control (atom 0))
;; this will succeed on its own
(sut/defexpect control-test-1 zero? @control)
;; then retest with a different control value
(deftest control-test-2
  (try
    (reset! control 1)
    (is-not' (control-test-1) (not (zero? 1)))
    (finally
      (reset! control 0))))

; Unit test for string compare routines

(deftest string-test
  (is (= "abc" (sut/str-match "abcdef" "abcefg")))
  (is (= ["def" "efg" "abc"] (sut/str-diff "abcdef" "abcefg")))
  (is (= "matches: \"abc\"\n>>>  expected diverges: \"def\"\n>>>    actual diverges: \"efg\""
         (sut/str-msg "abcdef" "abcefg" "abc"))))

; Test use of string compare routines as well as actual form in message
; on failure.  Tests are similar, but cljs one can be run with
; humane-test-output while clj one cannot.

#?(:clj (deftest ^:negative string-compare-failure-test
          (is-not' (sut/expect "abcdef" (str "abc" "efg"))
                   (not (=? "abcdef" "abcefg"))
                   #"(?is)(str \"abc\" \"efg\").*matches: \"abc\""))
   :cljs (deftest string-compare-failure-test
           (is-not' (sut/expect "abcdef" (str "abc" "efg"))
                    ["abcefg"]
                    #"(?is)(str \"abc\" \"efg\").*matches: \"abc\"")))

(deftest issue-19-regex-test
  (is (sut/expect (re-pattern "\\d+") "1000")))
