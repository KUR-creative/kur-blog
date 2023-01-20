(ns kur.blog.page.post.diff
  "NOTE: state(post set) is created from file system directly."
  (:require [clojure.set :as s]
            [kur.util.compare :refer [=by]]))

(defn ^:deprecated happened-assocd
  "old and new info(posts) from state sets mapped with same id"
  [old new]
  {:pre [(or old new) (not= old new)]}
  (cond (nil? new) (assoc old :happened ::delete)
        (nil? old) (assoc new :happened ::create)
        :else (assoc new :happened ::update))); (= (:id old) (:id new))

(defn ^:deprecated happened-assocds
  "Return changed posts associated happened event"
  [oldset newset]
  (let [;; Remove as-is (the intersection)
        olds (s/difference oldset newset)
        news (s/difference newset oldset)
        old-ids (mapv :id olds)
        new-ids (mapv :id news)
        ids (distinct (into old-ids new-ids))]
    (map happened-assocd
         (map (zipmap old-ids olds) ids)
         (map (zipmap new-ids news) ids))))

(defn ^:deprecated unchangeds [oldset newset]
  (s/intersection oldset newset))

(defn happened
  [old now]
  {:pre [(or old now)]}
  (cond (nil? now) ::delete
        (nil? old) ::create
        (=by #(vector (:md-path %) ; fname change don't modify mtime..
                      (:last-modified-millis %))
             old now) ::as-is ;assume id are same
        :else ::update))

(defn merge-and-assoc-happened
  "merge old posts and now posts, assoc happened.
   It takes over 'loaded content' from old-posts to now-posts
   - old-posts should be loaded(load-text post)
   - now-posts should not be loaded"
  [old-posts now-posts]
  (let [old-ids (mapv :id old-posts)
        now-ids (mapv :id now-posts)
        ids (sort (distinct (into old-ids now-ids)))
        mergeds (map #(assoc (merge %1 %2) :happened (happened %1 %2))
                     (map (zipmap old-ids old-posts) ids)
                     (map (zipmap now-ids now-posts) ids))]
    mergeds
    #_{:mergeds (map #(dissoc % :happened) mergeds)
       :id:happened (zipmap ids (map :happened mergeds))}))

(comment
  (def olds #{{:id 1}
              {:id 3 :3 3} {:id 4}
              {:id 2 :last-modified-millis 1} {:id 5 :last-modified-millis 0}})
  (def nows #{{:id 6}
              {:id      3} {:id 4}
              {:id 2 :last-modified-millis 20} {:id 5 :last-modified-millis 50}})

  (= (set (happened-assocds olds nows))
     #{{:id 1, :happened ::delete}
       {:id 6, :happened ::create}
       {:id 2, :2 -2, :happened ::update}
       {:id 5, :5 -5, :happened ::update}})

  (happened nil 1)
  (happened 1 nil)
  (happened {:last-modified-millis 1} {:last-modified-millis 1})
  (happened {:last-modified-millis 1} {:last-modified-millis 2})

  (merge-and-assoc-happened olds nows))