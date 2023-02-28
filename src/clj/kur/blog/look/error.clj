(ns kur.blog.look.error
  (:require [hiccup.page :refer [html5]]
            [kur.blog.look.template :refer [head header]]
            [kur.blog.page.post.md2x :refer [obsidian-html]]
            [kur.blog.policy :as policy]))

(defn page-404
  "posts is seq of Post"
  [posts]
  (let [{:keys [text title]}
        (some #(when (policy/id-404-post? %) %) posts)]
    (html5 (head :css-paths policy/common-css-paths
                 :title "KUR Creative Blog - 404 Not found")
           [:body
            header
            [:article {:class "container"}
             #_(when title [:h1 (policy/normalize-title title)])
             (when text (obsidian-html text))]])))


(defn page-50x
  "posts is seq of Post"
  [posts]
  (let [{:keys [text title]}
        (some #(when (policy/id-50x-post? %) %) posts)]
    (html5 (head :css-paths policy/common-css-paths
                 :title "KUR Creative Blog - 50x Internal server error")
           [:body
            header
            [:article {:class "container"}
             #_(when title [:h1 (policy/normalize-title title)])
             (when text (obsidian-html text))]])))