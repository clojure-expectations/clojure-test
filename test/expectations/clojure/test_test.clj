;; copyright (c) 2019 sean corfield, all rights reserved

(ns expectations.clojure.test-test
  "Test the testing framework -- this is sometimes harder than you might think!"
  (:require [clojure.test :refer [deftest is do-report]]
            [expectations.clojure.test :as sut]))

;; TODO: need tests for (defexpect test-name expected actual)

(defmacro is-not'
  "Construct a negative test for an expectation with a symbolic failure."
  [expectation failure]
  `(let [results# (atom nil)]
     (with-redefs [do-report (sut/all-report results#)]
       ~expectation)
     (is (some (fn [fail#]
                 (= '~failure (:actual fail#)))
               (:fail @results#)))))

(defmacro is-not
  "Construct a negative test for an expectation with a value-based failure."
  [expectation failure]
  `(let [results# (atom nil)]
     (with-redefs [do-report (sut/all-report results#)]
       ~expectation)
     (is (some (fn [fail#]
                 (= ~failure (:actual fail#)))
               (:fail @results#)))))

(defmacro passes
  "Construct a positive test for an expectation with a predicate-based success.

  This is needed for cases where a successful test wraps a failing behavior,
  such as `thrown?`, i.e., `(expect ExceptionType actual)`"
  [expectation success]
  `(let [results# (atom nil)]
     (with-redefs [do-report (sut/all-report results#)]
       ~expectation)
     (is (some (fn [pass#]
                 (~success (:actual pass#)))
               (:pass @results#)))))

(deftest predicate-test
  (is (sut/expect even? (+ 1 1)))
  (is (sut/expect empty? (list)))
  (is-not' (sut/expect even? (+ 1 1 1)) (not (even? 3)))
  (is-not' (sut/expect empty? [1]) (not (empty? [1]))))

(deftest equality-test
  (is (sut/expect 1 (* 1 1)))
  (is (sut/expect "foo" (str "f" "oo")))
  (is-not' (sut/expect 2 (* 1 1)) (not (=? 2 1)))
  (is-not' (sut/expect "fool" (str "f" "oo")) (not (=? "fool" "foo"))))

(deftest regex-test
  (is (sut/expect #"foo" "It's foobar!"))
  ;; TODO: fails because regexes never compare equal to themselves!
  #_(is-not' (sut/expect #"fool" "It's foobar!") (not (re-find #"fool" "It's foobar!"))))

(deftest exception-test
  (passes (sut/expect ArithmeticException (/ 12 0))
          (fn [ex]
            (let [t (Throwable->map ex)]
              (and (= "Divide by zero" (-> t :cause))
                   (or (= 'java.lang.ArithmeticException (-> t :via first :type))
                       (= java.lang.ArithmeticException (-> t :via first :type))))))))

(deftest class-test
  (is (sut/expect String (name :foo)))
  (is-not (sut/expect String :foo) clojure.lang.Keyword))

(try
  (eval '(do
           (require '[clojure.spec.alpha :as s])
           (s/def :small/value (s/and pos-int? #(< % 100)))
           (deftest spec-test
             (is (sut/expect :small/value (* 13 4)))
             (is-not' (sut/expect :small/value (* 13 40)) (not (=? :small/value 520))))))
  (catch Throwable _
    (println "\nOmitting Spec tests for Clojure" (clojure-version))))

(deftest collection-test
  (is (sut/expect {:foo 1} (in {:foo 1 :cat 4})))
  ;; TODO: need better tests here
  (is (nil? (sut/expect :foo (in #{:foo :bar}))))
  (is (nil? (sut/expect :foo (in [:bar :foo]))))
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
                 (sut/expect (more #"foo" "foobar" #(clojure.string/starts-with? % "f"))
                             (str "f" "oobar"))
                 (sut/expect #"foo"
                             (from-each [s ["l" "d" "bar"]]
                                        (str "foo" s)))))
