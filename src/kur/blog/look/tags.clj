(ns kur.blog.look.tags
  (:require [hiccup.element :refer [link-to]]
            [hiccup.page :refer [html5]]
            [kur.blog.look.template :refer [head]]))

(defn post-url [post-id] ; TODO: refactor
  (str "http://" "localhost"
       ":" 3000
       "/" post-id))

(defn post-link-li
  [{:keys [id title]}]
  [:li (link-to (post-url id) (if title title id))])

(defn tags-summary [tag:posts no-tags-posts]
  (let [sort-by-title #(sort-by :title %)
        tag:posts (into (sorted-map) (update-vals tag:posts sort-by-title))
        no-tags-posts (sort-by-title no-tags-posts)]
    (list (mapcat (fn [[tag posts]]
                    (list [:h3 (str "#" tag)]
                          [:ul (map post-link-li posts)]))
                  tag:posts)
          [:h3 "No tag"]
          [:ul (map post-link-li no-tags-posts)])))

; TODO: Add js to sort tags
(defn html [tag:posts no-tags-posts
            & {:keys [css-paths js-paths] :as opts}]
  (html5 (head :css-paths css-paths)
         [:body [:h1 "tags" (tags-summary tag:posts no-tags-posts)]]))