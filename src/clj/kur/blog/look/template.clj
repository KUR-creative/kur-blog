(ns kur.blog.look.template
  (:require [hiccup.page :refer [include-css include-js]]))

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