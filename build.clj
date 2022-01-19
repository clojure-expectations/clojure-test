(ns build
  "Expections (clojure-test) build script.

  clojure -T:build run-tests
  clojure -T:build run-tests :aliases '[:master]'

  clojure -T:build ci

  For more information, run:

  clojure -A:deps -T:build help/doc"
  (:require [clojure.tools.build.api :as b]
            [org.corfield.build :as bb]))

(def lib 'com.github.seancorfield/expectations)
(defn- the-version [patch] (format "2.0.%s" patch))
(def version (the-version (b/git-count-revs nil)))
(def snapshot (the-version "999-SNAPSHOT"))

(defn run-tests "Run the tests."
  [{:keys [aliases] :as opts}]
  (-> opts
      (cond->
        (some #{:humane :cljs} aliases)
        (assoc :main-opts ["-e" ":negative"]))
      (bb/run-tests)))

(defn ci
  "Run the CI pipeline of tests (and build the JAR).

  Specify :cljs true to run the ClojureScript tests as well."
  [opts]
  (-> opts
      (assoc :lib lib :version (if (:snapshot opts) snapshot version))
      (as-> opts
            (reduce (fn [opts alias]
                      (run-tests (assoc opts :aliases [alias])))
                    opts
                    (cond-> [:1.9 :1.10 :master :humane]
                      (:cljs opts)
                      (conj :cljs))))
      (bb/clean)
      (assoc :src-pom "template/pom.xml")
      (bb/jar)))

(defn deploy "Deploy the JAR to Clojars." [opts]
  (-> opts
      (assoc :lib lib :version (if (:snapshot opts) snapshot version))
      (bb/deploy)))
