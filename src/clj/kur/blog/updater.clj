(ns kur.blog.updater ;; TODO: Updater(delete also?)
  (:require [babashka.fs :as fs]
            [kur.blog.look.home :as look-home]
            [kur.blog.look.post :as look-post]
            [kur.blog.look.tags :as look-tags]
            [kur.blog.page.post :as post]
            [kur.blog.page.post.diff :as post-diff]
            [kur.blog.page.post.name :as name]
            [kur.blog.page.tags :as tags]
            [kur.util.file-system :as uf]))

(defn post-set [md-dir]
  (->> (uf/path-seq md-dir)
       (keep #(when-let [parts (name/valid-parts %)]
                [% parts]))
       (map (fn [[path parts]] (post/post path parts)))
       set))

(defn classify-posts
  "Return post groups classified by file system change(delete, write, as-is)
   NOTE: unchanged(post)s inherit loaded text from old-posts."
  [old-posts now-posts]
  ;(def old-posts old-posts)
  ;(def now-posts now-posts)
  (let [mergeds
        (vec (post-diff/merge-and-assoc-happened old-posts now-posts))

        {unchangeds true changeds false}
        (group-by #(= ::post-diff/as-is (:happened %)) mergeds)

        {to-deletes ::post/delete!, to-writes ::post/write!}
        (group-by post/how-update-html changeds)

        map-rm-hap (fn [posts] (map #(dissoc % :happened) posts))]
    {:unchangeds (map-rm-hap unchangeds)
     :to-deletes (map-rm-hap to-deletes)
     :to-writes (map-rm-hap to-writes)}))

(defn site
  "Return commands to maintain html files of site
   commands are [[f & args]*]"
  [unchanged-posts post-to-delete loaded-posts-to-write html-dir]
  (let [unchangeds-and-writes (concat unchanged-posts loaded-posts-to-write)
        html-path #(str (fs/path html-dir %))]
    (concat
     (map (fn [post] [spit (html-path (post/html-file-name post))
                      (look-post/html nil (:title post) (:text post))])
          loaded-posts-to-write)
     [[spit (html-path "tags.html")
       (look-tags/html (tags/tag:posts unchangeds-and-writes)
                       (filter #(not (tags/has-tags? %))
                               unchangeds-and-writes))]
      [spit (html-path "home.html")
       (look-home/html (sort-by :id unchangeds-and-writes))]]
     (map (fn [post]
            [fs/delete-if-exists (html-path (post/html-file-name post))])
          post-to-delete))))

(defn update! [site]
  (run! (fn [[f & args]] (apply f args)) site))

;;
(comment
  (def md-dir "test/fixture/blog-root/blog-md/")
  (def html-dir "test/fixture/blog-root/tmp-html/"))