(ns kur.blog-test.scenario
  (:require [babashka.fs :as fs]
            [clojure.test :refer [deftest testing is run-tests]]
            [cprop.core :refer [load-config]]
            [kur.blog.main :as main]
            [kur.blog.monitor :as monitor]
            [kur.util.file-system :as uf]))

(deftest change-post
  (let [config-path "test/fixture/config/scenario.edn"
        {:keys [md-dir html-dir fs-wait-ms]
         :as config} (load-config :file config-path)

        post-id "kur1234567890"
        md-path     (str (fs/path md-dir (str post-id ".+.pub.md")))
        new-md-path (str (fs/path md-dir (str post-id ".pub.md")))
        md-text "test"

        monitor (main/-main config-path)
        wait-ms (* 2 fs-wait-ms)]
    (uf/delete-all-except-gitkeep md-dir)
    (uf/delete-all-except-gitkeep html-dir)
    (testing "Change + meta to nil meta string then delete post html"
      (spit md-path md-text)
      (Thread/sleep wait-ms)
      (is (fs/exists? (fs/path html-dir (str post-id ".html"))))

      (fs/move md-path new-md-path)
      (Thread/sleep wait-ms)
      (is (not (fs/exists? (fs/path html-dir (str post-id ".html"))))))
    (monitor/stop! monitor)
    (uf/delete-all-except-gitkeep md-dir)
    (uf/delete-all-except-gitkeep html-dir)))

(comment
  (run-tests))