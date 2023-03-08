(ns kur.blog.page.post.name-test
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties
             :refer [for-all] :rename {for-all defp}]
            [kur.blog.page.post.name :as name]
            [kur.blog.page.post.name.generator :as ngen]
            [kur.blog.page.post.name.spec :as nspec]))

(defspec fname-parts-roundtrip-test 1000
  (defp [parts (s/gen ::nspec/file-name-parts
                      {::nspec/id (fn [] ngen/id)
                       ::nspec/title (fn [] ngen/post-title)})]
    (let [ks (keys parts)]
      (= (select-keys parts ks)
         (select-keys (name/fname->parts (name/parts->fname parts))
                      ks)))))

(comment
  (fname-parts-roundtrip-test))