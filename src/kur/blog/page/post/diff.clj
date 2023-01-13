(ns kur.blog.page.post.diff
  (:require [clojure.set :as s]))

(defn happened-assocd
  "old and new info(posts) from state sets mapped with same id"
  [old new]
  {:pre [(or old new) (not= old new)]}
  (cond (nil? new) (assoc old :happened ::delete)
        (nil? old) (assoc new :happened ::create)
        :else (assoc new :happened ::update))); (= (:id old) (:id new))

(defn happened-assocds
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

(comment
  (def olds #{{:id 1}     {:id 3 :3 3} {:id 4}  {:id 2 :2  2} {:id 5}})
  (def news     #{{:id 6} {:id 3 :3 3} {:id 4}  {:id 2 :2 -2} {:id 5 :5 -5}})

  (= (set (happened-assocds olds news))
     #{{:id 1, :happened ::delete}
       {:id 6, :happened ::create}
       {:id 2, :2 -2, :happened ::update}
       {:id 5, :5 -5, :happened ::update}}))