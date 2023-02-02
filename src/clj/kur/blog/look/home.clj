(ns kur.blog.look.home
  (:require [hiccup.element :refer [link-to]]
            [hiccup.page :refer [html5]]
            [kur.blog.look.post :as look-post]
            [kur.blog.look.template :refer [head]]))

(defn post-list
  [posts]
  (let [lis (mapv look-post/post-link-li posts)]
    (when (seq lis) (assert (apply distinct? lis)))
    (into [:ul] lis)))

(defn html
  "posts is seq of Post Publishable"
  [posts & {:keys [css-paths]}]
  (html5 (head :css-paths css-paths)
         [:body
          [:h1 "HOME"]
          ;; TODO: Add tags. how?
          (link-to "tags" [:p "tags"])
          (post-list posts)]))