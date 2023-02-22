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

(defn head [& {:keys [js-paths css-paths title]}]
  [:head
   scale1-viewport
   charset-utf8
   google-analytics-script
   favicon
   (apply include-css css-paths)
   (apply include-js js-paths)
   [:title title]])

(def navigation
  [:nav {:class "navigation"}
   (link-to "archive" "archive")
   (link-to "series" "series")
   (link-to "tags" "tags")
   (link-to "guests" "guests")
   [:img {:src policy/search-logo :height "20px" :width "20px"}]])
   ;; TODO: link to google site search url

(def header
  [:header {:class "container"}
   [:hr]
   [:div {:class "site-title"} (link-to "/" "KUR Creative")]
   [:hr]
   navigation
   [:hr]])