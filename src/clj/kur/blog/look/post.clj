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

(defn prev-next
  "Assume sorted-post-set is sorted in descending order
   So, get n,p from (next post prev). ex: kur500 kur404 kur000"
  [post sorted-post-set]
  (let [prev (first (subseq sorted-post-set > post))
        next (first (rsubseq sorted-post-set < post))]
    [(when-not (policy/admin-post? prev) prev)
     (when-not (policy/admin-post? next) next)]))

#_(def ids (apply sorted-set-by #(compare %2 %1)
                  (map :id sorted-post-set)
                  #_(take-last 10 (map :id sorted-post-set))))

(defn related-post-link [post]
  (if post
    (link-to {:class "hover-link"} (:id post) (:id post))
    [:span {:class "no-post"} "no more posts"])); id always 3(kur) + 10(YYMMddHHmm)

(defn html [post posts]
  (let [norm-title (-> post post/title-or-id policy/normalize-title)
        html-str (obsidian-html (:text post))
        series-name (:name (tags/series post))
        [prev next] (prev-next post posts)
        #_[prev-chapter next-chapter]
        #_(prev-next post
                     (apply)
                     (filter #(= series (-> % tags/series-info :name))))]
    ;(def posts posts) (map #(dissoc % :text) posts)
    (html5 (article-page
            {:title norm-title
             :more-tags (when (has-code? html-str)
                          [policy/agate-code-style-link])}
            {:content
             (list (when series-name
                     (link-to {:class "series-top-link"}
                              (str "series#" series-name) series-name))
                   [:h1 norm-title]
                   html-str
                   ; tags pane
                   [:div {:class "adjacent-in-time"}
                    (related-post-link prev)
                    (link-to {:class "hover-link"}
                             (str "archive#" (:id post)) "Archive")
                    (related-post-link next)])}))))

;;
(comment
  (def text "# 1 \n ## 2 \n ppap \n\n bbab \n - 1 \n - 22")
  (spit "t.html" (html [] "TITLE!!!" text))
  #_(spit "t.html"
          (html ["test/fixture/css/test-p/red-p.css"] text))

  (has-code? "<code class=\"hljs language-bash\">sudo pacman -Syu
sudo pacman -S clojure
</code>"))
