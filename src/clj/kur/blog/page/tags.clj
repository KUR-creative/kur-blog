(ns kur.blog.page.tags
  (:require [clojure.set :refer [union]]
            [clojure.string :as str]
            [medley.core :refer [assoc-some]]))

(defn series-info [series-tag]
  (let [[series? name no] (re-find #"^s/(.+)/(.+)$" series-tag)]
    (when series? {:name name :no no})))

;;
(defn tags [post]
  (-> post :frontmatter :tags))

(defn series
  "NOTE: Currently, only one series per post is allowed"
  [post]
  (some #(series-info %) (tags post)))

(defn has-tags? [post]
  (seq (tags post)))

;;
(defn tag:posts
  "All posts should have tags ((has-tags? %)!= nil).
   Otherwise, The post does not appear in the return map."
  [posts]
  (let [id:post (zipmap (map :id posts) posts)
        id:tag-sets (update-vals id:post #(-> % :frontmatter :tags set))
        all-tags (vec (apply union (vals id:tag-sets)))]
    (zipmap all-tags
            (map #(keep (fn [[id tag-set]]
                          (when (contains? tag-set %)
                            (id:post id)))
                        id:tag-sets)
                 all-tags))))

(defn series:posts
  "posts are sorted by series no.
   NOTE: An post can be included in multiple series"
  [tag:posts]
  (update-vals (->> tag:posts
                    (keep #(when-let [series-info (series-info (key %))]
                             (assoc series-info :posts (val %)))) #_(map (fn [p] (dissoc p :text)) (val %))
                    (group-by :name))
               #(mapcat :posts (sort-by :no compare %))))

(comment
  [(series-info "tutorial튜토리얼")
   (series-info "lng/lisp")
   (series-info "s/CSAPP독학/13.1")
   (series-info "s/CSAPP/독학/13.")
   (series-info "s/CSAPP/독학/pp/ap/13.1")
   (series-info "sa/CSAPP/131")
   (series-info "s/CSAPP/")
   (series-info "s//2")]

  (def posts [{:id 3 :frontmatter {:tags [:a :b :c]}}
              {:id 2 :frontmatter {:tags [:a :b    :0] :test :t}}
              {:id 1 :frontmatter {:tags [:a]}}
              {:id :x :frontmatter {:tags []}}
              {:id 0 :frontmatter {1 2}}])
  (filter has-tags? posts)
  (tag:posts posts)
  ;
  )