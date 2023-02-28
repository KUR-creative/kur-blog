(ns kur.blog.look.home
  (:require [hiccup.page :refer [html5]]
            [kur.blog.look.template :refer [article-page]]
            [kur.blog.page.post.md2x :refer [obsidian-html]]
            [kur.blog.policy :as policy]))

(defn html [posts]
  (let [{:keys [text title]}
        (some #(when (policy/introduction-post? %) %) posts)]
    (html5 (article-page {:title "KUR Creative Blog - Home"}
                         {:h1 (policy/normalize-title title)
                          :content (obsidian-html text)}))))