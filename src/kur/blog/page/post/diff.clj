(ns kur.blog.page.post.diff
  "NOTE: state(post set) is created from file system directly."
  (:require [kur.util.compare :refer [=by]]))

(defn happened
  [old now]
  {:pre [(or old now)]}
  (cond (nil? now) ::delete
        (nil? old) ::create
        (=by #(select-keys % [:md-path ; fname change don't modify mtime..
                              :last-modified-millis])
             old now) ::as-is ;assume id are same
        :else ::update))

(defn merge-and-assoc-happened
  "merge old posts and now posts, assoc happened.
   Merging takes over 'loaded content' from old-posts to now-posts
   - old-posts should be loaded (load-text post)
   - now-posts should not be loaded"
  [old-posts now-posts]
  (let [old-ids (mapv :id old-posts), now-ids (mapv :id now-posts)
        ids (distinct (into old-ids now-ids))]
    (map #(assoc (merge %1 %2) :happened (happened %1 %2))
         (map (zipmap old-ids old-posts) ids)
         (map (zipmap now-ids now-posts) ids))))

(comment
  (def olds #{{:id 1}
              {:id 3 :3 3} {:id 4}
              {:id 2 :last-modified-millis 1} {:id 5 :last-modified-millis 0}})
  (def nows #{{:id 6}
              {:id      3} {:id 4}
              {:id 2 :last-modified-millis 20} {:id 5 :last-modified-millis 50}})

  (happened nil 1)
  (happened 1 nil)
  (happened {:last-modified-millis 1} {:last-modified-millis 1})
  (happened {:last-modified-millis 1} {:last-modified-millis 2})

  (merge-and-assoc-happened olds nows))