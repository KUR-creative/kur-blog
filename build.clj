(ns build
  (:require [clojure.pprint :refer [pprint]]
            [clojure.tools.build.api :as b]))

(def release-dir "release")
(def target-dir (str release-dir "/" "updater/target"))
(def class-dir (str target-dir "/" "classes"))
(def src-dir (str target-dir "/" "src"))

(def prog-name "updater")
(def uber-file (format (str target-dir "/" "%s.jar") prog-name))
(def basis (b/create-basis {:project "deps.edn"}))

;;
(defn copy-lang-dir [lang]
  (b/copy-dir {:src-dirs [(str "src" "/" lang)]
               :target-dir (str src-dir "/" lang)}))

(defn version [_]
  {:latest-tag (b/git-process {:git-args ["describe" "--tags"]})
   :hash       (b/git-process {:git-args ["rev-parse" "HEAD"]})
   :short-hash (b/git-process {:git-args ["rev-parse" "--short" "HEAD"]})})

;; clj -T:build clean
(defn clean [_]
  (b/delete {:path target-dir}))

;; clj -T:build uber
(defn uber [_]
  (clean nil)

  (copy-lang-dir "js")
  #_(copy-lang-dir "html")
  #_(copy-lang-dir "css")
  (println "Copying File(s) success")

  (spit (str release-dir "/" "version.edn")
        (with-out-str (pprint (version nil))))
  (println "Writing metadata success")

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
  (println "Cleaning up..")

  (comment))