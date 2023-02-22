(ns kur.blog.look.post
  (:require [hiccup.element :refer [link-to]]
            [hiccup.page :refer [html5]]
            [kur.blog.look.template :refer [head]]
            [kur.blog.page.post :as post]
            [kur.blog.page.post.md2x :refer [obsidian-html]]
            [kur.blog.policy :as policy]))

(defn post-link-li [post]
  [:li (link-to (:id post)
                (policy/normalize-title (post/title-or-id post)))])

(defn html [title md-text]
  (let [norm-title (policy/normalize-title title)]
    (html5 (head :css-paths [policy/layout-css] ;css-paths
                 :title norm-title)
           [:body
            [:article {:class "container"}
             [:h1 norm-title]
             (obsidian-html md-text)]])))

;;
(comment
  (def text "# 1 \n ## 2 \n ppap \n\n bbab \n - 1 \n - 22")
  (spit "t.html" (html [] "TITLE!!!" text))
  #_(spit "t.html"
          (html ["test/fixture/css/test-p/red-p.css"] text)))
