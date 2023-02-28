(ns kur.blog.look.error
  (:require [hiccup.page :refer [html5]]
            [kur.blog.look.template :refer [article-page]]
            [kur.blog.page.post.md2x :refer [obsidian-html]]
            [kur.blog.policy :as policy]))

(defn error-page
  [posts error-post? title]
  (let [{:keys [text]} (some #(when (error-post? %) %) posts)]
    (html5 (article-page {:title title}
                         {:content (when text (obsidian-html text))}))))
(defn page-404 [posts]
  (error-page posts
              policy/id-404-post? "KUR Creative Blog - 404 Not found"))
(defn page-50x [posts]
  (error-page posts
              policy/id-50x-post? "KUR Creative Blog - 50x Internal server error"))