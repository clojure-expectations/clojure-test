;; copyright (c) 2019-2020 sean corfield, all rights reserved

(ns expectations.clojure.test-macros
  "Macros to support testing the testing framework."
  (:require #?(:clj [clojure.test :refer [is do-report] :as t]
               :cljs [cljs.test :refer [do-report assert-expr]
				:refer-macros [is assert-expr] :as t])
            #?(:cljs [cljs.spec.alpha :as s])
            #?(:clj [expectations.clojure.test :as sut]
               :cljs [expectations.clojure.test :include-macros true :as sut])))

(defmacro is-not'
  "Construct a negative test for an expectation with a symbolic failure."
  [expectation failure & [msg]]
  `(let [results# (atom nil)]
     (with-redefs [do-report (sut/all-report results#)]
       ~expectation)
     (t/is (some (fn [fail#]
                 (= '~failure (:actual fail#)))
               (:fail @results#)))
     (when ~msg
       (t/is (some (fn [fail#]
                   (re-find ~msg (:message fail#)))
                 (:fail @results#))))))

(defmacro is-not
  "Construct a negative test for an expectation with a value-based failure."
  [expectation failure & [msg]]
  `(let [results# (atom nil)]
     (with-redefs [do-report (sut/all-report results#)]
       ~expectation)
     (t/is (some (fn [fail#]
                 (= ~failure (:actual fail#)))
               (:fail @results#)))
     (when ~msg
       (t/is (some (fn [fail#]
                   (re-find ~msg (:message fail#)))
                 (:fail @results#))))))

(defmacro passes
  "Construct a positive test for an expectation with a predicate-based success.

  This is needed for cases where a successful test wraps a failing behavior,
  such as `thrown?`, i.e., `(expect ExceptionType actual)`"
  [expectation success]
  `(let [results# (atom nil)]
     (with-redefs [do-report (sut/all-report results#)]
       ~expectation)
     (t/is (some (fn [pass#]
                 (~success (:actual pass#)))
               (:pass @results#)))))

