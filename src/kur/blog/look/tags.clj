(ns kur.blog.look.tags
  (:require [hiccup.element :refer [link-to]]
            [hiccup.page :refer [html5]]
            [kur.blog.look.template :refer [head]]
            [kur.blog.page.post :as post]))

(defn post-link-li [post]
  [:li (link-to (-> post :id post/url) (post/title-or-id post))])

(defn tag-and-links-block [tag posts]
  (list [:h3 tag] [:ul (map post-link-li posts)]))

(defn tags-summary [tag:posts no-tags-posts]
  (let [sort-by-title #(sort-by post/title-or-id %)
        tag:posts (into (sorted-map) (update-vals tag:posts sort-by-title))
        no-tags-posts (sort-by-title no-tags-posts)]
    (cons (mapcat (fn [[tag posts]]
                    (tag-and-links-block (str "#" tag) posts))
                  tag:posts)
          (tag-and-links-block "No tag" no-tags-posts))))

; TODO: Add js to sort tags
(defn html [tag:posts no-tags-posts
            & {:keys [css-paths js-paths] :as opts}]
  (html5 (head :css-paths css-paths)
         [:body [:h1 "tags" (tags-summary tag:posts no-tags-posts)]]))