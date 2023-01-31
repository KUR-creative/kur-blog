(ns build
  (:require [clojure.tools.build.api :as b]))

(def target-dir "release/updater/target")
(def class-dir (str target-dir "/" "classes"))

(def prog-name "updater")
(def version "0.1.0")

(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format (str target-dir "/" "%s-%s-standalone.jar")
                       prog-name version))

;; clj -T:build clean
(defn clean [_]
  (b/delete {:path target-dir}))

;; clj -T:build uber
(defn uber [_]
  (clean nil)
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (println "Compilation success")
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'kur.blog.main})
  (println "Building uberjar success"))