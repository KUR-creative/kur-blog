(ns build
  (:require [clojure.tools.build.api :as b]))

(def target-dir "release/updater/target")
(def class-dir (str target-dir "/" "classes"))
(def src-dir (str target-dir "/" "src"))

(def prog-name "updater")
(def version "0.1.0")

(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format (str target-dir "/" "%s-%s-standalone.jar")
                       prog-name version))

;; clj -T:build clean
(defn clean [_]
  (b/delete {:path target-dir}))

(defn copy-lang-dir [lang]
  (b/copy-dir {:src-dirs [(str "src" "/" lang)]
               :target-dir (str src-dir "/" lang)}))

;; clj -T:build uber
(defn uber [_]
  (clean nil)
  (copy-lang-dir "js")
  #_(copy-lang-dir "html")
  #_(copy-lang-dir "css")
  (println "Copying File(s) success")

  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (println "Compilation success")

  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'kur.blog.main})
  (println "Building uberjar success")

  (b/delete {:path class-dir})
  (println "Cleaning up.."))