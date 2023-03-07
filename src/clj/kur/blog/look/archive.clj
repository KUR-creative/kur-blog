(ns kur.blog.look.archive
  (:require [hiccup.page :refer [html5]]
            [kur.blog.look.post :as look-post]
            [kur.blog.look.template :refer [article-page]]
            [kur.blog.policy :as policy]))

(defn post-list
  [posts]
  (let [lis (mapv #(look-post/post-link-li % :fragment-id (:id %)) posts)]
    (when (seq lis) (assert (apply distinct? lis)))
    (into [:ul] lis)))

(defn html
  [posts]
  (html5 (article-page
          {:title "KUR Creative Blog - Archive"}; TODO: SEO?
          {:h1 "Archive"
           :content (post-list (remove policy/admin-post? posts))})))