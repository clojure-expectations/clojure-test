(ns build
  "Expections (clojure-test) build script.

  clojure -T:build run-tests
  clojure -T:build run-tests :aliases '[:master]'

  clojure -T:build ci

  For more information, run:

  clojure -A:deps -T:build help/doc"
  (:require [clojure.tools.build.api :as b]
            [clojure.tools.deps.alpha :as t]
            [deps-deploy.deps-deploy :as dd]))

(def lib 'com.github.seancorfield/expectations)
;; we're not gold yet!
(def version "2.0.0-alpha3" #_(format "2.0.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(defn clean "Remove the target folder." [_]
  (println "\nCleaning target...")
  (b/delete {:path "target"}))

(defn jar "Build the library JAR file." [_]
  (println "\nWriting pom.xml...")
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :scm {:tag (str "v" version)}
                :basis basis
                :src-dirs ["src"]})
  (println "Copying src...")
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (println (str "Building jar " jar-file "..."))
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))

(defn- run-task
  [aliases]
  (println "\nRunning task for:" aliases)
  (let [basis    (b/create-basis {:aliases aliases})
        combined (t/combine-aliases basis aliases)
        cmds     (b/java-command {:basis     basis
                                  :java-opts (:jvm-opts combined)
                                  :main      'clojure.main
                                  :main-args (cond-> (:main-opts combined)
                                               (some #{:humane :cljs} aliases)
                                               (conj "-e" ":negative"))})
        {:keys [exit]} (b/process cmds)]
    (when-not (zero? exit)
      (throw (ex-info (str "Task failed for: " aliases) {})))))

(defn run-tests
  "Run regular tests.

  Optionally specify :aliases:
  [:1.9] -- test against Clojure 1.9 (the default)
  [:1.10] -- test against Clojure 1.10.3
  [:humane] -- test with HTO in the mix (and exclude :negative tests)
  [:master] -- test against Clojure 1.11 master snapshot
  [:cljs] -- test against ClojureScript"
  [{:keys [aliases] :as opts}]
  (run-task (into [:test] aliases))
  opts)

(defn ci
  "Run the CI pipeline of tests (and build the JAR).

  Specify :cljs true to run the ClojureScript tests as well."
  [opts]
  (-> opts
      (as-> opts
            (reduce (fn [opts alias]
                      (run-tests (assoc opts :aliases (cond-> [alias]
                                                        (not= :cljs alias)
                                                        (conj :runner)))))
                    opts
                    (cond-> [:1.9 :1.10 :master :humane]
                      (:cljs opts)
                      (conj :cljs))))
      (clean)
      (jar)))
