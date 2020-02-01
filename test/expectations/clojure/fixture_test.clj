;; copyright (c) 2019-2020 sean corfield, all rights reserved

(ns expectations.clojure.fixture-test
  "Test the fixtures of the testing framework."
  (:require [expectations.clojure.test :as sut]))

(def each (atom -1))
(def once (atom -1))

(sut/around-each (reset! each 0) (assert (= 1 @each)))
(sut/around-once (reset! once 0) (assert (= 2 @once)))

(sut/defexpect each-inc-1
  (do
    (swap! each inc)
    (swap! once inc)))

(sut/defexpect each-inc-2
  (do
    (swap! each inc)
    (swap! once inc)))
