(ns kur.blog.writer
  (:require [babashka.fs :as fs]
            [kur.blog.page.post :as post]
            [kur.blog.look.post :as look-post]
            [kur.util.file-system :as uf]))

(defn post-set [md-dir]
  (->> md-dir uf/path-seq (map post/post) set))

#_(defn site [])

;;
(comment
  (def md-dir "test/fixture/blog-root/blog-md/")
  (def html-dir "test/fixture/blog-root/tmp-html/")

  ;; only post case.. 솔직히 대부분은 post에 있어야 하는 로직인 듯
  (def site
    (map #(assoc %
                 :html-str (look-post/html nil (-> % :md-path slurp))
                 :html-path (str (fs/path html-dir (post/html-file-name %))))
         (post-set md-dir)))

  (run! #(spit (:html-path %) (:html-str %)) site))
