(ns kur.blog.writer ;; TODO: Updater(delete also?)
  (:require [babashka.fs :as fs]
            [kur.blog.look.post :as look-post]
            [kur.blog.page.post :as post]
            [kur.blog.look.tags :as look-tags]
            [kur.blog.page.tags :as tags]
            [kur.blog.look.home :as look-home]
            [kur.util.file-system :as uf]))

#_(defn post-set [md-dir]
    (->> md-dir uf/path-seq (map post/post) set))

(defn site
  "Return ([html-path page-html] ...). posts shuold be vector."
  [posts html-dir]
  (letfn [(html-path [fname] (str (fs/path html-dir fname)))]
    (map vector
         (conj (mapv #(html-path (post/html-file-name %)) posts)
               (html-path "tags.html")
               (html-path "home.html"))
         (conj (mapv #(look-post/html nil (:text %)) posts)
               (look-tags/html (tags/tag:posts posts)
                               (filter #(not (tags/has-tags? %)) posts))
               (look-home/html (sort-by :id ; id means creation time
                                        posts))))))

(defn write! [site]
  (run! (fn [[path html]] (spit path html)) site))

;;
(comment
  (def md-dir "test/fixture/blog-root/blog-md/")
  (def html-dir "test/fixture/blog-root/tmp-html/")

  (def a-site (site (->> md-dir uf/path-seq (map post/post)) html-dir))
  (write! a-site))