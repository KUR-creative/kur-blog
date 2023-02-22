(ns kur.blog.look.home
  (:require [hiccup.page :refer [html5]]
            [kur.blog.look.template :refer [head header]]
            [kur.blog.page.post.md2x :refer [obsidian-html]]
            [kur.blog.policy :as policy]))

#_(defn post-list
    [posts]
    (let [lis (mapv look-post/post-link-li posts)]
      (when (seq lis) (assert (apply distinct? lis)))
      (into [:ul] lis)))

(defn html
  "posts is seq of Post"
  [posts]
  (let [{:keys [text title]}
        (some #(when (policy/introduction-post? %) %) posts)]
    (html5 (head :css-paths [policy/layout-css];css-paths
                 :title "KUR Creative Blog - Home")
           [:body
            header
            [:article
             [:h1 (policy/normalize-title title)]
             (obsidian-html text)]])))