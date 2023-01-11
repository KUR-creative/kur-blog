(ns kur.blog.look.home
  (:require [hiccup.element :refer [link-to]]
            [hiccup.page :refer [html5]]
            [kur.blog.look.template :refer [head]]
            [kur.blog.look.post :as look-post]))

(defn post-list
  [posts]
  (into [:ul] (map look-post/post-link-li posts)))

(defn html
  "posts is seq of Post Publishable"
  [posts & {:keys [css-paths]}]
  (html5 (head :css-paths css-paths)
         [:body
          [:h1 "HOME"]
          ;; TODO: Add tags. how?
          (post-list posts)]))