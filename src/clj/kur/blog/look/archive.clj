(ns kur.blog.look.archive
  (:require [hiccup.page :refer [html5]]
            [kur.blog.look.post :as look-post]
            [kur.blog.look.template :refer [head header]]
            [kur.blog.policy :as policy]))

(defn post-list
  [posts]
  (let [lis (mapv look-post/post-link-li posts)]
    (when (seq lis) (assert (apply distinct? lis)))
    (into [:ul] lis)))

(defn html
  "posts is seq of Post"
  [posts & {:keys [css-paths]}]
  ;(def posts posts)
  (html5 (head :css-paths policy/common-css-paths
               :title "KUR Creative Blog - Archive") ;; TODO: SEO?
         [:body
          header
          [:article {:class "container"}
           [:h1 "Archive"]
           (post-list (remove policy/admin-post? posts))]]))