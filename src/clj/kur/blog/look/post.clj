(ns kur.blog.look.post
  (:require [hiccup.element :refer [link-to]]
            [hiccup.page :refer [html5]]
            [kur.blog.look.template :refer [head header]]
            [kur.blog.page.post :as post]
            [kur.blog.page.post.md2x :refer [obsidian-html]]
            [kur.blog.policy :as policy]))

(defn post-link-li [post]
  [:li (link-to (:id post)
                (policy/normalize-title (post/title-or-id post)))])

(defn has-code? [html-str]
  (re-find #"<code.+hljs.+>" html-str))

(defn html [title md-text]
  (let [norm-title (policy/normalize-title title)
        html-str (obsidian-html md-text)]
    (html5 (head :css-paths policy/common-css-paths
                 :title norm-title
                 :more-tags (when (has-code? html-str)
                              [policy/agate-code-style-link]))
           [:body
            header
            [:article {:class "container"}
             [:h1 norm-title]
             html-str]])))

;;
(comment
  (def text "# 1 \n ## 2 \n ppap \n\n bbab \n - 1 \n - 22")
  (spit "t.html" (html [] "TITLE!!!" text))
  #_(spit "t.html"
          (html ["test/fixture/css/test-p/red-p.css"] text))

  (has-code? "<code class=\"hljs language-bash\">sudo pacman -Syu
sudo pacman -S clojure
</code>"))
