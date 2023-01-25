(ns kur.blog.page.post.name-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties
             :refer [for-all] :rename {for-all defp}]
            [kur.blog.page.post.name :as name]))

(defspec fname-parts-roundtrip-test 1000
  (defp [parts (s/gen ::name/file-name-parts)]
    (let [ks (keys parts)]
      (= (select-keys parts ks)
         (select-keys (name/fname->parts (name/parts->fname parts))
                      ks)))))

(comment
  (fname-parts-roundtrip-test))