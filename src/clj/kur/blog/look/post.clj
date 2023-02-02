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

(defn html [css-paths title md-text]
  (html5 (head :css-paths css-paths)
         [:body
          [:h1 (policy/normalize-title title)]
          (obsidian-html md-text)]))

;;
(comment
  (def text "# 1 \n ## 2 \n ppap \n\n bbab \n - 1 \n - 22")
  (spit "t.html" (html [] "TITLE!!!" text))
  #_(spit "t.html"
          (html ["test/fixture/css/test-p/red-p.css"] text)))
