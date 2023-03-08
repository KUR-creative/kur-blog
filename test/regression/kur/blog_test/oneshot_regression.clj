(ns regression.kur.blog-test.oneshot-regression
  "Test current updater generate the same htmls as a specific version
   Test only initialization (one shot regression)"
  (:require [babashka.fs :as fs]
            [clojure.java.shell :refer [sh]]
            [cprop.core :refer [load-config]]
            [kur.blog.main :as main]
            [kur.blog.monitor :as monitor]))

(defn test-case [git-count]
  (let [root-dir "test/fixture/regression"
        cfg-path (format "%s/%s.edn" root-dir git-count)
        expected-htmls-dir (format "%s/%s/html" root-dir git-count)]
    (assert (fs/exists? cfg-path))
    (assert (fs/exists? expected-htmls-dir))
    {:cfg-path cfg-path, :expected-htmls-dir expected-htmls-dir}))

(defn diff [git-count]
  (let [{:keys [cfg-path expected-htmls-dir]} (test-case git-count)
        {:keys [html-dir]} (load-config :file cfg-path)
        updater (main/-main cfg-path)]
    (monitor/stop! updater)
    (sh "diff" html-dir expected-htmls-dir)))

(comment
  (def git-count 156)
  (println (:out (diff 156)))
  (println (:out (diff 159))) ; Subscribe, Guests temp page were added
  (println (:out (diff 178))) ; Change public-posts data structure
  (println (:out (diff 187))) ; Refactor look-post
  )