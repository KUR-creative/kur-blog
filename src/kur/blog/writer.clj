(ns kur.blog.writer
  (:require [babashka.fs :as fs]
            [kur.blog.look.post :as look-post]
            [kur.blog.page.post :as post]
            [kur.blog.look.tags :as look-tags]
            [kur.blog.page.tags :as tags]
            [kur.blog.look.home :as look-home]
            [kur.util.file-system :as uf]))

(defn post-set [md-dir]
  (->> md-dir uf/path-seq (map post/post) set))

#_(defn site [])

;;
(comment
  (def md-dir "test/fixture/blog-root/blog-md/")
  (def html-dir "test/fixture/blog-root/tmp-html/")

  ;; post 출력
  (def posts (post-set md-dir))
  (def post-htmls
    (map #(look-post/html nil (:text %)) posts))
  (def post-html-paths
    (map #(str (fs/path html-dir (post/html-file-name %))) posts))

  (run! (fn [[path html]] (spit path html))
        (map vector post-html-paths post-htmls))

  ;; tags 출력
  (spit (str (fs/path html-dir "tags.html"))
        (look-tags/html (tags/tag:posts posts)
                        (filter #(not (tags/has-tags? %)) posts)))

  ;; home 출력
  (spit (str (fs/path html-dir "home.html"))
        (look-home/html (sort-by :id posts))) ; id means creation time
  )
