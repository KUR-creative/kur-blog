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

(defn head [& {:keys [js-paths css-paths title]}]
  [:head
   scale1-viewport
   charset-utf8
   google-analytics-script
   (apply include-css css-paths)
   (apply include-js js-paths)
   [:title title]])

(def navigation
  [:nav
   (link-to "archive" "archive")
   (link-to "series" "series")
   (link-to "tags" "tags")
   (link-to "guests" "guests")])

(def header
  [:header
   [:img {:src policy/kur-logo :height "60px" :width "60px"}]
   [:div "KUR Creative"]
   [:img {:src policy/search-logo :height "60px" :width "60px"}]
   [:hr]
   navigation])