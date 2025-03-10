(ns kur.blog.updater ;; TODO: Updater(delete also?)
  (:require [babashka.fs :as fs]
            [kur.blog.look.archive :as look-archive]
            [kur.blog.look.error :as look-error]
            [kur.blog.look.home :as look-home]
            [kur.blog.look.post :as look-post]
            [kur.blog.look.subscribe :as look-subscribe]
            [kur.blog.look.tags-and-series :as look-tns]
            [kur.blog.page.post :as post]
            [kur.blog.page.post.diff :as post-diff]
            [kur.blog.page.post.name :as name]
            [kur.blog.policy :as policy]
            [kur.util.file-system :as uf]))

(defn post-set
  "Policy: Get post files in md-dir, not recursively!"
  [md-dir]
  ;(def md-dir md-dir)
  (->> (fs/list-dir md-dir)
       (remove fs/hidden?) ;; Remove .stfolder .stversion
       (map str)
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
  (let [public-posts (apply sorted-set-by #(compare (:id %2) (:id %1))
                            (concat unchanged-posts
                                    loaded-posts-to-write))
        html-path #(str (fs/path html-dir %))
        {:keys [series tags]} (look-tns/htmls public-posts)]
    (concat
     (map (fn [post] [spit
                      (html-path (post/html-file-name post))
                      (look-post/html post public-posts)])
          (remove policy/admin-post? loaded-posts-to-write))
     [[spit (html-path "404.html") (look-error/page-404 public-posts)]
      [spit (html-path "50x.html") (look-error/page-50x public-posts)]
      [spit (html-path "home.html") (look-home/html public-posts)]
      [spit (html-path "archive.html") (look-archive/html public-posts)]
      [spit (html-path "series.html") series]
      [spit (html-path "tags.html") tags]
      [spit (html-path "subscribe.html") (look-subscribe/html)]]
     (map (fn [post]
            [fs/delete-if-exists (html-path (post/html-file-name post))])
          post-to-delete))))

(defn update! [site]
  (run! (fn [[f & args]] (apply f args)) site))

;;
(comment
  (map fs/hidden? (uf/path-seq "/www/blog-base/md"))

  (post-set "/www/blog-base/md")
  (def md-dir "test/fixture/blog-root/blog-md/")
  (def html-dir "test/fixture/blog-root/tmp-html/"))