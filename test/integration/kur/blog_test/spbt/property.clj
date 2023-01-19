(ns kur.blog-test.spbt.property
  (:require [babashka.fs :as fs]
            [kur.blog.page.post :as post]
            [kur.blog.updater :as updater]
            [kur.util.file-system :as uf]))

(defn same-num-public-pages? [model html-dir]
  (when (not= (+ (count model) 2)
              (count (uf/path-seq html-dir #(= (fs/extension %) "html"))))
    (def model model)
    (def fseq (uf/path-seq html-dir #(= (fs/extension %) "html"))))
  (= (+ (count model) 2) ; 2 = tags + home
     (count (uf/path-seq html-dir #(= (fs/extension %) "html")))))

(defn correct-tags-page? [md-dir tags-html-str]
  (def md-dir md-dir)
  (def tags-html-str tags-html-str)
  (every? #(.contains tags-html-str %)
          (->> (updater/post-set md-dir)
               (filter post/public?)
               (keep :id))))
