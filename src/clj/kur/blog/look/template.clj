(ns kur.blog.look.template
  (:require [hiccup.element :refer [link-to]]
            [hiccup.page :refer [include-css include-js]]
            [kur.blog.policy :as policy]))

(def scale1-viewport
  [:meta {:name "viewport"
          :content "width=device-width, initial-scale=1"}])

(def charset-utf8 [:meta {:charset "utf-8"}])

(def google-analytics-script
  (slurp "src/html/google-analytics.html"))

(def favicon
  [:link {:href policy/favicon :rel "icon" :type "image/x-icon"}])

(defn head [& {:keys [js-paths css-paths title more-tags]}]
  (into [:head
         scale1-viewport
         charset-utf8
         google-analytics-script
         favicon
         (apply include-css css-paths)
         (apply include-js js-paths)
         [:title title]]
        more-tags))

;; Common
(defn only-cursor-link-to [url content]
  (link-to {:class "only-cursor"} url content))

(defn heading-link [heading]
  (only-cursor-link-to (str "#" (policy/slugify heading)) heading))

(def navigation
  [:nav.navigation
   (link-to {:class "only-cursor"} "archive" "archive")
   (link-to {:class "only-cursor"} "series" "series")
   (link-to {:class "only-cursor"} "tags" "tags")
   (link-to {:class "only-cursor"} "subscribe" "subscribe")
   (link-to "https://www.google.com/search?q=site%3Ablog.kurcreative.com+공사중.."
            [:img {:src policy/search-logo :height "20px" :width "20px"}])])
   ;; TODO: link to google site search url

(def header
  [:header.container
   [:hr]
   [:div.site-title
    (link-to {:class "only-cursor"} "/" "KUR Creative")]
   [:hr]
   navigation
   [:hr]])

(def footer
  [:footer.container
   [:div.site-footer "Copyright 2023 KUR. All Rights Reserved."]])

(defn article-page
  [{:keys [css-paths title more-tags]} {:keys [h1 content]}]
  (list (head :css-paths (if css-paths
                           css-paths
                           policy/common-css-paths)
              :title title
              :more-tags more-tags)
        [:body
         header
         [:article.container
          (when (seq h1) [:h1 h1])
          (when content content)]
         footer]))