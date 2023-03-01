(ns kur.blog.look.tags
  (:require [hiccup.page :refer [html5]]
            [kur.blog.look.post :as look-post]
            [kur.blog.look.template :refer [article-page heading-link]]
            [kur.blog.page.post :as post]
            [kur.blog.page.tags :as tags]
            [kur.blog.policy :as policy]
            [medley.core :refer [filter-keys]]))

(defn tag-and-links-block [tag posts]
  (list [:h3 {:id tag :style "margin-top: 0.5em;"} (heading-link tag)]
        [:ul (map look-post/post-link-li posts)]))

(defn tags-summary [tag:posts no-tags-posts]
  ;(def tag:posts tag:posts)
  ;(def no-tags-posts no-tags-posts)
  (let [sort-by-title #(sort-by post/title-or-id %)
        no-tags-posts (sort-by-title no-tags-posts)]
    (cons (mapcat (fn [[tag posts]]
                    (tag-and-links-block tag posts))
                  (into (sorted-map)
                        (-> tag:posts
                            (update-keys str)
                            (update-vals sort-by-title))))
          (when (seq no-tags-posts)
            (tag-and-links-block "No tag" no-tags-posts)))))

(defn series-summary [series:posts]
  (->> series:posts
       (sort-by key)
       (map (fn [[series posts]]
              (tag-and-links-block series posts)))))

; TODO: Add js to sort tags
(defn html [posts]
  (let [posts (remove policy/admin-post? posts)
        tag:posts (tags/tag:posts posts)
        no-tags-posts (remove tags/has-tags? posts)]
    ;(def tag:posts tag:posts)
    (html5 (article-page
            {:title "KUR Creative Blog - series & tags"}
            {:content
             (list
              [:h1 {:id "Series" :style "margin-bottom: 0.1em;"}
               (heading-link "Series")]
              (series-summary (tags/series:posts tag:posts))
              [:h1 {:id "Tags" :style "margin-bottom: 0.1em;"}
               (heading-link "Tags")]
              (tags-summary (filter-keys #(not (tags/series-info %))
                                         tag:posts)
                            no-tags-posts))}))))

#_(tags/series:posts tag:posts)