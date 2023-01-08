(ns kur.blog.look.post
  (:require
   [hiccup.page :refer [html5]]
   [kur.blog.page.post.md2x :refer [obsidian-html]]
   [kur.blog.look.template :refer [head]]))

(defn html [css-paths md-text]
  (html5 (head :css-paths css-paths)
         [:body (obsidian-html md-text)]))

;;
(comment
  (def text "# 1 \n ## 2 \n ppap \n\n bbab \n - 1 \n - 22")
  (spit "t.html" (html [] text))
  #_(spit "t.html"
          (html ["test/fixture/css/test-p/red-p.css"] text)))
