(ns integration.kur.blog-test.spbt
  (:require
   [babashka.fs :as fs]
   [clojure.spec.alpha :as s]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.generators :as g]
   [clojure.test.check.properties
    :refer [for-all] :rename {for-all defp}]
   [kur.blog-test.spbt.data-generator :as dgen]
   [kur.blog-test.spbt.operation-generator :as ogen]
   [kur.blog-test.spbt.property :as prop]
   [kur.blog-test.spbt.runner :as run]
   [kur.blog.obsidian.tag :as tag]
   [kur.blog.updater :as updater]
   [kur.util.file-system :as uf]))

(defn teardown-and-return [ret & dirs]
  (run! uf/delete-all-except-gitkeep dirs)
  ret)

(defspec test-without-timing
  {:num-tests 100
   ;:seed 1674007981491
   ;:max-size 60
   }
  (let [md-dir "test/fixture/spbt/md"
        html-dir "test/fixture/spbt/html"
        tags-html-path (str (fs/path html-dir "tags.html"))]
    (defp [[tag-set md-files ops]
           (g/let [tag-set (g/set (s/gen ::tag/tag) {:min-elements 1})
                   md-files (g/set (dgen/gen-md-file md-dir tag-set)
                                   {:min-elements 1})
                   ops (ogen/gen-ops md-files)]
             [tag-set md-files ops])
           #_(g/return [{:path "test/fixture/spbt/md/A7001010900.+.md", :text "", :kind :create}
                        {:kind :upd-sys}])
           #_(g/return
              [{:path "test/fixture/spbt/md/A7001010900.+..md", :text "", :kind :create}
               {:path "test/fixture/spbt/md/A7001010900.+..md", :text "", :kind :create}])
           #_(g/return [{:path "test/fixture/spbt/md/A7001010900.+.md",
                         :text "---\ntags:\n- []\nxs: 0\n\n---", :kind :create}
                        {:kind :upd-sys}])]
      (def ops ops)
      (teardown-and-return nil md-dir html-dir)

      ;(reset! cnt 0) (println "") ;;;
      (loop [posts (updater/post-set md-dir), model {}, ops ops]
        (if-let [op (first ops)]
          (let [;; Update model & actual
                new-model (run/next-model op model)
                new-posts (run/next-actual op [posts md-dir html-dir])]
            ;; Check property
            (if (or (not= (:kind op) :upd-sys)
                    ;; Properties
                    (and (prop/same-num-public-pages? new-model html-dir)
                         (prop/correct-tags-page? md-dir
                                                  (slurp tags-html-path))))
              (recur new-posts new-model (rest ops))
              (teardown-and-return false md-dir html-dir))) ; fail
          (teardown-and-return true md-dir html-dir))) ; pass test 
      )))

(comment
  (test-without-timing)

  ;;
  (def html-dir "test/fixture/spbt/html")
  (def md-dir "test/fixture/spbt/md")

  (def m0 {})
  (def m1 (run/next-model {:path "test/fixture/spbt/md/A7001010900.+.md", :text "", :kind :create} m0))
  (def m2 (run/next-model {:path "test/fixture/spbt/md/|", :text "", :kind :create} m1))
  (def m3 (run/next-model {:kind :upd-sys} m2))
  #_(def m4 (next-model {:kind :upd-sys} m3))

  (def olds (updater/post-set md-dir))
  (run/next-actual {:path "test/fixture/spbt/md/A7001010900.md", :text " ", :kind :create} nil)
  (def news (updater/post-set md-dir))
  (run/next-actual {:kind :upd-sys} [olds md-dir html-dir])

  #_(def olds1 (updater/post-set md-dir))
  #_(next-actual {:path "test/fixture/spbt/md/|", :text "", :kind :create} nil)
  #_(def news1 (updater/post-set md-dir))
  #_(next-actual {:kind :upd-sys} [olds1 md-dir html-dir]))