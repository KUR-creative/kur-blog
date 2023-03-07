(ns kur.blog.look.post
  (:require [hiccup.element :refer [link-to]]
            [hiccup.page :refer [html5]]
            [kur.blog.look.template :refer [article-page]]
            [kur.blog.page.post :as post]
            [kur.blog.page.post.md2x :refer [obsidian-html]]
            [kur.blog.page.tags :as tags]
            [kur.blog.policy :as policy]))

(defn post-link-li [post & {:keys [fragment-id]}]
  [:li
   (when fragment-id {:id fragment-id})
   (link-to (:id post)
            (policy/normalize-title (post/title-or-id post)))])

(defn has-code? [html-str]
  (re-find #"<code.+hljs.+>" html-str))

(defn html [post]
  (let [norm-title (-> post post/title-or-id policy/normalize-title)
        html-str (obsidian-html (:text post))
        tags (-> post :frontmatter :tags)
        series-name (:name (some #(tags/series-info %) tags)); TODO: now just only one series. but..
        ]
    (html5 (article-page
            {:title norm-title
             :more-tags (when (has-code? html-str)
                          [policy/agate-code-style-link])}
            {:content
             (list (when series-name
                     (link-to {:class "series-top-link"}
                              (str "series#" series-name) series-name))
                   [:h1 norm-title]
                   html-str)}))))

;;
(comment
  (def text "# 1 \n ## 2 \n ppap \n\n bbab \n - 1 \n - 22")
  (spit "t.html" (html [] "TITLE!!!" text))
  #_(spit "t.html"
          (html ["test/fixture/css/test-p/red-p.css"] text))

  (has-code? "<code class=\"hljs language-bash\">sudo pacman -Syu
sudo pacman -S clojure
</code>"))
