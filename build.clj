(ns build
  "Expections (clojure-test) build script.

   Run individual round of tests:

   clojure -T:build run-tests
   clojure -T:build run-tests :aliases '[:master]'

   Run multi-version tests:

   clojure -T:build test

   Also run cljs tests:

   clojure -T:build test :cljs true

   Run the CI pipeline (to build a JAR):

   clojure -T:build ci

   For more information, run:

   clojure -A:deps -T:build help/doc"
  (:refer-clojure :exclude [test])
  (:require [clojure.string :as str]
            [clojure.tools.build.api :as b]
            [clojure.tools.deps :as t]
            [deps-deploy.deps-deploy :as dd]))

(def lib 'com.github.seancorfield/expectations)
(defn- the-version [patch] (format "2.0.%s" patch))
(def version (the-version (b/git-count-revs nil)))
(def snapshot (the-version "999-SNAPSHOT"))
(def class-dir "target/classes")

(defn run-tests "Run the tests."
  [{:keys [aliases] :as opts}]
  (println "\nRunning tests for" (str/join ", " (map name aliases)) "...")
  (let [basis    (b/create-basis {:aliases (into [:test] aliases)})
        combined (t/combine-aliases basis (into [:test] aliases))
        cmds     (b/java-command
                  {:basis     basis
                   :main      'clojure.main
                   :main-args (cond-> (:main-args combined)
                                (some #{:humane :cljs} aliases)
                                (into ["-e" ":negative"]))})
        {:keys [exit]} (b/process cmds)]
    (when-not (zero? exit) (throw (ex-info "Tests failed" {}))))
  opts)

(defn test "Run all the tests." [opts]
  (reduce (fn [opts alias]
            (run-tests (assoc opts :aliases [alias])))
          opts
          (cond-> [:1.9 :1.10 :1.11 :master :humane]
            (:cljs opts)
            (conj :cljs)))
  opts)

(defn- jar-opts [opts]
  (let [version (if (:snapshot opts) snapshot version)]
    (assoc opts
           :lib lib :version version
           :jar-file (format "target/%s-%s.jar" lib version)
           :scm {:tag (str "v" version)}
           :basis (b/create-basis {})
           :class-dir class-dir
           :target "target"
           :src-dirs ["src"]
           :src-pom "template/pom.xml")))

(defn ci "Run the CI pipeline of tests (and build the JAR)." [opts]
  (test opts)
  (b/delete {:path "target"})
  (let [opts (jar-opts opts)]
    (println "\nWriting pom.xml...")
    (b/write-pom opts)
    (println "\nCopying source...")
    (b/copy-dir {:src-dirs ["resources" "src"] :target-dir class-dir})
    (println "\nBuilding JAR" (:jar-file opts) "...")
    (b/jar opts))
  opts)

(defn deploy "Deploy the JAR to Clojars." [opts]
  (let [{:keys [jar-file] :as opts} (jar-opts opts)]
    (dd/deploy {:installer :remote :artifact (b/resolve-path jar-file)
                :pom-file (b/pom-path (select-keys opts [:lib :class-dir]))}))
  opts)
