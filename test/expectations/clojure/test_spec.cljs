;; copyright (c) 2020 sean corfield, all rights reserved

(ns expectations.clojure.test-spec
  "Need to define spec in a separate compilation unit from where it is
  referenced in cljs."
  (:require [cljs.spec.alpha :as s]))

(s/def ::small-value (s/and pos-int? #(< % 100)))

