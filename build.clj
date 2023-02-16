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

;; clj -T:build clean
(defn clean [_]
  (println "Cleaning up previous target..")
  (b/delete {:path target-dir}))

;; clj -T:build copy-dirs
(defn copy-dirs [_]
  (println "Copying directories..")
  (copy-lang-dir "js")
  #_(copy-lang-dir "html")
  #_(copy-lang-dir "css")
  )

;; clj -T:build uber
(defn uber [_]
  #_(println "Writing metadata..")
  #_(spit (str release-dir "/" "version.edn")
          (with-out-str (pprint (version nil))))

  (println "Compiling..")
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})

  (println "Building uberjar..")
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'kur.blog.main})

  (println "Cleaning up..")
  (b/delete {:path class-dir}))

;; clj -T:build release # The one command
(defn release [_]
  (clean nil)
  (copy-dirs nil)
  (uber nil))
