;; copyright (c) 2019-2020 sean corfield, all rights reserved

(ns expectations.clojure.fixture-test
  "Test the fixtures of the testing framework."
  (:require [expectations.clojure.test :as sut]))

(def each (atom -1))
(def once (atom -1))
(def order (atom []))

;; before calls all happen in order
(sut/before-each (reset! order []))
;; expectations in before calls are valid
(sut/before-each (sut/expect [] @order))
(sut/before-each (swap! order conj :a))
;; these happen in order, but after the around calls
(sut/after-each  (swap! order conj :x))
(sut/after-each  (swap! order conj :y))
(sut/after-each  (swap! order conj :z))
;; these happen in order like before calls, but the after parts
;; are reversed (and all happen before the after calls!)
(sut/around-each #(do (swap! order conj :b) (%) (swap! order conj :w)))
(sut/around-each (swap! order conj :c) (swap! order conj :v))
;; expectations in after calls are valid
(sut/after-each  (sut/expect [:a :b :c :v :w :x :y :z] @order))
(sut/around-each (reset! each 0) (sut/expect 1 @each))
(sut/around-once (reset! once 0) (sut/expect (= 2 @once)))

(sut/defexpect each-inc-1
  (do
    (swap! each inc)
    (swap! once inc)))

(sut/defexpect each-inc-2
  (do
    (swap! each inc)
    (swap! once inc)))
