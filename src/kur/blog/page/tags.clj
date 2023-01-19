(ns kur.blog.page.tags
  (:require [clojure.set :refer [union]]))

(defn tags [post]
  (-> post :frontmatter :tags))

(defn has-tags? [post]
  (seq (tags post)))

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

(comment
  (def posts [{:id 3 :frontmatter {:tags [:a :b :c]}}
              {:id 2 :frontmatter {:tags [:a :b    :0] :test :t}}
              {:id 1 :frontmatter {:tags [:a]}}
              {:id :x :frontmatter {:tags []}}
              {:id 0 :frontmatter {1 2}}])
  (filter has-tags? posts)
  (tag:posts posts))